package Cryptography.Encryption;

import Cryptography.AlgorithmName;
import Cryptography.Model.EncryptedData;
import javax.crypto.SecretKey;

/**
 * Common contract for algorithms that protect stored password entries.
 */
public interface EncryptionStrategy {

    EncryptedData encrypt(String plaintext, SecretKey key);

    String decrypt(EncryptedData encryptedData, SecretKey key);

    AlgorithmName getAlgorithmName();
}
