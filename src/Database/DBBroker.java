/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Database;

import Cryptography.CryptoUtils;
import Cryptography.HashUtils;
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

    public boolean userExists(String username) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public int insertUser(User u) {
        String passwordHashed = HashUtils.hashPassword(u.getPassword());

        try {
            String sql = "INSERT INTO users (username, password, salt) VALUES (?, ?, ?)";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);

            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            String saltBase64 = Base64.getEncoder().encodeToString(salt);

            ps.setString(1, u.getUsername());
            ps.setString(2, passwordHashed);
            ps.setString(3, saltBase64);

            int rows = ps.executeUpdate();
            return rows > 0 ? 1 : 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public User selectUser(String username, String password) {
        User u = new User();

        try {
            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String password_db = rs.getString("password");

                if (HashUtils.checkPassword(password, password_db)) {
                    int userID_db = rs.getInt("id");
                    String salt_db = rs.getString("salt");

                    u.setId(userID_db);
                    u.setUsername(username);
                    u.setPassword(password);
                    u.setSalt(salt_db);

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

                byte[] salt = Base64.getDecoder().decode(u.getSalt());
                String encryptedPW = rs.getString("password");
                SecretKey key = CryptoUtils.deriveKey(u.getPassword(), salt);
                String decryptedPW = CryptoUtils.decrypt(encryptedPW, key);

                entry.setId(rs.getInt("id"));
                entry.setUserId(u.getId());
                entry.setService(rs.getString("service"));
                entry.setUsername(rs.getString("username"));
                entry.setDescription(rs.getString("description"));
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
            String sql = "INSERT INTO password_entries (user_id, service, username, password, description) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);

            byte[] salt = Base64.getDecoder().decode(u.getSalt());
            SecretKey key = CryptoUtils.deriveKey(u.getPassword(), salt);
            String encryptedPassword = CryptoUtils.encrypt(pe.getPassword(), key);

            ps.setInt(1, pe.getUserId());
            ps.setString(2, pe.getService());
            ps.setString(3, pe.getUsername());
            ps.setString(4, encryptedPassword);
            ps.setString(5, pe.getDescription());

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

}
