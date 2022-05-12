package com.hoogercoin;

import static com.hoogercoin.helpers.Graphtil.setWindowFlag;
import static com.hoogercoin.helpers.IRBank.isValidBankShabaId;

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

import com.hoogercoin.dialogs.YesNoDialog;
import com.hoogercoin.session.SessionManager;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

public class OwnerAuthenticateActivity extends AppCompatActivity implements SlidrInterface {

    SessionManager session;
    private ImageView back;
    private SlidrInterface slidr;
    private EditText owner;
    private EditText shaba;
    private AppCompatButton saveChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_authenticate);

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
        back = findViewById(R.id.back);
        owner = findViewById(R.id.ownerName);
        shaba = findViewById(R.id.shaba);
        saveChanges = findViewById(R.id.saveChanges);

        saveChanges.setOnClickListener(v -> {
            if (owner.getText().length() != 0 && shaba.getText().length() != 0) {
                session.setString("card_owner", owner.getText().toString());
                session.setString("card_shaba", shaba.getText().toString());
                Toast.makeText(this, "اطلاعات ثبت شد.", Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                Toast.makeText(this, "اطلاعات را کامل کنید.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void defineUIBehaviours() {

        owner.setText(session.getString("card_owner"));
        shaba.setText(session.getString("card_shaba"));

        back.setOnClickListener(v -> {
            finish();
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