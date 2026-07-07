package Cryptography.Hashing;

import Cryptography.AlgorithmName;
import Cryptography.Exceptions.CryptoOperationException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

/**
 * Argon2id implementation used for protecting the user's master password.
 */
public class Argon2idStrategy implements HashingStrategy {

    private static final String FORMAT_PREFIX = "ARGON2ID";
    private static final int VERSION = Argon2Parameters.ARGON2_VERSION_13;
    private static final int MEMORY_KB = 65536;
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 1;
    private static final int HASH_LENGTH_BYTES = 32;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final String SEPARATOR = "\\$";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String hash(String password) {
        byte[] salt = generateSalt();
        byte[] hash = deriveHash(password, salt, VERSION, MEMORY_KB, ITERATIONS, PARALLELISM, HASH_LENGTH_BYTES);

        return FORMAT_PREFIX
                + "$" + VERSION
                + "$" + MEMORY_KB
                + "$" + ITERATIONS
                + "$" + PARALLELISM
                + "$" + HASH_LENGTH_BYTES
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean verify(String password, String storedHash) {
        ParsedArgon2idHash parsedHash = parse(storedHash);
        if (parsedHash == null) {
            return false;
        }

        byte[] calculatedHash = deriveHash(
                password,
                parsedHash.salt,
                parsedHash.version,
                parsedHash.memoryKb,
                parsedHash.iterations,
                parsedHash.parallelism,
                parsedHash.hashLengthBytes
        );

        return MessageDigest.isEqual(calculatedHash, parsedHash.hash);
    }

    @Override
    public AlgorithmName getAlgorithmName() {
        return AlgorithmName.ARGON2ID;
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    private byte[] deriveHash(
            String password,
            byte[] salt,
            int version,
            int memoryKb,
            int iterations,
            int parallelism,
            int hashLengthBytes) {

        char[] passwordChars = password.toCharArray();

        try {
            Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withVersion(version)
                    .withMemoryAsKB(memoryKb)
                    .withIterations(iterations)
                    .withParallelism(parallelism)
                    .withSalt(salt)
                    .build();

            byte[] hash = new byte[hashLengthBytes];
            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(parameters);
            generator.generateBytes(passwordChars, hash);
            return hash;
        } catch (Exception ex) {
            throw new CryptoOperationException("Argon2id password hashing failed.", ex);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    private ParsedArgon2idHash parse(String storedHash) {
        if (storedHash == null) {
            return null;
        }

        String[] parts = storedHash.split(SEPARATOR);
        if (parts.length != 8 || !FORMAT_PREFIX.equals(parts[0])) {
            return null;
        }

        try {
            int version = Integer.parseInt(parts[1]);
            int memoryKb = Integer.parseInt(parts[2]);
            int iterations = Integer.parseInt(parts[3]);
            int parallelism = Integer.parseInt(parts[4]);
            int hashLengthBytes = Integer.parseInt(parts[5]);
            byte[] salt = Base64.getDecoder().decode(parts[6]);
            byte[] hash = Base64.getDecoder().decode(parts[7]);

            if (version <= 0
                    || memoryKb <= 0
                    || iterations <= 0
                    || parallelism <= 0
                    || hashLengthBytes <= 0
                    || salt.length == 0
                    || hash.length != hashLengthBytes) {
                return null;
            }

            return new ParsedArgon2idHash(
                    version,
                    memoryKb,
                    iterations,
                    parallelism,
                    hashLengthBytes,
                    salt,
                    hash
            );
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static class ParsedArgon2idHash {

        private final int version;
        private final int memoryKb;
        private final int iterations;
        private final int parallelism;
        private final int hashLengthBytes;
        private final byte[] salt;
        private final byte[] hash;

        private ParsedArgon2idHash(
                int version,
                int memoryKb,
                int iterations,
                int parallelism,
                int hashLengthBytes,
                byte[] salt,
                byte[] hash) {
            this.version = version;
            this.memoryKb = memoryKb;
            this.iterations = iterations;
            this.parallelism = parallelism;
            this.hashLengthBytes = hashLengthBytes;
            this.salt = salt;
            this.hash = hash;
        }
    }
}
