package Benchmark;

import java.nio.file.Paths;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BenchmarkConfigTest {

    @Test
    public void quickProfileShouldUseSmallSmokeTestCounts() {
        BenchmarkConfig config = BenchmarkConfig.fromArgs(new String[]{"--quick"});

        assertEquals(1, config.getHashWarmupIterations());
        assertEquals(1, config.getHashMeasuredIterations());
        assertEquals(50, config.getEncryptionWarmupOperations());
        assertEquals(2, config.getEncryptionMeasuredSamples());
        assertEquals(100, config.getEncryptionOperationsPerSample());
    }

    @Test
    public void explicitOptionsShouldOverrideProfileAndSortInputSizes() {
        BenchmarkConfig config = BenchmarkConfig.fromArgs(new String[]{
            "--quick",
            "--output=custom-results",
            "--hash-iterations=3",
            "--encryption-operations=50",
            "--input-sizes=256,16,64"
        });

        assertEquals(Paths.get("custom-results"), config.getOutputDirectory());
        assertEquals(3, config.getHashMeasuredIterations());
        assertEquals(50, config.getEncryptionOperationsPerSample());
        assertArrayEquals(new int[]{16, 64, 256}, config.getEncryptionInputSizes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectUnknownArgument() {
        BenchmarkConfig.fromArgs(new String[]{"--unknown=true"});
    }
}
