package com.hoogercoin;

import static com.hoogercoin.helpers.Graphtil.setWindowFlag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.hoogercoin.dialogs.LoadingDialog;
import com.hoogercoin.dialogs.SimpleDialog;
import com.hoogercoin.helpers.APIUtil;
import com.hoogercoin.helpers.Validity;
import com.hoogercoin.session.SessionManager;
import com.hoogercoin.session.SessionValidator;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements SlidrInterface {

    private SlidrInterface slidr;
    private ImageView back;
    private EditText username;
    private EditText password;
    private AppCompatButton login;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        slidr = Slidr.attach(this);
        AndroidNetworking.initialize(getApplicationContext());
        session = new SessionManager(getApplicationContext());

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

        if (SessionValidator.isLoggedIn(session)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }

        detectUIElements();
        defineUIElements();
    }

    private void detectUIElements() {
        back = findViewById(R.id.back);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
    }

    private void defineUIElements() {
        back.setOnClickListener(v -> {
            finish();
        });

        login.setOnClickListener(v -> {
            if (username.getText().toString().length() == 0 || password.getText().toString().length() == 0) {
                new SimpleDialog(this, "", "نام کاربری یا کلمه عبور نباید خالی باشد.", null, true).show();
            }
            else {
                if (Validity.isValidUsername(username.getText().toString())) {
                    JSONObject requestParam = new JSONObject();
                    try {
                        requestParam.put("username", username.getText().toString());
                        requestParam.put("password", password.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    LoadingDialog ld = new LoadingDialog(this, "", false);
                    ld.show();
                    AndroidNetworking.post(APIUtil.accountLogin)
                            .addJSONObjectBody(requestParam)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    ld.dismiss();
                                    try {
                                        if (response.getString("status").equals("success")) {
                                            session.setLoggedIn(response.getJSONObject("user").getString("user_id"),
                                                    response.getJSONObject("user").getString("username"),
                                                    password.getText().toString(),
                                                    response.getString("token"));
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(LoginActivity.this, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {
                                    ld.dismiss();
                                    Toast.makeText(LoginActivity.this, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else {
                    new SimpleDialog(this, "", "فرمت نام کاربری یا کلمه عبور صحیح نیست.", null, true).show();
                }
            }
        });
    }

    @Override
    public void lock() {
        slidr.lock();
    }

    @Override
    public void unlock() {
        slidr.unlock();
    }
}