package Integration;

import Cryptography.AlgorithmName;
import Database.DBConnection;
import Model.PasswordEntry;
import Model.User;
import Singletons.Controller;
import java.sql.PreparedStatement;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UserFlowIntegrationTest {

    @Test
    public void userShouldRegisterLoginManageEntriesAndCleanup() throws Exception {
        String suffix = String.valueOf(System.currentTimeMillis());
        String email = "integration-" + suffix + "@example.local";
        String password = "Integration123!";
        Controller controller = Controller.getInstance();

        try {
            User registrationUser = new User(email, password, AlgorithmName.BCRYPT);
            assertEquals(1, controller.insertUser(registrationUser));

            User loggedInUser = controller.loginUser(email, password);
            assertTrue(loggedInUser.getId() > 0);
            assertEquals(email, loggedInUser.getEmail());
            assertTrue(controller.loginUser(email, "wrong-password").getId() < 0);
            assertTrue(controller.loginUser("missing-" + email, password).getId() < 0);

            insertEntry(controller, loggedInUser, "gmail", "gmail-user", "gmail-secret", AlgorithmName.AES_GCM);
            insertEntry(controller, loggedInUser, "bank", "bank-user", "bank-secret", AlgorithmName.AES_CBC_HMAC);
            insertEntry(controller, loggedInUser, "cloud", "cloud-user", "cloud-secret", AlgorithmName.CHACHA20_POLY1305);

            List<PasswordEntry> entries = controller.selectEntries(loggedInUser);
            assertEquals(3, entries.size());
            assertContainsDecryptedPassword(entries, "gmail-secret");
            assertContainsDecryptedPassword(entries, "bank-secret");
            assertContainsDecryptedPassword(entries, "cloud-secret");

            PasswordEntry entryToUpdate = entries.get(0);
            entryToUpdate.setService("updated-service");
            entryToUpdate.setUsername("updated-user");
            entryToUpdate.setDescription("updated-description");
            assertTrue(controller.updateEntry(entryToUpdate, loggedInUser));

            List<PasswordEntry> updatedEntries = controller.selectEntries(loggedInUser);
            assertTrue(updatedEntries.stream().anyMatch(entry
                    -> "updated-service".equals(entry.getService())
                    && "updated-user".equals(entry.getUsername())
                    && "updated-description".equals(entry.getDescription())));

            for (PasswordEntry entry : updatedEntries) {
                assertTrue(controller.deleteEntry(entry, loggedInUser));
            }

            assertTrue(controller.selectEntries(loggedInUser).isEmpty());
        } finally {
            cleanup(email);
        }
    }

    private void insertEntry(
            Controller controller,
            User user,
            String service,
            String username,
            String password,
            AlgorithmName encryptionAlgorithm) {
        PasswordEntry entry = new PasswordEntry();
        entry.setService(service);
        entry.setUsername(username);
        entry.setPassword(password);
        entry.setDescription(service + " description");
        entry.setEncryptionAlgorithm(encryptionAlgorithm);

        assertTrue(controller.insertEntry(entry, user));
    }

    private void assertContainsDecryptedPassword(List<PasswordEntry> entries, String password) {
        assertTrue(entries.stream().anyMatch(entry -> password.equals(entry.getPassword())));
    }

    private void cleanup(String email) throws Exception {
        PreparedStatement selectUser = DBConnection.getInstance().getConnection()
                .prepareStatement("SELECT id FROM users WHERE email = ?");
        selectUser.setString(1, email);
        java.sql.ResultSet resultSet = selectUser.executeQuery();
        if (!resultSet.next()) {
            return;
        }

        int userId = resultSet.getInt("id");
        PreparedStatement deleteEntries = DBConnection.getInstance().getConnection()
                .prepareStatement("DELETE FROM password_entries WHERE user_id = ?");
        deleteEntries.setInt(1, userId);
        deleteEntries.executeUpdate();

        PreparedStatement deleteUser = DBConnection.getInstance().getConnection()
                .prepareStatement("DELETE FROM users WHERE id = ?");
        deleteUser.setInt(1, userId);
        deleteUser.executeUpdate();
    }
}
