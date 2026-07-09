/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import Cryptography.AlgorithmName;
import Cryptography.Model.EncryptedData;

/**
 *
 * @author Omnix
 */
public class PasswordEntry {

    private int id; // Database entry ID.
    private int userId; // Entry owner user ID.
    private String service; // Service or website name.
    private String username; // Service login identifier.
    private String password; // Plaintext or ciphertext password.
    private String description; // Optional entry note.
    private AlgorithmName encryptionAlgorithm = AlgorithmName.AES_GCM; // Password encryption algorithm.
    private String iv; // IV or nonce Base64.
    private String authenticationTag; // Authentication tag Base64.
    private String encryptionSalt; // Entry encryption salt.
    private String encryptionParameters; // Optional encryption parameters.

    public PasswordEntry() {
    }

    public PasswordEntry(int id, int userId, String service, String username, String password, String description) {
        this.id = id;
        this.userId = userId;
        this.service = service;
        this.username = username;
        this.password = password;
        this.description = description;
    }

    public PasswordEntry(
            int id,
            int userId,
            String service,
            String username,
            String password,
            String description,
            AlgorithmName encryptionAlgorithm,
            String iv,
            String authenticationTag,
            String encryptionSalt,
            String encryptionParameters) {
        this.id = id;
        this.userId = userId;
        this.service = service;
        this.username = username;
        this.password = password;
        this.description = description;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.iv = iv;
        this.authenticationTag = authenticationTag;
        this.encryptionSalt = encryptionSalt;
        this.encryptionParameters = encryptionParameters;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AlgorithmName getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(AlgorithmName encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getAuthenticationTag() {
        return authenticationTag;
    }

    public void setAuthenticationTag(String authenticationTag) {
        this.authenticationTag = authenticationTag;
    }

    public String getEncryptionSalt() {
        return encryptionSalt;
    }

    public void setEncryptionSalt(String encryptionSalt) {
        this.encryptionSalt = encryptionSalt;
    }

    public String getEncryptionParameters() {
        return encryptionParameters;
    }

    public void setEncryptionParameters(String encryptionParameters) {
        this.encryptionParameters = encryptionParameters;
    }

    public EncryptedData toEncryptedData() {
        return new EncryptedData(
                encryptionAlgorithm,
                password,
                iv,
                authenticationTag,
                encryptionSalt,
                encryptionParameters);
    }

    public void applyEncryptedData(EncryptedData encryptedData) {
        this.encryptionAlgorithm = encryptedData.getAlgorithmName();
        this.password = encryptedData.getCiphertextBase64();
        this.iv = encryptedData.getIvBase64();
        this.authenticationTag = encryptedData.getAuthenticationTagBase64();
        this.encryptionSalt = encryptedData.getSaltBase64();
        this.encryptionParameters = encryptedData.getParameters();
    }

}
