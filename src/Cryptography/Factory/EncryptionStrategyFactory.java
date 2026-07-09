package Cryptography.Factory;

import Cryptography.AlgorithmName;
import Cryptography.Encryption.AESCBCStrategy;
import Cryptography.Encryption.AESGCMStrategy;
import Cryptography.Encryption.ChaCha20Poly1305Strategy;
import Cryptography.Encryption.EncryptionStrategy;
import Cryptography.Exceptions.UnsupportedAlgorithmException;

/**
 * Resolves encryption strategies from algorithm identifiers.
 */
public class EncryptionStrategyFactory {

    private EncryptionStrategyFactory() {
    }

    public static EncryptionStrategy getStrategy(AlgorithmName algorithmName) {
        if (algorithmName == null) {
            throw new UnsupportedAlgorithmException("null");
        }

        switch (algorithmName) {
            case AES_GCM:
                return new AESGCMStrategy();
            case AES_CBC_HMAC:
                return new AESCBCStrategy();
            case CHACHA20_POLY1305:
                return new ChaCha20Poly1305Strategy();
            default:
                throw new UnsupportedAlgorithmException(algorithmName.getDatabaseValue());
        }
    }

    public static EncryptionStrategy getStrategy(String databaseValue) {
        return getStrategy(AlgorithmName.fromDatabaseValue(databaseValue));
    }
}
