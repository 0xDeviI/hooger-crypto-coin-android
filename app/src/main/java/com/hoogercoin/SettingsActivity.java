package com.hoogercoin;

import static com.hoogercoin.helpers.Graphtil.setWindowFlag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.hoogercoin.dialogs.YesNoDialog;
import com.hoogercoin.session.SessionManager;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

public class SettingsActivity extends AppCompatActivity implements SlidrInterface {

    SessionManager session;
    private ImageView back;
    private SlidrInterface slidr;
    private TextView username;
    private ConstraintLayout authorize;
    private ConstraintLayout logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        slidr = Slidr.attach(this);
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

        detectUIElements();
        defineUIBehaviours();
    }

    private void detectUIElements(){
        username = findViewById(R.id.username);
        authorize = findViewById(R.id.authorize);
        logout = findViewById(R.id.logout);
        back = findViewById(R.id.back);
    }

    private void defineUIBehaviours() {

        back.setOnClickListener(v -> {
            finish();
        });

        username.setText(session.getString("username"));

        authorize.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, OwnerAuthenticateActivity.class));
        });

        logout.setOnClickListener(v -> {
            new YesNoDialog(SettingsActivity.this, "خروج از حساب", "آیا قصد خروج از حساب کاربری را دارید؟", v2 -> {
                session = session.destroySession();
                Intent intent = new Intent(SettingsActivity.this, SplashSliderActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }, null, true).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}