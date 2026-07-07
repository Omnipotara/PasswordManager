package Cryptography.Hashing;

import BCrypt.src.org.mindrot.jbcrypt.BCrypt;
import Cryptography.AlgorithmName;

/**
 * BCrypt implementation used for protecting the user's master password.
 */
public class BCryptStrategy implements HashingStrategy {

    private static final int LOG_ROUNDS = 10;

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
    }

    @Override
    public boolean verify(String password, String storedHash) {
        return BCrypt.checkpw(password, storedHash);
    }

    @Override
    public AlgorithmName getAlgorithmName() {
        return AlgorithmName.BCRYPT;
    }
}
