package Cryptography;

import Cryptography.Hashing.BCryptStrategy;
import Cryptography.Hashing.HashingStrategy;

/**
 *
 * @author Omnix
 */
public class HashUtils {

    private static final HashingStrategy DEFAULT_HASHING_STRATEGY = new BCryptStrategy();

    public static String hashPassword(String password) {
        return DEFAULT_HASHING_STRATEGY.hash(password);
    }

    public static boolean checkPassword(String password, String hash) {
        return DEFAULT_HASHING_STRATEGY.verify(password, hash);
    }
}
