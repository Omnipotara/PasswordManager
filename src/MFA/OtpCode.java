package MFA;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Represents one in-memory OTP login challenge.
 */
public class OtpCode {

    private int userId;
    private String plainCode;
    private LocalDateTime expiresAt;
    private int attemptsUsed;
    private int maxAttempts;
    private boolean consumed;

    public OtpCode(int userId, String plainCode, LocalDateTime expiresAt, int maxAttempts) {
        this.userId = userId;
        this.plainCode = plainCode;
        this.expiresAt = expiresAt;
        this.maxAttempts = maxAttempts;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPlainCode() {
        return plainCode;
    }

    public void setPlainCode(String plainCode) {
        this.plainCode = plainCode;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getAttemptsUsed() {
        return attemptsUsed;
    }

    public void setAttemptsUsed(int attemptsUsed) {
        this.attemptsUsed = attemptsUsed;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public boolean isExpired(Clock clock) {
        return !LocalDateTime.now(clock).isBefore(expiresAt);
    }

    public boolean hasAttemptsLeft() {
        return attemptsUsed < maxAttempts;
    }

    public void incrementAttempts() {
        attemptsUsed++;
    }

    public void markConsumed() {
        consumed = true;
    }
}
