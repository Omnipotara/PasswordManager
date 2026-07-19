package Benchmark.Encryption;

import Benchmark.BenchmarkConfig;
import Benchmark.BenchmarkGroup;
import Benchmark.BenchmarkOperation;
import Benchmark.BenchmarkResult;
import Benchmark.StorageSizeCalculator;
import Cryptography.Encryption.AESCBCStrategy;
import Cryptography.Encryption.AESGCMStrategy;
import Cryptography.Encryption.ChaCha20Poly1305Strategy;
import Cryptography.Encryption.EncryptionStrategy;
import Cryptography.KeyDerivation.KeyDerivationService;
import Cryptography.Model.EncryptedData;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import javax.crypto.SecretKey;

/** Benchmarks password-entry encryption without timing PBKDF2 key derivation. */
public final class EncryptionBenchmarkRunner {

    private static final String MASTER_PASSWORD = "BenchmarkMasterPassword123!";
    private static final byte[] KEY_DERIVATION_SALT = "BenchmarkSalt123".getBytes(StandardCharsets.UTF_8);

    private final List<EncryptionStrategy> strategies = Arrays.asList(
            new AESGCMStrategy(),
            new AESCBCStrategy(),
            new ChaCha20Poly1305Strategy()
    );
    private final SecretKey key = new KeyDerivationService().deriveEncryptionKey(
            MASTER_PASSWORD,
            KEY_DERIVATION_SALT
    );

    public List<BenchmarkResult> run(String runId, BenchmarkConfig config) {
        List<BenchmarkResult> results = new ArrayList<>();
        List<InputCase> inputCases = prepareInputCases(config.getEncryptionInputSizes());
        warmUp(inputCases, config.getEncryptionWarmupOperations());

        for (InputCase inputCase : inputCases) {
            measureInputSize(
                    runId,
                    config,
                    inputCase.plaintext,
                    inputCase.encryptedByAlgorithm,
                    results
            );
        }
        return results;
    }

    private List<InputCase> prepareInputCases(int[] inputSizes) {
        List<InputCase> inputCases = new ArrayList<>();
        for (int inputSize : inputSizes) {
            String plaintext = plaintextOfSize(inputSize);
            inputCases.add(new InputCase(plaintext, prepareInputs(plaintext)));
        }
        return inputCases;
    }

    private Map<Cryptography.AlgorithmName, EncryptedData> prepareInputs(String plaintext) {
        Map<Cryptography.AlgorithmName, EncryptedData> encryptedByAlgorithm =
                new EnumMap<>(Cryptography.AlgorithmName.class);
        for (EncryptionStrategy strategy : strategies) {
            encryptedByAlgorithm.put(strategy.getAlgorithmName(), strategy.encrypt(plaintext, key));
        }
        return encryptedByAlgorithm;
    }

    private void warmUp(List<InputCase> inputCases, int warmupOperations) {
        for (int iteration = 1; iteration <= warmupOperations; iteration++) {
            for (InputCase inputCase : inputCases) {
                for (EncryptionStrategy strategy : rotatedStrategies(iteration)) {
                    EncryptedData encrypted = strategy.encrypt(inputCase.plaintext, key);
                    String decrypted = strategy.decrypt(
                            inputCase.encryptedByAlgorithm.get(strategy.getAlgorithmName()),
                            key
                    );
                    if (!inputCase.plaintext.equals(decrypted)) {
                        throw new IllegalStateException(strategy.getAlgorithmName() + " failed during warm-up.");
                    }
                    inputCase.encryptedByAlgorithm.put(strategy.getAlgorithmName(), encrypted);
                }
            }
        }
    }

    private void measureInputSize(
            String runId,
            BenchmarkConfig config,
            String plaintext,
            Map<Cryptography.AlgorithmName, EncryptedData> encryptedByAlgorithm,
            List<BenchmarkResult> results) {
        int inputSize = plaintext.getBytes(StandardCharsets.UTF_8).length;
        int operations = config.getEncryptionOperationsPerSample();

        for (int sample = 1; sample <= config.getEncryptionMeasuredSamples(); sample++) {
            for (EncryptionStrategy strategy : rotatedStrategies(sample)) {
                EncryptedBatch encryptedBatch = measureEncryptionBatch(strategy, plaintext, operations);
                verifyRoundTrip(strategy, plaintext, encryptedBatch.lastEncryptedData);
                encryptedByAlgorithm.put(strategy.getAlgorithmName(), encryptedBatch.lastEncryptedData);

                results.add(resultFor(
                        runId,
                        strategy,
                        BenchmarkOperation.ENCRYPT,
                        encryptedBatch.lastEncryptedData,
                        inputSize,
                        sample,
                        operations,
                        encryptedBatch.durationNanoseconds
                ));
            }

            for (EncryptionStrategy strategy : rotatedStrategies(sample)) {
                EncryptedData encryptedData = encryptedByAlgorithm.get(strategy.getAlgorithmName());
                DecryptedBatch decryptedBatch = measureDecryptionBatch(strategy, encryptedData, operations);
                if (!plaintext.equals(decryptedBatch.lastPlaintext)) {
                    throw new IllegalStateException(strategy.getAlgorithmName() + " failed benchmark decryption.");
                }

                results.add(resultFor(
                        runId,
                        strategy,
                        BenchmarkOperation.DECRYPT,
                        encryptedData,
                        inputSize,
                        sample,
                        operations,
                        decryptedBatch.durationNanoseconds
                ));
            }
        }
    }

    private EncryptedBatch measureEncryptionBatch(
            EncryptionStrategy strategy,
            String plaintext,
            int operations) {
        EncryptedData lastEncrypted = null;
        long start = System.nanoTime();
        for (int operation = 0; operation < operations; operation++) {
            lastEncrypted = strategy.encrypt(plaintext, key);
        }
        long duration = System.nanoTime() - start;
        return new EncryptedBatch(lastEncrypted, duration);
    }

    private DecryptedBatch measureDecryptionBatch(
            EncryptionStrategy strategy,
            EncryptedData encryptedData,
            int operations) {
        String lastPlaintext = null;
        long start = System.nanoTime();
        for (int operation = 0; operation < operations; operation++) {
            lastPlaintext = strategy.decrypt(encryptedData, key);
        }
        long duration = System.nanoTime() - start;
        return new DecryptedBatch(lastPlaintext, duration);
    }

    private BenchmarkResult resultFor(
            String runId,
            EncryptionStrategy strategy,
            BenchmarkOperation operation,
            EncryptedData encryptedData,
            int inputSize,
            int sample,
            int operations,
            long durationNanoseconds) {
        return new BenchmarkResult(
                runId,
                BenchmarkGroup.ENCRYPTION,
                strategy.getAlgorithmName(),
                operation,
                parametersOf(encryptedData),
                inputSize,
                sample,
                operations,
                durationNanoseconds,
                null,
                StorageSizeCalculator.ciphertextBytes(encryptedData),
                StorageSizeCalculator.metadataBytes(encryptedData),
                true
        );
    }

    private String parametersOf(EncryptedData encryptedData) {
        int ivBytes = Base64.getDecoder().decode(encryptedData.getIvBase64()).length;
        int tagBytes = Base64.getDecoder().decode(encryptedData.getAuthenticationTagBase64()).length;
        return "keyBits=256;ivOrNonceBytes=" + ivBytes
                + ";authenticationTagBytes=" + tagBytes
                + ";" + encryptedData.getParameters();
    }

    private void verifyRoundTrip(EncryptionStrategy strategy, String plaintext, EncryptedData encryptedData) {
        String decrypted = strategy.decrypt(encryptedData, key);
        if (!plaintext.equals(decrypted)) {
            throw new IllegalStateException(strategy.getAlgorithmName() + " failed benchmark round-trip.");
        }
    }

    private List<EncryptionStrategy> rotatedStrategies(int iteration) {
        List<EncryptionStrategy> rotated = new ArrayList<>(strategies.size());
        int startIndex = (iteration - 1) % strategies.size();
        for (int offset = 0; offset < strategies.size(); offset++) {
            rotated.add(strategies.get((startIndex + offset) % strategies.size()));
        }
        return rotated;
    }

    private String plaintextOfSize(int sizeBytes) {
        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@";
        StringBuilder plaintext = new StringBuilder(sizeBytes);
        for (int index = 0; index < sizeBytes; index++) {
            plaintext.append(alphabet.charAt(index % alphabet.length()));
        }
        return plaintext.toString();
    }

    private static final class EncryptedBatch {

        private final EncryptedData lastEncryptedData;
        private final long durationNanoseconds;

        private EncryptedBatch(EncryptedData lastEncryptedData, long durationNanoseconds) {
            this.lastEncryptedData = lastEncryptedData;
            this.durationNanoseconds = durationNanoseconds;
        }
    }

    private static final class DecryptedBatch {

        private final String lastPlaintext;
        private final long durationNanoseconds;

        private DecryptedBatch(String lastPlaintext, long durationNanoseconds) {
            this.lastPlaintext = lastPlaintext;
            this.durationNanoseconds = durationNanoseconds;
        }
    }

    private static final class InputCase {

        private final String plaintext;
        private final Map<Cryptography.AlgorithmName, EncryptedData> encryptedByAlgorithm;

        private InputCase(
                String plaintext,
                Map<Cryptography.AlgorithmName, EncryptedData> encryptedByAlgorithm) {
            this.plaintext = plaintext;
            this.encryptedByAlgorithm = encryptedByAlgorithm;
        }
    }
}
