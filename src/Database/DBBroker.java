/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Database;

import Cryptography.AlgorithmName;
import Cryptography.Encryption.AESGCMStrategy;
import Cryptography.Encryption.EncryptionStrategy;
import Cryptography.Factory.EncryptionStrategyFactory;
import Cryptography.Factory.HashingStrategyFactory;
import Cryptography.Hashing.HashingStrategy;
import Cryptography.KeyDerivation.KeyDerivationService;
import Cryptography.Model.EncryptedData;
import Model.PasswordEntry;
import Model.User;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;

/**
 *
 * @author Omnix
 */
public class DBBroker {

    private static final KeyDerivationService KEY_DERIVATION_SERVICE = new KeyDerivationService();
    private static final AESGCMStrategy LEGACY_AES_GCM_STRATEGY = new AESGCMStrategy();
    private static final int ENCRYPTION_SALT_LENGTH_BYTES = 16;

    public boolean userExists(String email) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public int insertUser(User u) {
        AlgorithmName hashingAlgorithm = u.getHashingAlgorithm();
        HashingStrategy hashingStrategy = HashingStrategyFactory.getStrategy(hashingAlgorithm);
        String passwordHashed = hashingStrategy.hash(u.getPassword());

        try {
            String sql = "INSERT INTO users (email, password_hash, salt, hashing_algorithm, mfa_enabled) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);

            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            String saltBase64 = Base64.getEncoder().encodeToString(salt);

            ps.setString(1, u.getEmail());
            ps.setString(2, passwordHashed);
            ps.setString(3, saltBase64);
            ps.setString(4, hashingAlgorithm.getDatabaseValue());
            ps.setBoolean(5, u.isMfaEnabled());

            int rows = ps.executeUpdate();
            return rows > 0 ? 1 : 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public User selectUser(String email, String password) {
        User u = new User();

        try {
            String sql = "SELECT * FROM users WHERE email = ?";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String password_db = rs.getString("password_hash");
                String hashingAlgorithm_db = rs.getString("hashing_algorithm");
                HashingStrategy hashingStrategy = HashingStrategyFactory.getStrategy(hashingAlgorithm_db);

                if (hashingStrategy.verify(password, password_db)) {
                    int userID_db = rs.getInt("id");
                    String salt_db = rs.getString("salt");

                    u.setId(userID_db);
                    u.setEmail(email);
                    u.setPassword(password);
                    u.setPasswordHash(password_db);
                    u.setSalt(salt_db);
                    u.setHashingAlgorithm(hashingStrategy.getAlgorithmName());
                    u.setMfaEnabled(rs.getBoolean("mfa_enabled"));

                } else {
                    u.setId(-1);
                }
            } else {
                u.setId(-1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
            u.setId(-1);
            return u;
        }
        return u;
    }

    public List<PasswordEntry> selectEntries(User u) {
        List<PasswordEntry> entryList = new ArrayList<>();

        try {
            String sql = "SELECT * FROM password_entries WHERE user_id = ?";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
            ps.setInt(1, u.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PasswordEntry entry = new PasswordEntry();

                entry.setId(rs.getInt("id"));
                entry.setUserId(u.getId());
                entry.setService(rs.getString("service"));
                entry.setUsername(rs.getString("username"));
                entry.setDescription(rs.getString("description"));
                entry.setPassword(rs.getString("password"));
                entry.setEncryptionAlgorithm(AlgorithmName.fromDatabaseValue(rs.getString("encryption_algorithm")));
                entry.setIv(rs.getString("iv"));
                entry.setAuthenticationTag(rs.getString("authentication_tag"));
                entry.setEncryptionSalt(rs.getString("encryption_salt"));
                entry.setEncryptionParameters(rs.getString("encryption_parameters"));

                SecretKey key = deriveEntryKey(u, entry.getEncryptionSalt());
                String decryptedPW;
                if (entry.getIv() == null || entry.getIv().isEmpty() || entry.getAuthenticationTag() == null || entry.getAuthenticationTag().isEmpty()) {
                    decryptedPW = decryptLegacyAesGcm(entry.getPassword(), key);
                } else {
                    EncryptionStrategy encryptionStrategy = EncryptionStrategyFactory.getStrategy(entry.getEncryptionAlgorithm());
                    decryptedPW = encryptionStrategy.decrypt(entry.toEncryptedData(), key);
                }
                entry.setPassword(decryptedPW);

                entryList.add(entry);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entryList;
    }

    public boolean insertEntry(PasswordEntry pe, User u) {
        try {
            String sql = "INSERT INTO password_entries "
                    + "(user_id, service, username, password, encryption_algorithm, iv, authentication_tag, encryption_salt, encryption_parameters, description) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);

            AlgorithmName encryptionAlgorithm = pe.getEncryptionAlgorithm();
            EncryptionStrategy encryptionStrategy = EncryptionStrategyFactory.getStrategy(encryptionAlgorithm);
            String encryptionSalt = generateEncryptionSalt();
            SecretKey key = deriveEntryKey(u, encryptionSalt);
            EncryptedData encryptedData = encryptionStrategy.encrypt(pe.getPassword(), key);
            encryptedData.setSaltBase64(encryptionSalt);
            pe.applyEncryptedData(encryptedData);

            ps.setInt(1, u.getId());
            ps.setString(2, pe.getService());
            ps.setString(3, pe.getUsername());
            ps.setString(4, pe.getPassword());
            ps.setString(5, pe.getEncryptionAlgorithm().getDatabaseValue());
            ps.setString(6, pe.getIv());
            ps.setString(7, pe.getAuthenticationTag());
            ps.setString(8, pe.getEncryptionSalt());
            ps.setString(9, pe.getEncryptionParameters());
            ps.setString(10, pe.getDescription());

            int insertedRows = ps.executeUpdate();
            return insertedRows > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean deleteEntry(PasswordEntry pe, User u) {
        try {
            String sql = "DELETE FROM password_entries WHERE id = ? AND user_id = ?";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
            ps.setInt(1, pe.getId());
            ps.setInt(2, u.getId());
            int deletedRows = ps.executeUpdate();

            return deletedRows > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean updateEntry(PasswordEntry pe, User u) {
        try {
            String sql = "UPDATE password_entries SET service = ?, username = ?, description = ? WHERE id = ? AND user_id = ?";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
            ps.setString(1, pe.getService());
            ps.setString(2, pe.getUsername());
            ps.setString(3, pe.getDescription());
            ps.setInt(4, pe.getId());
            ps.setInt(5, u.getId());
            int updatedRows = ps.executeUpdate();
            return updatedRows > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private SecretKey deriveEntryKey(User user, String encryptionSalt) {
        String saltBase64 = encryptionSalt != null && !encryptionSalt.isEmpty()
                ? encryptionSalt
                : user.getSalt();
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        return KEY_DERIVATION_SERVICE.deriveEncryptionKey(user.getPassword(), salt);
    }

    private String generateEncryptionSalt() {
        byte[] salt = new byte[ENCRYPTION_SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String decryptLegacyAesGcm(String legacyCiphertext, SecretKey key) {
        EncryptedData encryptedData = LEGACY_AES_GCM_STRATEGY.fromLegacyCombinedBase64(legacyCiphertext);
        return LEGACY_AES_GCM_STRATEGY.decrypt(encryptedData, key);
    }

}
