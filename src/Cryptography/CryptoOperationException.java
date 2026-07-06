package Cryptography;

/**
 * Thrown when a cryptographic operation fails after a valid algorithm was chosen.
 */
public class CryptoOperationException extends CryptoException {

    public CryptoOperationException(String message) {
        super(message);
    }

    public CryptoOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
