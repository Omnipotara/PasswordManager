package MFA;

/**
 * Contains the OTP challenge and the code that should be delivered by email.
 */
public class OtpGenerationResult {

    private final String plainCode;
    private final OtpCode otpCode;

    public OtpGenerationResult(String plainCode, OtpCode otpCode) {
        this.plainCode = plainCode;
        this.otpCode = otpCode;
    }

    public String getPlainCode() {
        return plainCode;
    }

    public OtpCode getOtpCode() {
        return otpCode;
    }
}
