package Benchmark;

import Cryptography.AlgorithmName;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BenchmarkSummaryTest {

    @Test
    public void shouldCalculateMedianPercentileAverageAndDeviation() {
        List<BenchmarkResult> results = new ArrayList<>();
        for (int sample = 1; sample <= 5; sample++) {
            results.add(result(sample, sample * 1_000_000L));
        }

        BenchmarkSummary summary = BenchmarkSummary.from(results);

        assertEquals(1.0, summary.getMinimumMilliseconds(), 0.000001);
        assertEquals(3.0, summary.getMedianMilliseconds(), 0.000001);
        assertEquals(3.0, summary.getAverageMilliseconds(), 0.000001);
        assertEquals(5.0, summary.getPercentile95Milliseconds(), 0.000001);
        assertEquals(5.0, summary.getMaximumMilliseconds(), 0.000001);
        assertEquals(Math.sqrt(2), summary.getStandardDeviationMilliseconds(), 0.000001);
        assertEquals(5, summary.getSampleCount());
        assertEquals(5, summary.getTotalOperations());
    }

    @Test
    public void shouldNormalizeBatchDurationToOneOperation() {
        BenchmarkResult result = new BenchmarkResult(
                "test-run",
                BenchmarkGroup.ENCRYPTION,
                AlgorithmName.AES_GCM,
                BenchmarkOperation.ENCRYPT,
                "test=true",
                16,
                1,
                100,
                50_000_000,
                null,
                24,
                64,
                true
        );

        assertEquals(500_000.0, result.getAverageDurationNanoseconds(), 0.000001);
        assertEquals(0.5, result.getAverageDurationMilliseconds(), 0.000001);
    }

    private BenchmarkResult result(int sample, long durationNanoseconds) {
        return new BenchmarkResult(
                "test-run",
                BenchmarkGroup.HASHING,
                AlgorithmName.BCRYPT,
                BenchmarkOperation.HASH,
                "cost=10",
                20,
                sample,
                1,
                durationNanoseconds,
                1024L,
                60,
                0,
                true
        );
    }
}
