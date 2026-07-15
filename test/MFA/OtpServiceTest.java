package MFA;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.Duration;
import java.security.SecureRandom;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class OtpServiceTest {

    @Test
    public void generatedOtpShouldHaveSixDigitsAndVerifySuccessfully() {
        OtpService service = new OtpService();
        OtpGenerationResult result = service.createOtp(1);

        assertTrue(result.getPlainCode().matches("\\d{6}"));
        assertTrue(service.verify(result.getOtpCode(), result.getPlainCode()).isSuccessful());
    }

    @Test
    public void otpShouldRejectReuse() {
        OtpService service = new OtpService();
        OtpGenerationResult result = service.createOtp(1);

        assertTrue(service.verify(result.getOtpCode(), result.getPlainCode()).isSuccessful());
        assertEquals(
                OtpVerificationResult.Status.CONSUMED,
                service.verify(result.getOtpCode(), result.getPlainCode()).getStatus());
    }

    @Test
    public void otpShouldLockAfterThreeWrongAttempts() {
        OtpService service = new OtpService();
        OtpCode otpCode = service.createOtp(1).getOtpCode();

        assertFalse(service.verify(otpCode, "000000").isSuccessful());
        assertFalse(service.verify(otpCode, "000001").isSuccessful());
        assertEquals(
                OtpVerificationResult.Status.TOO_MANY_ATTEMPTS,
                service.verify(otpCode, "000002").getStatus());
    }

    @Test
    public void newOtpShouldInvalidatePreviousOtp() {
        OtpService service = new OtpService();
        OtpGenerationResult first = service.createOtp(1);
        service.createOtp(1, first.getOtpCode());

        assertTrue(first.getOtpCode().isConsumed());
    }

    @Test
    public void expiredOtpShouldBeRejected() {
        MutableClock clock = new MutableClock(Instant.parse("2026-07-15T10:00:00Z"));
        OtpService service = new OtpService(new SecureRandom(), clock);
        OtpGenerationResult result = service.createOtp(1);

        clock.advance(OtpService.VALIDITY_DURATION.plusSeconds(1));

        assertEquals(
                OtpVerificationResult.Status.EXPIRED,
                service.verify(result.getOtpCode(), result.getPlainCode()).getStatus());
    }

    private static class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
