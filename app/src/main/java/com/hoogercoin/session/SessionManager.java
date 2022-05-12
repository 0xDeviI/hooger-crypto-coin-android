package com.hoogercoin.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {
    private Context context;
    private SharedPreferences preferences;
    String masterKeyAlias = null;

    public SessionManager(Context context) {
        this.context = context;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            preferences = EncryptedSharedPreferences.create(
                    "hgrc_a_shp",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SessionManager(Context context, String sessionName) {
        this.context = context;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            preferences = EncryptedSharedPreferences.create(
                    sessionName,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SessionManager destroySession() {
        preferences.edit().clear().apply();
        return null;
    }

    public void setBoolean(String key, boolean value){
        preferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key){
        return preferences.getBoolean(key, false);
    }

    public void setString(String key, String value){
        preferences.edit().putString(key, value).apply();
    }

    public String getString(String key){
        return preferences.getString(key, "");
    }

    public void setLoggedIn(String userId, String username, String password, String token){
        preferences.edit().putBoolean("isLoggedIn", true).apply();
        preferences.edit().putString("userId", userId).apply();
        preferences.edit().putString("username", username).apply();
        preferences.edit().putString("password", password).apply();
        preferences.edit().putString("token", token).apply();
    }

    public String getToken() {
        return "Bearer " + getString("token");
    }

    public boolean isLoggedIn(){
        return preferences.getBoolean("isLoggedIn", false);
    }

}
