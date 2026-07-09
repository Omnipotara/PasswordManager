package Cryptography;

import Cryptography.Encryption.AESGCMStrategy;
import Cryptography.KeyDerivation.KeyDerivationService;
import Cryptography.Model.EncryptedData;
import javax.crypto.*;

public class CryptoUtils {

    private static final KeyDerivationService KEY_DERIVATION_SERVICE = new KeyDerivationService();
    private static final AESGCMStrategy DEFAULT_ENCRYPTION_STRATEGY = new AESGCMStrategy();

    public static SecretKey deriveKey(String masterPassword, byte[] salt) throws Exception {
        return KEY_DERIVATION_SERVICE.deriveEncryptionKey(masterPassword, salt);
    }

    public static String encrypt(String plaintext, SecretKey key) throws Exception {
        EncryptedData encryptedData = DEFAULT_ENCRYPTION_STRATEGY.encrypt(plaintext, key);
        return DEFAULT_ENCRYPTION_STRATEGY.toLegacyCombinedBase64(encryptedData);
    }

    public static String decrypt(String ciphertext, SecretKey key) throws Exception {
        EncryptedData encryptedData = DEFAULT_ENCRYPTION_STRATEGY.fromLegacyCombinedBase64(ciphertext);
        return DEFAULT_ENCRYPTION_STRATEGY.decrypt(encryptedData, key);
    }
}
