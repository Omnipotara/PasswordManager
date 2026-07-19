package Benchmark;

import Cryptography.AlgorithmName;
import Cryptography.Model.EncryptedData;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StorageSizeCalculatorTest {

    @Test
    public void shouldCountPersistedCiphertextAndMetadataStrings() {
        EncryptedData data = new EncryptedData(
                AlgorithmName.AES_GCM,
                "Y2lwaGVydGV4dA==",
                "aXY=",
                "dGFn",
                null,
                "tagLengthBits=128"
        );

        assertEquals(16, StorageSizeCalculator.ciphertextBytes(data));
        assertEquals(7 + 4 + 4 + 17, StorageSizeCalculator.metadataBytes(data));
    }
}
