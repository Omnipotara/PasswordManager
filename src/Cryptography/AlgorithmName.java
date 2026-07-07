package Cryptography;

import Cryptography.Exceptions.UnsupportedAlgorithmException;

/**
 * Centralized algorithm identifiers persisted in the database and reused by UI,
 * strategy factories, benchmarks, and documentation.
 */
public enum AlgorithmName {
    BCRYPT("BCRYPT", AlgorithmType.HASHING),
    PBKDF2("PBKDF2", AlgorithmType.HASHING),
    ARGON2ID("ARGON2ID", AlgorithmType.HASHING),
    
    AES_GCM("AES_GCM", AlgorithmType.ENCRYPTION),
    AES_CBC_HMAC("AES_CBC_HMAC", AlgorithmType.ENCRYPTION),
    CHACHA20_POLY1305("CHACHA20_POLY1305", AlgorithmType.ENCRYPTION);

    private final String databaseValue;
    private final AlgorithmType type;

    AlgorithmName(String databaseValue, AlgorithmType type) {
        this.databaseValue = databaseValue;
        this.type = type;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }

    public AlgorithmType getType() {
        return type;
    }

    public boolean isHashingAlgorithm() {
        return type == AlgorithmType.HASHING;
    }

    public boolean isEncryptionAlgorithm() {
        return type == AlgorithmType.ENCRYPTION;
    }

    public static AlgorithmName fromDatabaseValue(String value) {
        if (value == null) {
            throw new UnsupportedAlgorithmException("null");
        }

        for (AlgorithmName algorithmName : values()) {
            if (algorithmName.databaseValue.equalsIgnoreCase(value.trim())) {
                return algorithmName;
            }
        }

        throw new UnsupportedAlgorithmException(value);
    }

    public enum AlgorithmType {
        HASHING,
        ENCRYPTION
    }
}
