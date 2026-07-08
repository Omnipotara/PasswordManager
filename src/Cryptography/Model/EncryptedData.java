package Cryptography.Model;

import Cryptography.AlgorithmName;

public class EncryptedData {

    private AlgorithmName algorithmName;
    private String ciphertextBase64;
    private String ivBase64;
    private String authenticationTagBase64;
    private String saltBase64;
    private String parameters;

    public EncryptedData() {
    }

    public EncryptedData(AlgorithmName algorithmName, String ciphertextBase64, String ivBase64) {
        this.algorithmName = algorithmName;
        this.ciphertextBase64 = ciphertextBase64;
        this.ivBase64 = ivBase64;
    }

    public EncryptedData(
            AlgorithmName algorithmName,
            String ciphertextBase64,
            String ivBase64,
            String authenticationTagBase64,
            String saltBase64,
            String parameters) {
        this.algorithmName = algorithmName;
        this.ciphertextBase64 = ciphertextBase64;
        this.ivBase64 = ivBase64;
        this.authenticationTagBase64 = authenticationTagBase64;
        this.saltBase64 = saltBase64;
        this.parameters = parameters;
    }

    public AlgorithmName getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(AlgorithmName algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getCiphertextBase64() {
        return ciphertextBase64;
    }

    public void setCiphertextBase64(String ciphertextBase64) {
        this.ciphertextBase64 = ciphertextBase64;
    }

    public String getIvBase64() {
        return ivBase64;
    }

    public void setIvBase64(String ivBase64) {
        this.ivBase64 = ivBase64;
    }

    public String getAuthenticationTagBase64() {
        return authenticationTagBase64;
    }

    public void setAuthenticationTagBase64(String authenticationTagBase64) {
        this.authenticationTagBase64 = authenticationTagBase64;
    }

    public String getSaltBase64() {
        return saltBase64;
    }

    public void setSaltBase64(String saltBase64) {
        this.saltBase64 = saltBase64;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public boolean hasAuthenticationTag() {
        return authenticationTagBase64 != null && !authenticationTagBase64.isEmpty();
    }

    public boolean hasSalt() {
        return saltBase64 != null && !saltBase64.isEmpty();
    }

    public boolean hasParameters() {
        return parameters != null && !parameters.isEmpty();
    }
}
