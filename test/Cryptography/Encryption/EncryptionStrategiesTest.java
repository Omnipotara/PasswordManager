package Cryptography.Encryption;

import Cryptography.Exceptions.CryptoOperationException;
import Cryptography.KeyDerivation.KeyDerivationService;
import Cryptography.Model.EncryptedData;
import java.util.Base64;
import javax.crypto.SecretKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

public class EncryptionStrategiesTest {

    private static final String PLAINTEXT = "VerySecretEntryPassword123!";
    private static final byte[] SALT = "1234567890123456".getBytes();
    private final SecretKey key = new KeyDerivationService().deriveEncryptionKey("MasterPassword123!", SALT);
    private final SecretKey wrongKey = new KeyDerivationService().deriveEncryptionKey("WrongMasterPassword123!", SALT);

    @Test
    public void aesGcmShouldEncryptDecryptAndRejectTamperedCiphertext() {
        assertEncryptionStrategyWorks(new AESGCMStrategy());
    }

    @Test
    public void aesCbcHmacShouldEncryptDecryptAndRejectTamperedCiphertext() {
        assertEncryptionStrategyWorks(new AESCBCStrategy());
    }

    @Test
    public void chaCha20Poly1305ShouldEncryptDecryptAndRejectTamperedCiphertext() {
        assertEncryptionStrategyWorks(new ChaCha20Poly1305Strategy());
    }

    private void assertEncryptionStrategyWorks(EncryptionStrategy strategy) {
        EncryptedData encryptedData = strategy.encrypt(PLAINTEXT, key);

        assertNotEquals(PLAINTEXT, encryptedData.getCiphertextBase64());
        assertEquals(PLAINTEXT, strategy.decrypt(encryptedData, key));
        assertDecryptFails(strategy, encryptedData, wrongKey);

        EncryptedData tamperedData = copyOf(encryptedData);
        tamperedData.setCiphertextBase64(tamperBase64(tamperedData.getCiphertextBase64()));
        assertDecryptFails(strategy, tamperedData, key);

        EncryptedData tamperedTag = copyOf(encryptedData);
        tamperedTag.setAuthenticationTagBase64(tamperBase64(tamperedTag.getAuthenticationTagBase64()));
        assertDecryptFails(strategy, tamperedTag, key);

        EncryptedData tamperedIv = copyOf(encryptedData);
        tamperedIv.setIvBase64(tamperBase64(tamperedIv.getIvBase64()));
        assertDecryptFails(strategy, tamperedIv, key);
    }

    private void assertDecryptFails(EncryptionStrategy strategy, EncryptedData encryptedData, SecretKey decryptionKey) {
        try {
            strategy.decrypt(encryptedData, decryptionKey);
            org.junit.Assert.fail("Tampered encrypted data should not decrypt.");
        } catch (CryptoOperationException expected) {
            // Expected security failure.
        }
    }

    private EncryptedData copyOf(EncryptedData encryptedData) {
        return new EncryptedData(
                encryptedData.getAlgorithmName(),
                encryptedData.getCiphertextBase64(),
                encryptedData.getIvBase64(),
                encryptedData.getAuthenticationTagBase64(),
                encryptedData.getSaltBase64(),
                encryptedData.getParameters());
    }

    private String tamperBase64(String value) {
        byte[] decoded = Base64.getDecoder().decode(value);
        decoded[0] = (byte) (decoded[0] ^ 1);
        return Base64.getEncoder().encodeToString(decoded);
    }
}
