package Cryptography;

import Cryptography.KeyDerivation.KeyDerivationService;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

public class CryptoUtils {

    private static final KeyDerivationService KEY_DERIVATION_SERVICE = new KeyDerivationService();

    public static SecretKey deriveKey(String masterPassword, byte[] salt) throws Exception {
        return KEY_DERIVATION_SERVICE.deriveEncryptionKey(masterPassword, salt);
    }

    public static String encrypt(String plaintext, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String ciphertext, SecretKey key) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        byte[] iv = new byte[12];
        System.arraycopy(decoded, 0, iv, 0, 12);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] decrypted = cipher.doFinal(decoded, 12, decoded.length - 12);
        return new String(decrypted);
    }
}
