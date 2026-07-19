package Cryptography.Hashing;

import Cryptography.AlgorithmName;
import Cryptography.Exceptions.CryptoOperationException;
import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.types.Argon2;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Argon2id implementation used for protecting the user's master password.
 */
public class Argon2idStrategy implements HashingStrategy {

    private static final int MEMORY_KB = 65536;
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 1;
    private static final int HASH_LENGTH_BYTES = 32;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Argon2Function ARGON2ID_FUNCTION = Argon2Function.getInstance(
            MEMORY_KB,
            ITERATIONS,
            PARALLELISM,
            HASH_LENGTH_BYTES,
            Argon2.ID
    );

    @Override
    public String hash(String password) {
        try {
            Hash hash = ARGON2ID_FUNCTION.hash(password.getBytes(StandardCharsets.UTF_8), generateSalt());
            return hash.getResult();
        } catch (Exception ex) {
            throw new CryptoOperationException("Argon2id password hashing failed.", ex);
        }
    }

    @Override
    public boolean verify(String password, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        try {
            return Argon2Function.getInstanceFromHash(storedHash).check(password, storedHash);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public AlgorithmName getAlgorithmName() {
        return AlgorithmName.ARGON2ID;
    }

    @Override
    public String getParametersDescription() {
        return "variant=Argon2id;memoryKiB=" + MEMORY_KB
                + ";iterations=" + ITERATIONS
                + ";parallelism=" + PARALLELISM
                + ";hashBytes=" + HASH_LENGTH_BYTES
                + ";saltBytes=" + SALT_LENGTH_BYTES;
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }
}
