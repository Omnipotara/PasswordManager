package Benchmark;

import Benchmark.Scoring.AlgorithmScoreSummary;
import Benchmark.Scoring.CategoryScore;
import Benchmark.Scoring.EvaluationCriteriaCatalog;
import Benchmark.Scoring.ScoringModel;
import Benchmark.Scoring.ScoringReport;
import Cryptography.AlgorithmName;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ScoringModelTest {

    @Test
    public void shouldScoreEveryCriterionAndCalculateUnweightedCategoryAverages() {
        List<BenchmarkSummary> summaries = BenchmarkAnalyzer.summarize(completeRawResults());
        EvaluationCriteriaCatalog catalog = new EvaluationCriteriaCatalog();

        ScoringReport report = new ScoringModel(catalog).evaluate(summaries);

        assertEquals(96, report.getCriterionScores().size());
        assertEquals(24, report.getCategoryScores().size());
        assertEquals(6, report.getAlgorithmSummaries().size());
        for (CategoryScore category : report.getCategoryScores()) {
            assertEquals(4, category.getCriterionCount());
            assertTrue(category.getAverageScore() >= 1.0);
            assertTrue(category.getAverageScore() <= 5.0);
        }
        for (AlgorithmScoreSummary algorithm : report.getAlgorithmSummaries()) {
            double expected = (algorithm.getPerformanceScore()
                    + algorithm.getSecurityScore()
                    + algorithm.getImplementationPracticalityScore()
                    + algorithm.getPasswordManagerSuitabilityScore()) / 4.0;
            assertEquals(expected, algorithm.getOverallAverage(), 0.000001);
        }
    }

    private List<BenchmarkResult> completeRawResults() {
        List<BenchmarkResult> results = new ArrayList<>();
        long duration = 1_000_000;
        for (AlgorithmName algorithm : AlgorithmName.values()) {
            if (algorithm.isHashingAlgorithm()) {
                results.add(result(algorithm, BenchmarkGroup.HASHING, BenchmarkOperation.HASH, 24, duration++, 60, 0));
                results.add(result(algorithm, BenchmarkGroup.HASHING, BenchmarkOperation.VERIFY, 24, duration++, 0, 0));
            } else {
                for (int inputSize : new int[]{16, 1024}) {
                    results.add(result(algorithm, BenchmarkGroup.ENCRYPTION, BenchmarkOperation.ENCRYPT,
                            inputSize, duration++, inputSize + 8, 64));
                    results.add(result(algorithm, BenchmarkGroup.ENCRYPTION, BenchmarkOperation.DECRYPT,
                            inputSize, duration++, inputSize + 8, 64));
                }
            }
        }
        return results;
    }

    private BenchmarkResult result(
            AlgorithmName algorithm,
            BenchmarkGroup group,
            BenchmarkOperation operation,
            int inputSize,
            long duration,
            int outputSize,
            int metadataSize) {
        return new BenchmarkResult(
                "test-run",
                group,
                algorithm,
                operation,
                "test=true",
                inputSize,
                1,
                1,
                duration,
                group == BenchmarkGroup.HASHING ? 1024L : null,
                outputSize,
                metadataSize,
                true
        );
    }
}
