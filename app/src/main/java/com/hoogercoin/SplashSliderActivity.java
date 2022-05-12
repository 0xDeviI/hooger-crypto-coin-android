package com.hoogercoin;

import static com.hoogercoin.helpers.Graphtil.setWindowFlag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.hoogercoin.adapters.SplashViewPagerAdapter;

public class SplashSliderActivity extends AppCompatActivity {

    ViewPager2 splash_pager;
    SplashViewPagerAdapter svpa;
    AppCompatButton next;
    AppCompatButton register;
    AppCompatButton login;
    ConstraintLayout last_btn_section;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_slider);

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

    private void detectUIElements() {
        splash_pager = findViewById(R.id.splash_pager);
        next = findViewById(R.id.next);
        register = findViewById(R.id.register);
        login = findViewById(R.id.login);
        last_btn_section = findViewById(R.id.last_btn_section);
    }

    private void defineUIBehaviours() {
        svpa = new SplashViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        splash_pager.setAdapter(svpa);


        next.setOnClickListener(v -> {
            if (splash_pager.getCurrentItem() == svpa.getItemCount() - 2){
                next.setVisibility(View.INVISIBLE);
                last_btn_section.setVisibility(View.VISIBLE);
            }
            splash_pager.setCurrentItem(splash_pager.getCurrentItem() + 1);
        });

        login.setOnClickListener(v -> {
            startActivity(new Intent(SplashSliderActivity.this, LoginActivity.class));
        });

        register.setOnClickListener(v -> {
            startActivity(new Intent(SplashSliderActivity.this, RegisterActivity.class));
        });
    }
}