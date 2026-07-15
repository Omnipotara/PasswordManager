package Cryptography.Hashing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class HashingStrategiesTest {

    private static final String PASSWORD = "MasterPassword123!";
    private static final String WRONG_PASSWORD = "WrongPassword123!";

    @Test
    public void bcryptShouldVerifyCorrectPasswordAndRejectWrongPassword() {
        assertHashingStrategyWorks(new BCryptStrategy());
    }

    @Test
    public void pbkdf2ShouldVerifyCorrectPasswordAndRejectWrongPassword() {
        assertHashingStrategyWorks(new PBKDF2HashingStrategy());
    }

    @Test
    public void argon2idShouldVerifyCorrectPasswordAndRejectWrongPassword() {
        assertHashingStrategyWorks(new Argon2idStrategy());
    }

    private void assertHashingStrategyWorks(HashingStrategy strategy) {
        String storedHash = strategy.hash(PASSWORD);

        assertNotEquals(PASSWORD, storedHash);
        assertTrue(strategy.verify(PASSWORD, storedHash));
        assertFalse(strategy.verify(WRONG_PASSWORD, storedHash));
    }
}
