package Cryptography.Encryption;

import Cryptography.AlgorithmName;
import Cryptography.Exceptions.CryptoOperationException;
import Cryptography.Model.EncryptedData;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCBCStrategy implements EncryptionStrategy {

    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int IV_LENGTH_BYTES = 16;
    private static final String PARAMETERS = "cipher=AES/CBC/PKCS5Padding;mac=HmacSHA256;mode=encrypt-then-mac";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public EncryptedData encrypt(String plaintext, SecretKey key) {
        try {
            DerivedKeys derivedKeys = deriveKeys(key);
            byte[] iv = generateIv();

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, derivedKeys.encryptionKey, new IvParameterSpec(iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] authenticationTag = calculateHmac(derivedKeys.macKey, iv, ciphertext);

            return new EncryptedData(
                    getAlgorithmName(),
                    Base64.getEncoder().encodeToString(ciphertext),
                    Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(authenticationTag),
                    null,
                    PARAMETERS
            );
        } catch (Exception ex) {
            throw new CryptoOperationException("AES-CBC-HMAC encryption failed.", ex);
        }
    }

    @Override
    public String decrypt(EncryptedData encryptedData, SecretKey key) {
        try {
            DerivedKeys derivedKeys = deriveKeys(key);
            byte[] iv = Base64.getDecoder().decode(encryptedData.getIvBase64());
            byte[] ciphertext = Base64.getDecoder().decode(encryptedData.getCiphertextBase64());
            byte[] authenticationTag = Base64.getDecoder().decode(encryptedData.getAuthenticationTagBase64());
            byte[] expectedTag = calculateHmac(derivedKeys.macKey, iv, ciphertext);

            if (!MessageDigest.isEqual(authenticationTag, expectedTag)) {
                throw new CryptoOperationException("AES-CBC-HMAC integrity check failed.");
            }

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, derivedKeys.encryptionKey, new IvParameterSpec(iv));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (CryptoOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CryptoOperationException("AES-CBC-HMAC decryption failed.", ex);
        }
    }

    @Override
    public AlgorithmName getAlgorithmName() {
        return AlgorithmName.AES_CBC_HMAC;
    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }

    private byte[] calculateHmac(SecretKey macKey, byte[] iv, byte[] ciphertext) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(macKey);
        mac.update(iv);
        mac.update(ciphertext);
        return mac.doFinal();
    }

    private DerivedKeys deriveKeys(SecretKey key) throws Exception {
        byte[] keyBytes = key.getEncoded();
        if (keyBytes == null || keyBytes.length == 0) {
            throw new CryptoOperationException("Encoded key bytes are required for AES-CBC-HMAC.");
        }

        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec baseKey = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);

        mac.init(baseKey);
        byte[] encryptionKeyBytes = mac.doFinal("AES-CBC encryption key".getBytes(StandardCharsets.UTF_8));

        mac.init(baseKey);
        byte[] macKeyBytes = mac.doFinal("AES-CBC authentication key".getBytes(StandardCharsets.UTF_8));

        return new DerivedKeys(
                new SecretKeySpec(encryptionKeyBytes, "AES"),
                new SecretKeySpec(macKeyBytes, HMAC_ALGORITHM)
        );
    }

    private static class DerivedKeys {

        private final SecretKey encryptionKey;
        private final SecretKey macKey;

        private DerivedKeys(SecretKey encryptionKey, SecretKey macKey) {
            this.encryptionKey = encryptionKey;
            this.macKey = macKey;
        }
    }
}
