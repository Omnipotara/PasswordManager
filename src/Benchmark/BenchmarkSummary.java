package Benchmark;

import Cryptography.AlgorithmName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Aggregated statistics for comparable samples. */
public final class BenchmarkSummary {

    private final BenchmarkGroup group;
    private final AlgorithmName algorithm;
    private final BenchmarkOperation operation;
    private final String parameters;
    private final int inputSizeBytes;
    private final int sampleCount;
    private final long totalOperations;
    private final double minimumNanoseconds;
    private final double medianNanoseconds;
    private final double averageNanoseconds;
    private final double percentile95Nanoseconds;
    private final double maximumNanoseconds;
    private final double standardDeviationNanoseconds;
    private final Long averageEstimatedPeakMemoryBytes;
    private final double averageOutputSizeBytes;
    private final double averageMetadataSizeBytes;

    private BenchmarkSummary(
            BenchmarkResult first,
            int sampleCount,
            long totalOperations,
            double minimumNanoseconds,
            double medianNanoseconds,
            double averageNanoseconds,
            double percentile95Nanoseconds,
            double maximumNanoseconds,
            double standardDeviationNanoseconds,
            Long averageEstimatedPeakMemoryBytes,
            double averageOutputSizeBytes,
            double averageMetadataSizeBytes) {
        this.group = first.getGroup();
        this.algorithm = first.getAlgorithm();
        this.operation = first.getOperation();
        this.parameters = first.getParameters();
        this.inputSizeBytes = first.getInputSizeBytes();
        this.sampleCount = sampleCount;
        this.totalOperations = totalOperations;
        this.minimumNanoseconds = minimumNanoseconds;
        this.medianNanoseconds = medianNanoseconds;
        this.averageNanoseconds = averageNanoseconds;
        this.percentile95Nanoseconds = percentile95Nanoseconds;
        this.maximumNanoseconds = maximumNanoseconds;
        this.standardDeviationNanoseconds = standardDeviationNanoseconds;
        this.averageEstimatedPeakMemoryBytes = averageEstimatedPeakMemoryBytes;
        this.averageOutputSizeBytes = averageOutputSizeBytes;
        this.averageMetadataSizeBytes = averageMetadataSizeBytes;
    }

    public static BenchmarkSummary from(List<BenchmarkResult> results) {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("At least one benchmark result is required.");
        }

        BenchmarkResult first = results.get(0);
        List<Double> durations = new ArrayList<>();
        long totalOperations = 0;
        long memoryTotal = 0;
        int memorySamples = 0;
        long outputTotal = 0;
        long metadataTotal = 0;

        for (BenchmarkResult result : results) {
            ensureComparable(first, result);
            durations.add(result.getAverageDurationNanoseconds());
            totalOperations += result.getOperationsPerSample();
            outputTotal += result.getOutputSizeBytes();
            metadataTotal += result.getMetadataSizeBytes();
            if (result.getEstimatedPeakMemoryBytes() != null) {
                memoryTotal += result.getEstimatedPeakMemoryBytes();
                memorySamples++;
            }
        }

        Collections.sort(durations);
        double average = durations.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = durations.stream()
                .mapToDouble(duration -> Math.pow(duration - average, 2))
                .average()
                .orElse(0);

        return new BenchmarkSummary(
                first,
                results.size(),
                totalOperations,
                durations.get(0),
                median(durations),
                average,
                percentile(durations, 0.95),
                durations.get(durations.size() - 1),
                Math.sqrt(variance),
                memorySamples == 0 ? null : Math.round(memoryTotal / (double) memorySamples),
                outputTotal / (double) results.size(),
                metadataTotal / (double) results.size()
        );
    }

    private static void ensureComparable(BenchmarkResult first, BenchmarkResult candidate) {
        boolean comparable = first.getGroup() == candidate.getGroup()
                && first.getAlgorithm() == candidate.getAlgorithm()
                && first.getOperation() == candidate.getOperation()
                && first.getInputSizeBytes() == candidate.getInputSizeBytes()
                && first.getParameters().equals(candidate.getParameters());
        if (!comparable) {
            throw new IllegalArgumentException("Cannot summarize non-comparable benchmark results.");
        }
    }

    private static double median(List<Double> sortedValues) {
        int middle = sortedValues.size() / 2;
        if (sortedValues.size() % 2 == 1) {
            return sortedValues.get(middle);
        }
        return (sortedValues.get(middle - 1) + sortedValues.get(middle)) / 2.0;
    }

    private static double percentile(List<Double> sortedValues, double percentile) {
        int rank = (int) Math.ceil(percentile * sortedValues.size());
        int index = Math.max(0, Math.min(sortedValues.size() - 1, rank - 1));
        return sortedValues.get(index);
    }

    public double getMinimumMilliseconds() {
        return minimumNanoseconds / 1_000_000.0;
    }

    public double getMedianMilliseconds() {
        return medianNanoseconds / 1_000_000.0;
    }

    public double getAverageMilliseconds() {
        return averageNanoseconds / 1_000_000.0;
    }

    public double getPercentile95Milliseconds() {
        return percentile95Nanoseconds / 1_000_000.0;
    }

    public double getMaximumMilliseconds() {
        return maximumNanoseconds / 1_000_000.0;
    }

    public double getStandardDeviationMilliseconds() {
        return standardDeviationNanoseconds / 1_000_000.0;
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

    public int getSampleCount() {
        return sampleCount;
    }

    public long getTotalOperations() {
        return totalOperations;
    }

    public double getMinimumNanoseconds() {
        return minimumNanoseconds;
    }

    public double getMedianNanoseconds() {
        return medianNanoseconds;
    }

    public double getAverageNanoseconds() {
        return averageNanoseconds;
    }

    public double getPercentile95Nanoseconds() {
        return percentile95Nanoseconds;
    }

    public double getMaximumNanoseconds() {
        return maximumNanoseconds;
    }

    public double getStandardDeviationNanoseconds() {
        return standardDeviationNanoseconds;
    }

    public Long getAverageEstimatedPeakMemoryBytes() {
        return averageEstimatedPeakMemoryBytes;
    }

    public double getAverageOutputSizeBytes() {
        return averageOutputSizeBytes;
    }

    public double getAverageMetadataSizeBytes() {
        return averageMetadataSizeBytes;
    }
}
