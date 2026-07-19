package Benchmark.Hashing;

import Benchmark.BenchmarkConfig;
import Benchmark.BenchmarkGroup;
import Benchmark.BenchmarkOperation;
import Benchmark.BenchmarkResult;
import Benchmark.PeakMemoryMonitor;
import Cryptography.Hashing.Argon2idStrategy;
import Cryptography.Hashing.BCryptStrategy;
import Cryptography.Hashing.HashingStrategy;
import Cryptography.Hashing.PBKDF2HashingStrategy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Benchmarks the exact hashing strategies used by registration and login. */
public final class HashingBenchmarkRunner {

    private static final String PASSWORD = "BenchmarkMasterPassword123!";

    private final List<HashingStrategy> strategies = Arrays.asList(
            new BCryptStrategy(),
            new PBKDF2HashingStrategy(),
            new Argon2idStrategy()
    );

    public List<BenchmarkResult> run(String runId, BenchmarkConfig config) {
        warmUp(config.getHashWarmupIterations());

        List<BenchmarkResult> results = new ArrayList<>();
        int inputSizeBytes = PASSWORD.getBytes(StandardCharsets.UTF_8).length;

        for (int sample = 1; sample <= config.getHashMeasuredIterations(); sample++) {
            for (HashingStrategy strategy : rotatedStrategies(sample)) {
                MeasuredHash measuredHash = measureHash(strategy);
                results.add(new BenchmarkResult(
                        runId,
                        BenchmarkGroup.HASHING,
                        strategy.getAlgorithmName(),
                        BenchmarkOperation.HASH,
                        strategy.getParametersDescription(),
                        inputSizeBytes,
                        sample,
                        1,
                        measuredHash.durationNanoseconds,
                        measuredHash.estimatedPeakMemoryBytes,
                        measuredHash.storedHash.getBytes(StandardCharsets.UTF_8).length,
                        0,
                        true
                ));

                MeasuredVerification verification = measureVerification(strategy, measuredHash.storedHash);
                if (!verification.verified) {
                    throw new IllegalStateException(strategy.getAlgorithmName() + " failed benchmark verification.");
                }
                results.add(new BenchmarkResult(
                        runId,
                        BenchmarkGroup.HASHING,
                        strategy.getAlgorithmName(),
                        BenchmarkOperation.VERIFY,
                        strategy.getParametersDescription(),
                        inputSizeBytes,
                        sample,
                        1,
                        verification.durationNanoseconds,
                        verification.estimatedPeakMemoryBytes,
                        0,
                        0,
                        true
                ));
            }
        }
        return results;
    }

    private void warmUp(int iterations) {
        for (int iteration = 1; iteration <= iterations; iteration++) {
            for (HashingStrategy strategy : rotatedStrategies(iteration)) {
                String storedHash = strategy.hash(PASSWORD);
                if (!strategy.verify(PASSWORD, storedHash)) {
                    throw new IllegalStateException(strategy.getAlgorithmName() + " failed during warm-up.");
                }
            }
        }
    }

    private MeasuredHash measureHash(HashingStrategy strategy) {
        PeakMemoryMonitor memoryMonitor = PeakMemoryMonitor.start();
        String storedHash;
        long duration;
        try {
            long start = System.nanoTime();
            storedHash = strategy.hash(PASSWORD);
            duration = System.nanoTime() - start;
        } finally {
            memoryMonitor.close();
        }
        return new MeasuredHash(storedHash, duration, memoryMonitor.getEstimatedPeakDeltaBytes());
    }

    private MeasuredVerification measureVerification(HashingStrategy strategy, String storedHash) {
        PeakMemoryMonitor memoryMonitor = PeakMemoryMonitor.start();
        boolean verified;
        long duration;
        try {
            long start = System.nanoTime();
            verified = strategy.verify(PASSWORD, storedHash);
            duration = System.nanoTime() - start;
        } finally {
            memoryMonitor.close();
        }
        return new MeasuredVerification(verified, duration, memoryMonitor.getEstimatedPeakDeltaBytes());
    }

    private List<HashingStrategy> rotatedStrategies(int iteration) {
        List<HashingStrategy> rotated = new ArrayList<>(strategies.size());
        int startIndex = (iteration - 1) % strategies.size();
        for (int offset = 0; offset < strategies.size(); offset++) {
            rotated.add(strategies.get((startIndex + offset) % strategies.size()));
        }
        return rotated;
    }

    private static final class MeasuredHash {

        private final String storedHash;
        private final long durationNanoseconds;
        private final long estimatedPeakMemoryBytes;

        private MeasuredHash(String storedHash, long durationNanoseconds, long estimatedPeakMemoryBytes) {
            this.storedHash = storedHash;
            this.durationNanoseconds = durationNanoseconds;
            this.estimatedPeakMemoryBytes = estimatedPeakMemoryBytes;
        }
    }

    private static final class MeasuredVerification {

        private final boolean verified;
        private final long durationNanoseconds;
        private final long estimatedPeakMemoryBytes;

        private MeasuredVerification(boolean verified, long durationNanoseconds, long estimatedPeakMemoryBytes) {
            this.verified = verified;
            this.durationNanoseconds = durationNanoseconds;
            this.estimatedPeakMemoryBytes = estimatedPeakMemoryBytes;
        }
    }
}
