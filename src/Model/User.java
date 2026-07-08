/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import Cryptography.AlgorithmName;

/**
 *
 * @author Omnix
 */
public class User {
    private int id; // Database user ID.
    private String email; // Current login identifier.
    private String password; // Current plain session password.
    private String passwordHash; // Stored master password hash.
    private String salt; // Encryption key derivation salt.
    private AlgorithmName hashingAlgorithm = AlgorithmName.BCRYPT; // Master password hash algorithm.
    private String hashingParameters; // Optional separated hash parameters.
    private boolean mfaEnabled; // MFA enabled flag.

    public User() {
    }

    public User(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, AlgorithmName hashingAlgorithm) {
        this.email = email;
        this.password = password;
        this.hashingAlgorithm = hashingAlgorithm;
    }

    public User(String email, String password, AlgorithmName hashingAlgorithm, boolean mfaEnabled) {
        this.email = email;
        this.password = password;
        this.hashingAlgorithm = hashingAlgorithm;
        this.mfaEnabled = mfaEnabled;
    }

    public User(int id, String email, String password, String salt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.salt = salt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public AlgorithmName getHashingAlgorithm() {
        return hashingAlgorithm;
    }

    public void setHashingAlgorithm(AlgorithmName hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
    }

    public String getHashingParameters() {
        return hashingParameters;
    }

    public void setHashingParameters(String hashingParameters) {
        this.hashingParameters = hashingParameters;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }
}
