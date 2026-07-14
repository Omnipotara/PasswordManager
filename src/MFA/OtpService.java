package MFA;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Generates and verifies one-time email login codes.
 */
public class OtpService {

    public static final int CODE_DIGITS = 6;
    public static final int MAX_ATTEMPTS = 3;
    public static final Duration VALIDITY_DURATION = Duration.ofMinutes(2);

    private static final int MIN_CODE_VALUE = 100000;
    private static final int CODE_RANGE = 900000;

    private final SecureRandom secureRandom;
    private final Clock clock;

    public OtpService() {
        this(new SecureRandom(), Clock.systemDefaultZone());
    }

    public OtpService(SecureRandom secureRandom, Clock clock) {
        this.secureRandom = secureRandom;
        this.clock = clock;
    }

    public OtpGenerationResult createOtp(int userId) {
        return createOtp(userId, null);
    }

    public OtpGenerationResult createOtp(int userId, OtpCode previousOtp) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User id must be positive.");
        }

        if (previousOtp != null && !previousOtp.isConsumed()) {
            previousOtp.markConsumed();
        }

        String plainCode = generatePlainCode();
        LocalDateTime expiresAt = LocalDateTime.now(clock).plus(VALIDITY_DURATION);
        OtpCode otpCode = new OtpCode(userId, plainCode, expiresAt, MAX_ATTEMPTS);

        return new OtpGenerationResult(plainCode, otpCode);
    }

    public OtpVerificationResult verify(OtpCode otpCode, String submittedCode) {
        if (otpCode == null || submittedCode == null || !submittedCode.matches("\\d{" + CODE_DIGITS + "}")) {
            return OtpVerificationResult.failure(OtpVerificationResult.Status.INVALID_INPUT);
        }

        if (otpCode.isConsumed()) {
            return OtpVerificationResult.failure(OtpVerificationResult.Status.CONSUMED);
        }

        if (otpCode.isExpired(clock)) {
            otpCode.markConsumed();
            return OtpVerificationResult.failure(OtpVerificationResult.Status.EXPIRED);
        }

        if (!otpCode.hasAttemptsLeft()) {
            otpCode.markConsumed();
            return OtpVerificationResult.failure(OtpVerificationResult.Status.TOO_MANY_ATTEMPTS);
        }

        if (Objects.equals(submittedCode, otpCode.getPlainCode())) {
            otpCode.markConsumed();
            return OtpVerificationResult.success();
        }

        otpCode.incrementAttempts();
        if (!otpCode.hasAttemptsLeft()) {
            otpCode.markConsumed();
            return OtpVerificationResult.failure(OtpVerificationResult.Status.TOO_MANY_ATTEMPTS);
        }

        return OtpVerificationResult.failure(OtpVerificationResult.Status.INVALID_CODE);
    }

    private String generatePlainCode() {
        int code = secureRandom.nextInt(CODE_RANGE) + MIN_CODE_VALUE;
        return String.valueOf(code);
    }
}
