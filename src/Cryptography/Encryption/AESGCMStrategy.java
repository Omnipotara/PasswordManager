package Cryptography.Encryption;

import Cryptography.AlgorithmName;
import Cryptography.Exceptions.CryptoOperationException;
import Cryptography.Model.EncryptedData;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class AESGCMStrategy implements EncryptionStrategy {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int TAG_LENGTH_BYTES = TAG_LENGTH_BITS / 8;
    private static final String PARAMETERS = "tagLengthBits=" + TAG_LENGTH_BITS;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public EncryptedData encrypt(String plaintext, SecretKey key) {
        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            byte[] cipherTextWithTag = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            int ciphertextLength = cipherTextWithTag.length - TAG_LENGTH_BYTES;
            byte[] ciphertext = Arrays.copyOfRange(cipherTextWithTag, 0, ciphertextLength);
            byte[] authenticationTag = Arrays.copyOfRange(cipherTextWithTag, ciphertextLength, cipherTextWithTag.length);

            return new EncryptedData(
                    getAlgorithmName(),
                    Base64.getEncoder().encodeToString(ciphertext),
                    Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(authenticationTag),
                    null,
                    PARAMETERS
            );
        } catch (Exception ex) {
            throw new CryptoOperationException("AES-GCM encryption failed.", ex);
        }
    }

    @Override
    public String decrypt(EncryptedData encryptedData, SecretKey key) {
        try {
            byte[] iv = Base64.getDecoder().decode(encryptedData.getIvBase64());
            byte[] ciphertext = Base64.getDecoder().decode(encryptedData.getCiphertextBase64());
            byte[] authenticationTag = Base64.getDecoder().decode(encryptedData.getAuthenticationTagBase64());
            byte[] cipherTextWithTag = combine(ciphertext, authenticationTag);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(cipherTextWithTag);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new CryptoOperationException("AES-GCM decryption failed.", ex);
        }
    }

    @Override
    public AlgorithmName getAlgorithmName() {
        return AlgorithmName.AES_GCM;
    }

    public String toLegacyCombinedBase64(EncryptedData encryptedData) {
        byte[] iv = Base64.getDecoder().decode(encryptedData.getIvBase64());
        byte[] ciphertext = Base64.getDecoder().decode(encryptedData.getCiphertextBase64());
        byte[] authenticationTag = Base64.getDecoder().decode(encryptedData.getAuthenticationTagBase64());
        return Base64.getEncoder().encodeToString(combine(iv, combine(ciphertext, authenticationTag)));
    }

    public EncryptedData fromLegacyCombinedBase64(String legacyCiphertext) {
        try {
            byte[] decoded = Base64.getDecoder().decode(legacyCiphertext);
            if (decoded.length <= IV_LENGTH_BYTES + TAG_LENGTH_BYTES) {
                throw new CryptoOperationException("Legacy AES-GCM payload is too short.");
            }

            byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH_BYTES);
            byte[] cipherTextWithTag = Arrays.copyOfRange(decoded, IV_LENGTH_BYTES, decoded.length);
            int ciphertextLength = cipherTextWithTag.length - TAG_LENGTH_BYTES;
            byte[] ciphertext = Arrays.copyOfRange(cipherTextWithTag, 0, ciphertextLength);
            byte[] authenticationTag = Arrays.copyOfRange(cipherTextWithTag, ciphertextLength, cipherTextWithTag.length);

            return new EncryptedData(
                    getAlgorithmName(),
                    Base64.getEncoder().encodeToString(ciphertext),
                    Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(authenticationTag),
                    null,
                    PARAMETERS
            );
        } catch (IllegalArgumentException ex) {
            throw new CryptoOperationException("Legacy AES-GCM payload is not valid Base64.", ex);
        }
    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }

    private byte[] combine(byte[] first, byte[] second) {
        byte[] combined = new byte[first.length + second.length];
        System.arraycopy(first, 0, combined, 0, first.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }
}
