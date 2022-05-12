package com.hoogercoin;

import static com.hoogercoin.helpers.Graphtil.setWindowFlag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.transition.TransitionManager;
import androidx.transition.Transition;
import androidx.transition.Fade;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.hoogercoin.datastructs.CryptResult;
import com.hoogercoin.dialogs.LoadingDialog;
import com.hoogercoin.dialogs.SimpleDialog;
import com.hoogercoin.helpers.APIUtil;
import com.hoogercoin.helpers.Keyler;
import com.hoogercoin.helpers.MemoHero;
import com.hoogercoin.helpers.Validity;
import com.hoogercoin.session.SessionManager;
import com.hoogercoin.helpers.CryptorQuest;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class RegisterActivity extends AppCompatActivity {

    private SlidrInterface slidr;
    private ImageView back;
    private AppCompatButton register_next;
    private EditText username;
    LinearLayout register_step_1;
    LinearLayout register_step_2;
    TextView passphrase;
    ImageView copyPassphrase;
    int step = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        slidr = Slidr.attach(this);
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

        detectUIElements();
        defineUIElements();
    }

    private void detectUIElements() {
        back = findViewById(R.id.back);
        register_next = findViewById(R.id.register_next);
        username = findViewById(R.id.username);
        register_step_1 = findViewById(R.id.register_step_1);
        register_step_2 = findViewById(R.id.register_step_2);
        passphrase = findViewById(R.id.passphrase);
        copyPassphrase = findViewById(R.id.copyPassphrase);
    }

    private void defineUIElements() {
        back.setOnClickListener(v -> {
            finish();
        });

        register_next.setOnClickListener(v -> {
            if (step == 1) {
                if (username.getText().toString().length() != 0 && Validity.isValidUsername(username.getText().toString())){
                    JSONObject requestParam = new JSONObject();
                    try {
                        requestParam.put("username", username.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    LoadingDialog ld = new LoadingDialog(this, "", false);
                    ld.show();
                        AndroidNetworking.post(APIUtil.registerAccount)
                                .addJSONObjectBody(requestParam)
                                .addHeaders("Publickey", Keyler.getKey(getApplicationContext(), true, Keyler.KeyType.COMMUNICATION_PUBLIC_KEY))
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        ld.dismiss();
                                        try {
                                            boolean success = response.getString("status").equals("success");
                                            if (success) {
                                                setVisibility(register_step_1, View.GONE);
                                                passphrase.setText(response.getJSONObject("data").getString("password"));
                                                setVisibility(register_step_2, View.VISIBLE);
                                                register_next.setText("ایجاد حساب");
                                                step++;
                                            }
                                            else {
                                                Toast.makeText(RegisterActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(RegisterActivity.this, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        Toast.makeText(RegisterActivity.this, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                                    }
                                });

                }
                else {
                    new SimpleDialog(this, "", "نام کاربری فقط باید شامل اعداد و حروف انگلیسی باشد.", null, true).show();
                }
            }
            else if (step == 2) {
                SessionManager session = new SessionManager(getApplicationContext(), username.getText().toString());
                KeyPair keyPair = Keyler.generateRSAKeyPair(2048);
                session.setString("private_key", Keyler.privateKeyToString(keyPair.getPrivate()));
                session.setString("public_key", Keyler.publicKeyToString(keyPair.getPublic()));

                JSONObject requestParam = new JSONObject();
                try {
                    requestParam.put("username", username.getText().toString());
                    requestParam.put("public_key", session.getString("public_key"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                LoadingDialog ld = new LoadingDialog(this, "", false);
                ld.show();
                AndroidNetworking.post(APIUtil.savePublicKey)
                        .addJSONObjectBody(requestParam)
                        .addHeaders("Publickey", Keyler.getKey(getApplicationContext(), true, Keyler.KeyType.COMMUNICATION_PUBLIC_KEY))
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                ld.dismiss();
                                try {
                                    if (response.getString("status").equals("success")) {
                                        Toast.makeText(RegisterActivity.this, "کیف پول جدید شما ایجاد شد.", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(RegisterActivity.this, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(RegisterActivity.this, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                Toast.makeText(RegisterActivity.this, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        copyPassphrase.setOnClickListener(v -> {
            MemoHero.setClipboard(getApplicationContext(), passphrase.getText().toString());
            Toast.makeText(this, "کلید خصوصی کپی شد، آن را در جایی ایمن نگهداری کنید.", Toast.LENGTH_LONG).show();
        });
    }

    private void setVisibility(View view, int visibility) {
        Transition transition = new Fade();
        if (visibility != View.INVISIBLE && visibility != View.GONE) {
            transition.setDuration(600);
        }
        transition.addTarget(view);
        TransitionManager.beginDelayedTransition((ViewGroup) view.getParent(), transition);
        view.setVisibility(visibility);
    }

    private void lockslide(View v){
        slidr.lock();
    }


    private void unlockslide(View v){
        slidr.unlock();
    }
}