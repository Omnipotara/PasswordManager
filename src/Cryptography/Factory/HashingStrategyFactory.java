package Cryptography.Factory;

import Cryptography.AlgorithmName;
import Cryptography.Exceptions.UnsupportedAlgorithmException;
import Cryptography.Hashing.Argon2idStrategy;
import Cryptography.Hashing.BCryptStrategy;
import Cryptography.Hashing.HashingStrategy;
import Cryptography.Hashing.PBKDF2HashingStrategy;

/**
 * Resolves hashing strategies from algorithm identifiers.
 */
public class HashingStrategyFactory {

    private HashingStrategyFactory() {
    }

    public static HashingStrategy getStrategy(AlgorithmName algorithmName) {
        if (algorithmName == null) {
            throw new UnsupportedAlgorithmException("null");
        }

        switch (algorithmName) {
            case BCRYPT:
                return new BCryptStrategy();
            case PBKDF2:
                return new PBKDF2HashingStrategy();
            case ARGON2ID:
                return new Argon2idStrategy();
            default:
                throw new UnsupportedAlgorithmException(algorithmName.getDatabaseValue());
        }
    }

    public static HashingStrategy getStrategy(String databaseValue) {
        return getStrategy(AlgorithmName.fromDatabaseValue(databaseValue));
    }
}
