package com.hoogercoin.helpers;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.hoogercoin.datastructs.CryptResult;
import com.hoogercoin.session.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptorQuest {

    // this class is a helper class to handle crypto-based http requests.

    private final static String CRYPTO_METHOD = "RSA";
    private final static int CRYPTO_BITS = 2048;

    public enum EncryptKey {
        PUBLIC_KEY,
        SERVER_PUBLIC_KEY,
        COMMUNICATION_PUBLIC_KEY
    }


    public enum DecryptKey {
        PRIVATE_KEY,
        COMMUNICATION_PRIVATE_KEY
    }

    public static Map<String, String> generateKeyPair()
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(CRYPTO_METHOD);
        kpg.initialize(CRYPTO_BITS);
        KeyPair kp = kpg.genKeyPair();
        PublicKey publicKey = kp.getPublic();
        PrivateKey privateKey = kp.getPrivate();

        Map map = new HashMap<String, String>();
        map.put("privateKey", Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT));
        map.put("publicKey", Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT));
        return map;
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static CryptResult encrypt(Context context, String plain, EncryptKey encryptKey)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException,
            InvalidKeySpecException, InvalidAlgorithmParameterException {

        // symmetrically encrypt data using AES communication key.
        Cipher aesCipher = Cipher.getInstance("AES");
        String symmetricKey = Keyler.getKey(context, true, Keyler.KeyType.COMMUNICATION_SYMMETRIC_KEY);
        aesCipher.init(Cipher.ENCRYPT_MODE, stringToSecretKey(symmetricKey), generateIv());
        byte[] encryptedBytes = aesCipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        String encrypted = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);



        if (encryptKey == EncryptKey.COMMUNICATION_PUBLIC_KEY) {
            // asymmetrically encrypt key using RSA-OAEP-Padded.
            String key = Keyler.getKey(context, false, Keyler.KeyType.COMMUNICATION_PUBLIC_KEY);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.ENCRYPT_MODE, stringToPublicKey(key));
            byte[] encryptedKeyBytes = cipher.doFinal(symmetricKey.getBytes(StandardCharsets.UTF_8));
            String encryptedKey = Base64.encodeToString(encryptedKeyBytes, Base64.DEFAULT);

            return new CryptResult(encrypted, encryptedKey);
        }
        else if (encryptKey == EncryptKey.SERVER_PUBLIC_KEY) {
            // asymmetrically encrypt key using RSA-OAEP-Padded.
            String key = Keyler.getKey(context, false, Keyler.KeyType.SERVER_PUBLIC_KEY);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.ENCRYPT_MODE, stringToPublicKey(key));
            byte[] encryptedKeyBytes = cipher.doFinal(symmetricKey.getBytes(StandardCharsets.UTF_8));
            String encryptedKey = Base64.encodeToString(encryptedKeyBytes, Base64.DEFAULT);

            return new CryptResult(encrypted, encryptedKey);
        }
        else {
            return null;
        }
    }

    public static String decrypt(Context context, CryptResult cryptResult, DecryptKey decryptKey)
            throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            BadPaddingException,
            IllegalBlockSizeException,
            InvalidKeySpecException,
            InvalidKeyException {

        if (decryptKey == DecryptKey.COMMUNICATION_PRIVATE_KEY) {
            // asymmetrically decrypt key using RSA-OAEP-Padded.
            String key = Keyler.getKey(context, false, Keyler.KeyType.COMMUNICATION_PRIVATE_KEY);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.DECRYPT_MODE, stringToPrivateKey(key));
            byte[] decryptedKeyBytes = cipher.doFinal(Base64.decode(cryptResult.getEncryptedKey(), Base64.DEFAULT));
            String symmetricKey = new String(decryptedKeyBytes);

            // symmetrically decrypt data using AES communication key.
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, stringToSecretKey(symmetricKey));
            byte[] decryptedBytes = aesCipher.doFinal(Base64.decode(cryptResult.getEncryptedData(), Base64.DEFAULT));
            String decrypted = Base64.encodeToString(decryptedBytes, Base64.DEFAULT);
            return decrypted;
        }
        else {
            return "";
        }
    }

    public static SecretKey stringToSecretKey(String stringKey) {
        byte[] decodedKey = Base64.decode(stringKey, Base64.DEFAULT);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }


    public static PublicKey stringToPublicKey(String publicKeyString)
            throws InvalidKeySpecException,
            NoSuchAlgorithmException {

        byte[] keyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(CRYPTO_METHOD);
        return keyFactory.generatePublic(spec);
    }

    public static PrivateKey stringToPrivateKey(String privateKeyString)
            throws InvalidKeySpecException,
            NoSuchAlgorithmException {

        byte [] pkcs8EncodedBytes = Base64.decode(privateKeyString, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance(CRYPTO_METHOD);
        return kf.generatePrivate(keySpec);
    }

}
