package Cryptography.Hashing;

import Cryptography.AlgorithmName;

/**
 * Common contract for algorithms that protect the user's master password.
 */
public interface HashingStrategy {

    String hash(String password);

    boolean verify(String password, String storedHash);

    AlgorithmName getAlgorithmName();
}
