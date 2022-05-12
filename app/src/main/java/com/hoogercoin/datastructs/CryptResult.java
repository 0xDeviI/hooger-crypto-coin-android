package com.hoogercoin.datastructs;

public class CryptResult {
    private String encryptedData, encryptedKey;

    public CryptResult(String encryptedData, String encryptedKey) {
        this.encryptedData = encryptedData;
        this.encryptedKey = encryptedKey;
    }

    public CryptResult() {
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }
}
