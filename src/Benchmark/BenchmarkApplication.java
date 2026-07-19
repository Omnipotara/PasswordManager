package Benchmark;

import Benchmark.Encryption.EncryptionBenchmarkRunner;
import Benchmark.Hashing.HashingBenchmarkRunner;
import Benchmark.Scoring.EvaluationCriteriaCatalog;
import Benchmark.Scoring.ScoringModel;
import Benchmark.Scoring.ScoringReport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Entry point for the complete Phase 9 benchmark and scoring workflow. */
public final class BenchmarkApplication {

    private static final DateTimeFormatter RUN_ID_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss'Z'");

    private BenchmarkApplication() {
    }

    public static void main(String[] args) throws Exception {
        if (containsHelp(args)) {
            printHelp();
            return;
        }

        BenchmarkConfig config = BenchmarkConfig.fromArgs(args);
        String runId = ZonedDateTime.now(ZoneOffset.UTC).format(RUN_ID_FORMAT);
        Path outputDirectory = config.getOutputDirectory().toAbsolutePath().normalize();
        Files.createDirectories(outputDirectory);

        System.out.println("Pokrecem hashing benchmark...");
        List<BenchmarkResult> rawResults = new ArrayList<>();
        rawResults.addAll(new HashingBenchmarkRunner().run(runId, config));

        System.out.println("Pokrecem encryption benchmark...");
        rawResults.addAll(new EncryptionBenchmarkRunner().run(runId, config));
        sortRawResults(rawResults);

        List<BenchmarkSummary> summaries = BenchmarkAnalyzer.summarize(rawResults);
        EvaluationCriteriaCatalog criteriaCatalog = new EvaluationCriteriaCatalog();
        ScoringReport scoringReport = new ScoringModel(criteriaCatalog).evaluate(summaries);
        Map<String, String> environment = BenchmarkEnvironment.collect(runId, config);

        new CsvBenchmarkWriter().writeAll(
                outputDirectory,
                rawResults,
                summaries,
                environment,
                criteriaCatalog.getAll(),
                scoringReport
        );
        new BenchmarkReportWriter().write(
                outputDirectory.resolve("benchmark-report.txt"),
                environment,
                summaries,
                scoringReport
        );

        System.out.println("Benchmark je uspesno zavrsen.");
        System.out.println("Rezultati: " + outputDirectory);
        scoringReport.getAlgorithmSummaries().forEach(summary -> System.out.printf(
                java.util.Locale.ROOT,
                "  %s: prosek %.2f%n",
                summary.getAlgorithm().getDatabaseValue(),
                summary.getOverallAverage()
        ));
    }

    private static void sortRawResults(List<BenchmarkResult> results) {
        results.sort(Comparator
                .comparing(BenchmarkResult::getGroup)
                .thenComparing(BenchmarkResult::getAlgorithm)
                .thenComparing(BenchmarkResult::getOperation)
                .thenComparingInt(BenchmarkResult::getInputSizeBytes)
                .thenComparingInt(BenchmarkResult::getSampleNumber));
    }

    private static boolean containsHelp(String[] args) {
        for (String argument : args) {
            if (argument.equals("--help") || argument.equals("-h")) {
                return true;
            }
        }
        return false;
    }

    private static void printHelp() {
        System.out.println("Password Manager benchmark");
        System.out.println("Ant: ant benchmark");
        System.out.println("Quick smoke test: ant benchmark -Dbenchmark.args=--quick");
        System.out.println("Opcije:");
        System.out.println("  --quick");
        System.out.println("  --output=<folder>");
        System.out.println("  --hash-warmups=<broj>");
        System.out.println("  --hash-iterations=<broj>");
        System.out.println("  --encryption-warmups=<broj>");
        System.out.println("  --encryption-samples=<broj>");
        System.out.println("  --encryption-operations=<broj>");
        System.out.println("  --input-sizes=<bajti,bajti,...>");
    }
}
