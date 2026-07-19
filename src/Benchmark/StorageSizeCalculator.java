package Benchmark;

import Cryptography.Model.EncryptedData;
import java.nio.charset.StandardCharsets;

/** Calculates the actual UTF-8 size of values persisted in text columns. */
public final class StorageSizeCalculator {

    private StorageSizeCalculator() {
    }

    public static int ciphertextBytes(EncryptedData data) {
        return utf8Bytes(data.getCiphertextBase64());
    }

    public static int metadataBytes(EncryptedData data) {
        return utf8Bytes(data.getAlgorithmName().getDatabaseValue())
                + utf8Bytes(data.getIvBase64())
                + utf8Bytes(data.getAuthenticationTagBase64())
                + utf8Bytes(data.getSaltBase64())
                + utf8Bytes(data.getParameters());
    }

    private static int utf8Bytes(String value) {
        return value == null ? 0 : value.getBytes(StandardCharsets.UTF_8).length;
    }
}
