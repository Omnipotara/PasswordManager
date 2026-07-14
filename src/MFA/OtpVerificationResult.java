package MFA;

/**
 * Result of checking a submitted OTP against a stored challenge.
 */
public class OtpVerificationResult {

    private final boolean successful;
    private final Status status;

    private OtpVerificationResult(boolean successful, Status status) {
        this.successful = successful;
        this.status = status;
    }

    public static OtpVerificationResult success() {
        return new OtpVerificationResult(true, Status.SUCCESS);
    }

    public static OtpVerificationResult failure(Status status) {
        return new OtpVerificationResult(false, status);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        SUCCESS,
        INVALID_INPUT,
        INVALID_CODE,
        EXPIRED,
        TOO_MANY_ATTEMPTS,
        CONSUMED
    }
}
