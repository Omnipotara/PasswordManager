/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Database;

import Cryptography.HashUtils;
import Model.User;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Omnix
 */
public class DBBroker {

    public boolean userExists(String username){
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
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
            ps.setString(1, u.getUsername());
            ps.setString(2, passwordHashed);
            
            int rows = ps.executeUpdate();
            return rows > 0 ? 1 : 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

}
