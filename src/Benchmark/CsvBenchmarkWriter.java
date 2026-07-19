package Benchmark;

import Benchmark.Scoring.AlgorithmScoreSummary;
import Benchmark.Scoring.CategoryScore;
import Benchmark.Scoring.CriterionScore;
import Benchmark.Scoring.EvaluationCriterion;
import Benchmark.Scoring.ScoringReport;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Writes stable UTF-8 CSV files suitable for spreadsheet charts and tables. */
public final class CsvBenchmarkWriter {

    private static final char UTF8_BOM = '\uFEFF';

    public void writeAll(
            Path outputDirectory,
            List<BenchmarkResult> rawResults,
            List<BenchmarkSummary> summaries,
            Map<String, String> environment,
            List<EvaluationCriterion> criteria,
            ScoringReport scoringReport) throws IOException {
        Files.createDirectories(outputDirectory);
        writeRawResults(outputDirectory.resolve("benchmark-raw.csv"), rawResults);
        writeSummaries(outputDirectory.resolve("benchmark-summary.csv"), summaries);
        writeKeyValueCsv(outputDirectory.resolve("benchmark-environment.csv"), "property", "value", environment);
        writeKeyValueCsv(outputDirectory.resolve("benchmark-references.csv"), "reference", "url", BenchmarkReferences.all());
        writeCriteria(outputDirectory.resolve("evaluation-criteria.csv"), criteria);
        writeCriterionScores(outputDirectory.resolve("algorithm-criterion-scores.csv"), scoringReport.getCriterionScores());
        writeCategoryScores(outputDirectory.resolve("algorithm-category-scores.csv"), scoringReport.getCategoryScores());
        writeAlgorithmSummaries(outputDirectory.resolve("algorithm-score-summary.csv"), scoringReport.getAlgorithmSummaries());
    }

    private void writeRawResults(Path path, List<BenchmarkResult> results) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "run_id", "group", "algorithm", "operation", "parameters",
                "input_size_bytes", "sample", "operations_per_sample",
                "sample_duration_ns", "average_duration_ns", "average_duration_ms",
                "estimated_peak_memory_bytes", "output_size_bytes", "metadata_size_bytes", "successful"
        ));
        for (BenchmarkResult result : results) {
            rows.add(Arrays.asList(
                    result.getRunId(),
                    result.getGroup().name(),
                    result.getAlgorithm().getDatabaseValue(),
                    result.getOperation().name(),
                    result.getParameters(),
                    String.valueOf(result.getInputSizeBytes()),
                    String.valueOf(result.getSampleNumber()),
                    String.valueOf(result.getOperationsPerSample()),
                    String.valueOf(result.getSampleDurationNanoseconds()),
                    decimal(result.getAverageDurationNanoseconds()),
                    decimal(result.getAverageDurationMilliseconds()),
                    nullableLong(result.getEstimatedPeakMemoryBytes()),
                    String.valueOf(result.getOutputSizeBytes()),
                    String.valueOf(result.getMetadataSizeBytes()),
                    String.valueOf(result.isSuccessful())
            ));
        }
        writeRows(path, rows);
    }

    private void writeSummaries(Path path, List<BenchmarkSummary> summaries) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "group", "algorithm", "operation", "parameters", "input_size_bytes",
                "sample_count", "total_operations", "min_ms", "median_ms", "average_ms",
                "p95_ms", "max_ms", "standard_deviation_ms", "average_estimated_peak_memory_bytes",
                "average_output_size_bytes", "average_metadata_size_bytes"
        ));
        for (BenchmarkSummary summary : summaries) {
            rows.add(Arrays.asList(
                    summary.getGroup().name(),
                    summary.getAlgorithm().getDatabaseValue(),
                    summary.getOperation().name(),
                    summary.getParameters(),
                    String.valueOf(summary.getInputSizeBytes()),
                    String.valueOf(summary.getSampleCount()),
                    String.valueOf(summary.getTotalOperations()),
                    decimal(summary.getMinimumMilliseconds()),
                    decimal(summary.getMedianMilliseconds()),
                    decimal(summary.getAverageMilliseconds()),
                    decimal(summary.getPercentile95Milliseconds()),
                    decimal(summary.getMaximumMilliseconds()),
                    decimal(summary.getStandardDeviationMilliseconds()),
                    nullableLong(summary.getAverageEstimatedPeakMemoryBytes()),
                    decimal(summary.getAverageOutputSizeBytes()),
                    decimal(summary.getAverageMetadataSizeBytes())
            ));
        }
        writeRows(path, rows);
    }

    private void writeCriteria(Path path, List<EvaluationCriterion> criteria) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("group", "category", "criterion_code", "criterion", "evidence_type", "description"));
        for (EvaluationCriterion criterion : criteria) {
            rows.add(Arrays.asList(
                    criterion.getGroup().name(),
                    criterion.getCategory().getDisplayName(),
                    criterion.getCode(),
                    criterion.getName(),
                    criterion.getEvidenceType().getDisplayName(),
                    criterion.getDescription()
            ));
        }
        writeRows(path, rows);
    }

    private void writeCriterionScores(Path path, List<CriterionScore> scores) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "group", "algorithm", "category", "criterion_code", "criterion",
                "score_1_to_5", "evidence_type", "evidence", "comment"
        ));
        for (CriterionScore score : scores) {
            EvaluationCriterion criterion = score.getCriterion();
            rows.add(Arrays.asList(
                    criterion.getGroup().name(),
                    score.getAlgorithm().getDatabaseValue(),
                    criterion.getCategory().getDisplayName(),
                    criterion.getCode(),
                    criterion.getName(),
                    String.valueOf(score.getScore()),
                    criterion.getEvidenceType().getDisplayName(),
                    score.getEvidence(),
                    score.getComment()
            ));
        }
        writeRows(path, rows);
    }

    private void writeCategoryScores(Path path, List<CategoryScore> scores) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("group", "algorithm", "category", "criterion_count", "average_score"));
        for (CategoryScore score : scores) {
            rows.add(Arrays.asList(
                    score.getGroup().name(),
                    score.getAlgorithm().getDatabaseValue(),
                    score.getCategory().getDisplayName(),
                    String.valueOf(score.getCriterionCount()),
                    decimal(score.getAverageScore())
            ));
        }
        writeRows(path, rows);
    }

    private void writeAlgorithmSummaries(Path path, List<AlgorithmScoreSummary> summaries) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "group", "algorithm", "performance_score", "security_score",
                "implementation_practicality_score", "password_manager_suitability_score",
                "overall_average", "conclusion"
        ));
        for (AlgorithmScoreSummary summary : summaries) {
            rows.add(Arrays.asList(
                    summary.getGroup().name(),
                    summary.getAlgorithm().getDatabaseValue(),
                    decimal(summary.getPerformanceScore()),
                    decimal(summary.getSecurityScore()),
                    decimal(summary.getImplementationPracticalityScore()),
                    decimal(summary.getPasswordManagerSuitabilityScore()),
                    decimal(summary.getOverallAverage()),
                    summary.getConclusion()
            ));
        }
        writeRows(path, rows);
    }

    private void writeKeyValueCsv(
            Path path,
            String keyHeader,
            String valueHeader,
            Map<String, String> values) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(keyHeader, valueHeader));
        values.forEach((key, value) -> rows.add(Arrays.asList(key, value)));
        writeRows(path, rows);
    }

    void writeRows(Path path, List<List<String>> rows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(UTF8_BOM);
            for (List<String> row : rows) {
                for (int index = 0; index < row.size(); index++) {
                    if (index > 0) {
                        writer.write(',');
                    }
                    writer.write(escape(row.get(index)));
                }
                writer.newLine();
            }
        }
    }

    static String escape(String value) {
        String safeValue = value == null ? "" : value;
        boolean quote = safeValue.contains(",")
                || safeValue.contains("\"")
                || safeValue.contains("\n")
                || safeValue.contains("\r");
        if (!quote) {
            return safeValue;
        }
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    private String decimal(double value) {
        return String.format(Locale.ROOT, "%.6f", value);
    }

    private String nullableLong(Long value) {
        return value == null ? "" : String.valueOf(value);
    }
}
