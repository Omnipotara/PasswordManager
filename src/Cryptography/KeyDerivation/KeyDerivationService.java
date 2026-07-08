package Cryptography.KeyDerivation;

import Cryptography.Exceptions.CryptoOperationException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyDerivationService {

    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String KEY_ALGORITHM = "AES";
    private static final int ITERATIONS = 200000;
    private static final int KEY_LENGTH_BITS = 256;

    public SecretKey deriveEncryptionKey(String masterPassword, byte[] salt) {
        if (masterPassword == null || salt == null || salt.length == 0) {
            throw new CryptoOperationException("Master password and salt are required for key derivation.");
        }

        char[] passwordChars = masterPassword.toCharArray();
        try {
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        } catch (Exception ex) {
            throw new CryptoOperationException("Encryption key derivation failed.", ex);
        } finally {
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }

    public String getAlgorithmName() {
        return KDF_ALGORITHM;
    }

    public int getIterations() {
        return ITERATIONS;
    }

    public int getKeyLengthBits() {
        return KEY_LENGTH_BITS;
    }
}
