package Cryptography.Factory;

import Cryptography.AlgorithmName;
import Cryptography.Encryption.AESCBCStrategy;
import Cryptography.Encryption.AESGCMStrategy;
import Cryptography.Encryption.ChaCha20Poly1305Strategy;
import Cryptography.Exceptions.UnsupportedAlgorithmException;
import Cryptography.Hashing.Argon2idStrategy;
import Cryptography.Hashing.BCryptStrategy;
import Cryptography.Hashing.PBKDF2HashingStrategy;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class StrategyFactoryTest {

    @Test
    public void hashingFactoryShouldResolveKnownAlgorithms() {
        assertTrue(HashingStrategyFactory.getStrategy(AlgorithmName.BCRYPT) instanceof BCryptStrategy);
        assertTrue(HashingStrategyFactory.getStrategy(AlgorithmName.PBKDF2) instanceof PBKDF2HashingStrategy);
        assertTrue(HashingStrategyFactory.getStrategy(AlgorithmName.ARGON2ID) instanceof Argon2idStrategy);
    }

    @Test
    public void encryptionFactoryShouldResolveKnownAlgorithms() {
        assertTrue(EncryptionStrategyFactory.getStrategy(AlgorithmName.AES_GCM) instanceof AESGCMStrategy);
        assertTrue(EncryptionStrategyFactory.getStrategy(AlgorithmName.AES_CBC_HMAC) instanceof AESCBCStrategy);
        assertTrue(EncryptionStrategyFactory.getStrategy(AlgorithmName.CHACHA20_POLY1305) instanceof ChaCha20Poly1305Strategy);
    }

    @Test(expected = UnsupportedAlgorithmException.class)
    public void hashingFactoryShouldRejectEncryptionAlgorithm() {
        HashingStrategyFactory.getStrategy(AlgorithmName.AES_GCM);
    }

    @Test(expected = UnsupportedAlgorithmException.class)
    public void encryptionFactoryShouldRejectHashingAlgorithm() {
        EncryptionStrategyFactory.getStrategy(AlgorithmName.BCRYPT);
    }

    @Test(expected = UnsupportedAlgorithmException.class)
    public void algorithmNameShouldRejectUnknownDatabaseValue() {
        AlgorithmName.fromDatabaseValue("UNKNOWN");
    }
}
