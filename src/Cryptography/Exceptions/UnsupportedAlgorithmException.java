package Cryptography.Exceptions;

/**
 * Thrown when persisted data references an algorithm unsupported by the app.
 */
public class UnsupportedAlgorithmException extends CryptoException {

    public UnsupportedAlgorithmException(String algorithmName) {
        super("Unsupported algorithm: " + algorithmName);
    }
}
