/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Cryptography;
import BCrypt.src.org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Omnix
 */
public class HashUtils {
    public static String hashPassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
    
    public static boolean checkPassword(String password, String hash){
        return BCrypt.checkpw(password, hash);
    }
}
