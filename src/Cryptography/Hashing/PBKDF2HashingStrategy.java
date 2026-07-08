package Cryptography.Hashing;

import Cryptography.AlgorithmName;
import Cryptography.Exceptions.CryptoOperationException;
import com.password4j.Hash;
import com.password4j.PBKDF2Function;
import com.password4j.types.Hmac;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * PBKDF2 implementation used for protecting the user's master password.
 */
public class PBKDF2HashingStrategy implements HashingStrategy {

    private static final String FORMAT_PREFIX = "PBKDF2";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 200000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final String SEPARATOR = "\\$";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String hash(String password) {
        byte[] salt = generateSalt();
        byte[] hash = deriveHash(password, salt, ITERATIONS, KEY_LENGTH_BITS);

        return FORMAT_PREFIX
                + "$" + ALGORITHM
                + "$" + ITERATIONS
                + "$" + KEY_LENGTH_BITS
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean verify(String password, String storedHash) {
        ParsedPBKDF2Hash parsedHash = parse(storedHash);
        if (parsedHash == null) {
            return false;
        }

        byte[] calculatedHash = deriveHash(
                password,
                parsedHash.salt,
                parsedHash.iterations,
                parsedHash.keyLengthBits
        );

        return MessageDigest.isEqual(calculatedHash, parsedHash.hash);
    }

    @Override
    public AlgorithmName getAlgorithmName() {
        return AlgorithmName.PBKDF2;
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    private byte[] deriveHash(String password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBKDF2Function pbkdf2Function = PBKDF2Function.getInstance(Hmac.SHA256, iterations, keyLengthBits);
            Hash hash = pbkdf2Function.hash(password.getBytes(StandardCharsets.UTF_8), salt);
            return hash.getResultAsBytes();
        } catch (Exception ex) {
            throw new CryptoOperationException("PBKDF2 password hashing failed.", ex);
        }
    }

    private ParsedPBKDF2Hash parse(String storedHash) {
        if (storedHash == null) {
            return null;
        }

        String[] parts = storedHash.split(SEPARATOR);
        if (parts.length != 6 || !FORMAT_PREFIX.equals(parts[0]) || !ALGORITHM.equals(parts[1])) {
            return null;
        }

        try {
            int iterations = Integer.parseInt(parts[2]);
            int keyLengthBits = Integer.parseInt(parts[3]);
            byte[] salt = Base64.getDecoder().decode(parts[4]);
            byte[] hash = Base64.getDecoder().decode(parts[5]);
            if (iterations <= 0 || keyLengthBits <= 0 || salt.length == 0 || hash.length == 0) {
                return null;
            }

            return new ParsedPBKDF2Hash(iterations, keyLengthBits, salt, hash);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static class ParsedPBKDF2Hash {

        private final int iterations;
        private final int keyLengthBits;
        private final byte[] salt;
        private final byte[] hash;

        private ParsedPBKDF2Hash(int iterations, int keyLengthBits, byte[] salt, byte[] hash) {
            this.iterations = iterations;
            this.keyLengthBits = keyLengthBits;
            this.salt = salt;
            this.hash = hash;
        }
    }
}
