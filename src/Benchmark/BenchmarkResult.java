package Benchmark;

import Cryptography.AlgorithmName;

/**
 * One measured sample. A sample may contain a batch of fast operations; the
 * per-operation duration is derived from the measured batch duration.
 */
public final class BenchmarkResult {

    private final String runId;
    private final BenchmarkGroup group;
    private final AlgorithmName algorithm;
    private final BenchmarkOperation operation;
    private final String parameters;
    private final int inputSizeBytes;
    private final int sampleNumber;
    private final int operationsPerSample;
    private final long sampleDurationNanoseconds;
    private final Long estimatedPeakMemoryBytes;
    private final int outputSizeBytes;
    private final int metadataSizeBytes;
    private final boolean successful;

    public BenchmarkResult(
            String runId,
            BenchmarkGroup group,
            AlgorithmName algorithm,
            BenchmarkOperation operation,
            String parameters,
            int inputSizeBytes,
            int sampleNumber,
            int operationsPerSample,
            long sampleDurationNanoseconds,
            Long estimatedPeakMemoryBytes,
            int outputSizeBytes,
            int metadataSizeBytes,
            boolean successful) {
        this.runId = required(runId, "runId");
        this.group = required(group, "group");
        this.algorithm = required(algorithm, "algorithm");
        this.operation = required(operation, "operation");
        this.parameters = parameters == null ? "" : parameters;
        this.inputSizeBytes = nonNegative(inputSizeBytes, "inputSizeBytes");
        this.sampleNumber = positive(sampleNumber, "sampleNumber");
        this.operationsPerSample = positive(operationsPerSample, "operationsPerSample");
        this.sampleDurationNanoseconds = nonNegative(sampleDurationNanoseconds, "sampleDurationNanoseconds");
        this.estimatedPeakMemoryBytes = estimatedPeakMemoryBytes;
        this.outputSizeBytes = nonNegative(outputSizeBytes, "outputSizeBytes");
        this.metadataSizeBytes = nonNegative(metadataSizeBytes, "metadataSizeBytes");
        this.successful = successful;
    }

    public double getAverageDurationNanoseconds() {
        return sampleDurationNanoseconds / (double) operationsPerSample;
    }

    public double getAverageDurationMilliseconds() {
        return getAverageDurationNanoseconds() / 1_000_000.0;
    }

    private static <T> T required(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value;
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value;
    }

    private static int positive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than zero.");
        }
        return value;
    }

    private static int nonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative.");
        }
        return value;
    }

    private static long nonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative.");
        }
        return value;
    }

    public String getRunId() {
        return runId;
    }

    public BenchmarkGroup getGroup() {
        return group;
    }

    public AlgorithmName getAlgorithm() {
        return algorithm;
    }

    public BenchmarkOperation getOperation() {
        return operation;
    }

    public String getParameters() {
        return parameters;
    }

    public int getInputSizeBytes() {
        return inputSizeBytes;
    }

    public int getSampleNumber() {
        return sampleNumber;
    }

    public int getOperationsPerSample() {
        return operationsPerSample;
    }

    public long getSampleDurationNanoseconds() {
        return sampleDurationNanoseconds;
    }

    public Long getEstimatedPeakMemoryBytes() {
        return estimatedPeakMemoryBytes;
    }

    public int getOutputSizeBytes() {
        return outputSizeBytes;
    }

    public int getMetadataSizeBytes() {
        return metadataSizeBytes;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
