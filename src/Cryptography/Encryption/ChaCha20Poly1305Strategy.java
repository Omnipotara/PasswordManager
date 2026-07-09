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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ChaCha20Poly1305Strategy implements EncryptionStrategy {

    private static final String TRANSFORMATION = "ChaCha20-Poly1305";
    private static final String KEY_ALGORITHM = "ChaCha20";
    private static final int NONCE_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BYTES = 16;
    private static final String PARAMETERS = "tagLengthBits=128";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public EncryptedData encrypt(String plaintext, SecretKey key) {
        try {
            byte[] nonce = generateNonce();
            SecretKey chachaKey = toChaChaKey(key);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, chachaKey, new IvParameterSpec(nonce));

            byte[] ciphertextWithTag = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            int ciphertextLength = ciphertextWithTag.length - TAG_LENGTH_BYTES;
            byte[] ciphertext = Arrays.copyOfRange(ciphertextWithTag, 0, ciphertextLength);
            byte[] authenticationTag = Arrays.copyOfRange(ciphertextWithTag, ciphertextLength, ciphertextWithTag.length);

            return new EncryptedData(
                    getAlgorithmName(),
                    Base64.getEncoder().encodeToString(ciphertext),
                    Base64.getEncoder().encodeToString(nonce),
                    Base64.getEncoder().encodeToString(authenticationTag),
                    null,
                    PARAMETERS
            );
        } catch (Exception ex) {
            throw new CryptoOperationException("ChaCha20-Poly1305 encryption failed.", ex);
        }
    }

    @Override
    public String decrypt(EncryptedData encryptedData, SecretKey key) {
        try {
            byte[] nonce = Base64.getDecoder().decode(encryptedData.getIvBase64());
            byte[] ciphertext = Base64.getDecoder().decode(encryptedData.getCiphertextBase64());
            byte[] authenticationTag = Base64.getDecoder().decode(encryptedData.getAuthenticationTagBase64());
            byte[] ciphertextWithTag = combine(ciphertext, authenticationTag);
            SecretKey chachaKey = toChaChaKey(key);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, chachaKey, new IvParameterSpec(nonce));
            byte[] plaintext = cipher.doFinal(ciphertextWithTag);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new CryptoOperationException("ChaCha20-Poly1305 decryption failed.", ex);
        }
    }

    @Override
    public AlgorithmName getAlgorithmName() {
        return AlgorithmName.CHACHA20_POLY1305;
    }

    private byte[] generateNonce() {
        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(nonce);
        return nonce;
    }

    private SecretKey toChaChaKey(SecretKey key) {
        byte[] keyBytes = key.getEncoded();
        if (keyBytes == null || keyBytes.length != 32) {
            throw new CryptoOperationException("ChaCha20-Poly1305 requires a 256-bit key.");
        }
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    private byte[] combine(byte[] first, byte[] second) {
        byte[] combined = new byte[first.length + second.length];
        System.arraycopy(first, 0, combined, 0, first.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }
}
