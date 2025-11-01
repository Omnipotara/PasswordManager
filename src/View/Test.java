/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package View;

import Cryptography.HashUtils;

/**
 *
 * @author Omnix
 */
public class Test {
    public static void main(String[] args) {
        String pass = "ognjen123";
        String hashedPass = HashUtils.hashPassword(pass);
        
        String a = "ognjen";
        String b = "123";
        String pass1 = a + b;
        String hashedPass1 = HashUtils.hashPassword(pass1);
        
        System.out.println("Pass: " + hashedPass);
        System.out.println("Pass 1: " + hashedPass1);
        
        System.out.println(HashUtils.checkPassword(pass1, hashedPass));
        
        
    }
   
}
