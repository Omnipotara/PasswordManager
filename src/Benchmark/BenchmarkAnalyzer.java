package Benchmark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BenchmarkAnalyzer {

    private BenchmarkAnalyzer() {
    }

    public static List<BenchmarkSummary> summarize(List<BenchmarkResult> results) {
        Map<SummaryKey, List<BenchmarkResult>> grouped = new LinkedHashMap<>();
        for (BenchmarkResult result : results) {
            SummaryKey key = new SummaryKey(result);
            grouped.computeIfAbsent(key, unused -> new ArrayList<>()).add(result);
        }

        List<BenchmarkSummary> summaries = new ArrayList<>();
        for (List<BenchmarkResult> comparableResults : grouped.values()) {
            summaries.add(BenchmarkSummary.from(comparableResults));
        }
        summaries.sort(Comparator
                .comparing(BenchmarkSummary::getGroup)
                .thenComparing(BenchmarkSummary::getAlgorithm)
                .thenComparing(BenchmarkSummary::getOperation)
                .thenComparingInt(BenchmarkSummary::getInputSizeBytes));
        return summaries;
    }

    private static final class SummaryKey {

        private final BenchmarkGroup group;
        private final String algorithm;
        private final BenchmarkOperation operation;
        private final String parameters;
        private final int inputSizeBytes;

        private SummaryKey(BenchmarkResult result) {
            this.group = result.getGroup();
            this.algorithm = result.getAlgorithm().getDatabaseValue();
            this.operation = result.getOperation();
            this.parameters = result.getParameters();
            this.inputSizeBytes = result.getInputSizeBytes();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof SummaryKey)) {
                return false;
            }
            SummaryKey key = (SummaryKey) other;
            return group == key.group
                    && algorithm.equals(key.algorithm)
                    && operation == key.operation
                    && parameters.equals(key.parameters)
                    && inputSizeBytes == key.inputSizeBytes;
        }

        @Override
        public int hashCode() {
            int hash = group.hashCode();
            hash = 31 * hash + algorithm.hashCode();
            hash = 31 * hash + operation.hashCode();
            hash = 31 * hash + parameters.hashCode();
            hash = 31 * hash + inputSizeBytes;
            return hash;
        }
    }
}
