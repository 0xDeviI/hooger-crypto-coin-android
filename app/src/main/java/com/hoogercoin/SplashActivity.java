package com.hoogercoin;

import static com.hoogercoin.helpers.Graphtil.setWindowFlag;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.hoogercoin.dialogs.SimpleDialog;
import com.hoogercoin.helpers.APIUtil;
import com.hoogercoin.helpers.CryptorQuest;
import com.hoogercoin.helpers.Keyler;
import com.hoogercoin.helpers.MemoHero;
import com.hoogercoin.session.SessionManager;
import com.hoogercoin.session.SessionValidator;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SplashActivity extends AppCompatActivity {

    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        session = new SessionManager(getApplicationContext());
        AndroidNetworking.initialize(getApplicationContext());

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setupSecurityProtocols();
        fetchServerInfo();
    }

    private void setupSecurityProtocols() {
        SessionManager protocols = new SessionManager(getApplicationContext(), "protocols");
        if (protocols.getString("communication_private_key").length() == 0) {
            SecretKey secretKey = Keyler.generateAESKey(128);
            KeyPair keyPairs = Keyler.generateRSAKeyPair(2048);
            protocols.setString("communication_symmetric_key", Keyler.symmetricKeyToString(secretKey));
            protocols.setString("communication_private_key", Keyler.privateKeyToString(keyPairs.getPrivate()));
            protocols.setString("communication_public_key", Keyler.publicKeyToString(keyPairs.getPublic()));
        }
    }

    private void fetchServerInfo() {
        SessionManager serverSession = new SessionManager(getApplicationContext(), "server");
        AndroidNetworking.get(APIUtil.getServerInfo)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                serverSession.setString("server_name", response.getJSONObject("data").getString("server_name"));
                                serverSession.setString("server_description", response.getJSONObject("data").getString("server_description"));
                                serverSession.setString("server_version", response.getJSONObject("data").getString("server_version"));
                                serverSession.setString("public_key", response.getJSONObject("data").getString("public_key"));
                                redirect();
                            }
                            else {
                                new SimpleDialog(SplashActivity.this, "خطا", "خطایی در دریافت اطلاعات سرور رخ داد.", v -> {
                                    finish();
                                }, false).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        new SimpleDialog(SplashActivity.this, "خطا", "خطایی در دریافت اطلاعات سرور رخ داد.", v -> {
                            finish();
                        }, false).show();
                    }
                });
    }

    private void redirect(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SessionValidator.isLoggedIn(session)) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
                else {
                    startActivity(new Intent(SplashActivity.this, SplashSliderActivity.class));
                    finish();
                }
            }
        }, 3000);

    }



}