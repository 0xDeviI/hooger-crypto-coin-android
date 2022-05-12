package com.hoogercoin.helpers;

import android.content.Context;
import android.util.Base64;

import com.hoogercoin.session.SessionManager;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Keyler {
    // Keyler is a helper class that generates RSA key pairs.

    public enum KeyType {
        PUBLIC_KEY,
        PRIVATE_KEY,
        SERVER_PUBLIC_KEY,
        COMMUNICATION_PUBLIC_KEY,
        COMMUNICATION_PRIVATE_KEY,
        COMMUNICATION_SYMMETRIC_KEY
    }

    public static KeyPair generateRSAKeyPair(int keySize) {
        KeyPair ret = null;

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize, new SecureRandom());
            ret = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static SecretKey generateAESKey(int keySize) {
        SecretKey ret = null;

        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(keySize, new SecureRandom());
            ret = generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static String publicKeyToString(PublicKey publicKey){
        return Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
    }


    public static String symmetricKeyToString(SecretKey publicKey){
        return Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
    }

    public static String privateKeyToString(PrivateKey privateKey){
        return Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
    }

    public static String getKey(Context context, boolean oneLineKey, KeyType keyType) {
        String key = "";
        SessionManager session;
        if (keyType == KeyType.PUBLIC_KEY){
            session = new SessionManager(context);
            key = session.getString("public_key");
        }
        else if (keyType == KeyType.PRIVATE_KEY){
            session = new SessionManager(context);
            key = session.getString("private_key");
        }
        else if (keyType == KeyType.SERVER_PUBLIC_KEY){
            session = new SessionManager(context, "server");
            key = session.getString("public_key");
        }
        else if (keyType == KeyType.COMMUNICATION_PUBLIC_KEY){
            session = new SessionManager(context, "protocols");
            key = session.getString("communication_public_key");
        }
        else if (keyType == KeyType.COMMUNICATION_PRIVATE_KEY){
            session = new SessionManager(context, "protocols");
            key = session.getString("communication_private_key");
        }
        else if (keyType == KeyType.COMMUNICATION_SYMMETRIC_KEY) {
            session = new SessionManager(context, "protocols");
            key = session.getString("communication_symmetric_key");
        }
        return oneLineKey ? key.replace("\n", "") : key;
    }

}
