package Benchmark;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CsvBenchmarkWriterTest {

    @Test
    public void shouldLeaveSimpleValuesUnquoted() {
        assertEquals("AES_GCM", CsvBenchmarkWriter.escape("AES_GCM"));
    }

    @Test
    public void shouldQuoteCommasAndEscapeQuotes() {
        assertEquals("\"cost=10, note=\"\"test\"\"\"", CsvBenchmarkWriter.escape("cost=10, note=\"test\""));
    }

    @Test
    public void shouldWriteUtf8BomForExcelCompatibility() throws Exception {
        Path output = Files.createTempFile("benchmark-csv-", ".csv");
        try {
            new CsvBenchmarkWriter().writeRows(
                    output,
                    Arrays.asList(Arrays.asList("kriterijum", "čćšžđ"))
            );

            byte[] bytes = Files.readAllBytes(output);
            assertArrayEquals(
                    new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                    Arrays.copyOf(bytes, 3)
            );
        } finally {
            Files.deleteIfExists(output);
        }
    }
}
