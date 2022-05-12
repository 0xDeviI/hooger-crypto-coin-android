package com.hoogercoin;

import static com.hoogercoin.helpers.Browserable.openCustomTab;
import static com.hoogercoin.helpers.Graphtil.setWindowFlag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.hoogercoin.datastructs.Pointer;
import com.hoogercoin.dialogs.LoadingDialog;
import com.hoogercoin.dialogs.SimpleDialog;
import com.hoogercoin.dialogs.YesNoDialog;
import com.hoogercoin.executors.FetchTomanPerPrice;
import com.hoogercoin.helpers.APIUtil;
import com.hoogercoin.helpers.MoneyTextWatcher;
import com.hoogercoin.helpers.TypoHelper;
import com.hoogercoin.paymentgateways.NextPay;
import com.hoogercoin.payments.Paymentic;
import com.hoogercoin.session.SessionManager;
import com.hoogercoin.session.SessionValidator;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;

public class SellCoinActivity extends AppCompatActivity implements SlidrInterface {

    private SlidrInterface slidr;
    private ImageView back;
    private EditText tomanPrice;
    private EditText hoogerPrice;
    private AppCompatButton sellHooger;
    private SessionManager session;
    private TextView oneHoogerPrice;
    private boolean tomanPerHoogerCalculated = false;
    private Timer timer;
    private Pointer tomanPerHooger = new Pointer(0);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_coin);

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

        detectUIElements();
        defineUIBehaviours();

        calculateOneHoogerPrice();

        timer = new Timer();
        timer.schedule(new FetchTomanPerPrice(SellCoinActivity.this, session, tomanPerHooger, oneHoogerPrice), 5000, 5000);

    }

    private void calculateOneHoogerPrice() {
        LoadingDialog loadingDialog = new LoadingDialog(this, "", false);
        loadingDialog.show();
        AndroidNetworking.get(APIUtil.getCurrentCoinPrice)
                .addHeaders("Authorization", session.getToken())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingDialog.dismiss();
                        try {
                            if (response.getString("status").equals("success")) {
                                hoogerPrice.setText("1");
                                tomanPrice.setText(String.valueOf(response.getInt("price")));
                                tomanPerHooger.setObject(response.getInt("price"));
                                oneHoogerPrice.setText("قیمت هوگر: " + TypoHelper.persianDigitMoney(String.valueOf((Integer) tomanPerHooger.getObject())) + " تومان");
                                tomanPerHoogerCalculated = true;
                            }
                            else {
                                Toast.makeText(SellCoinActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loadingDialog.dismiss();
                        if (SessionValidator.isSessionExpired(anError.getResponse())) {
                            if (SessionValidator.fetchNewToken(SellCoinActivity.this, session)) {
                                calculateOneHoogerPrice();
                            }
                            else {
                                Toast.makeText(SellCoinActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        else {
                            Toast.makeText(SellCoinActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    private void detectUIElements() {
        back = findViewById(R.id.back);
        tomanPrice = findViewById(R.id.tomanPrice);
        hoogerPrice = findViewById(R.id.hoogerPrice);
        sellHooger = findViewById(R.id.sellHooger);
        oneHoogerPrice = findViewById(R.id.oneHoogerPrice);
    }

    private void recalculateCoinPrice() {
        try {
            if (tomanPerHoogerCalculated) {
                double price = Double.parseDouble(hoogerPrice.getText().toString());
                double toman = price * (Integer) tomanPerHooger.getObject();
                tomanPrice.setText(String.valueOf((int) toman));
            }
        }
        catch (NumberFormatException nfe) {
        }
    }

    private boolean isVerified() {
        return !session.getString("card_owner").equals("")
                && !session.getString("card_shaba").equals("");
    }

    private void sellCoin() {
        JSONObject requestParam = new JSONObject();
        try {
            requestParam.put("sender", session.getString("userId"));
            requestParam.put("hooger_price", (Integer) tomanPerHooger.getObject());
            requestParam.put("amount", hoogerPrice.getText().toString());
            LoadingDialog ld = new LoadingDialog(this, "درحال فروش هوگر کوین ...", false);
            ld.show();
            AndroidNetworking.post(APIUtil.sellCoin)
                    .addJSONObjectBody(requestParam)
                    .addHeaders("Authorization", session.getToken())
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            ld.dismiss();
                            try {
                                if (response.getString("status").equals("success")) {
                                    new SimpleDialog(SellCoinActivity.this, "فروش موفق", "هوگر کوین شما فروخته شد. مبلغ ریالی تا 48 ساعت آینده به حساب شما واریز می شود.", v -> {
                                        finish();
                                    }, false).show();
                                }
                                else {
                                    Toast.makeText(SellCoinActivity.this, "خطایی در تایید تراکنش رخ داد.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            ld.dismiss();
                            if (SessionValidator.isSessionExpired(anError.getResponse())) {
                                if (SessionValidator.fetchNewToken(SellCoinActivity.this, session)) {
                                    sellCoin();
                                }
                                else {
                                    Toast.makeText(SellCoinActivity.this, "خطایی رخ داد...", Toast.LENGTH_LONG).show();
                                }
                            }
                            else {
                                Toast.makeText(SellCoinActivity.this, "خطایی رخ داد...", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void defineUIBehaviours() {
        back.setOnClickListener(v -> {
            finish();
        });

        tomanPrice.addTextChangedListener(new MoneyTextWatcher(tomanPrice));
        hoogerPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recalculateCoinPrice();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        oneHoogerPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recalculateCoinPrice();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sellHooger.setOnClickListener(v -> {
            if (isVerified()) {
                if (hoogerPrice.getText().length() != 0 && tomanPrice.getText().length() != 0) {
                    new YesNoDialog(SellCoinActivity.this, "تایید فروش", "آیا مایل به فروش " + hoogerPrice.getText().toString() + " هوگر و دريافت " + tomanPrice.getText().toString() + " تومان هستید؟", v1 -> {
                        LoadingDialog loadingDialog = new LoadingDialog(this, "درحال اتصال به درگاه پرداخت...", false);
                        loadingDialog.show();
                        AndroidNetworking.post(APIUtil.nextPayWithdrawCheckout)
                                .addBodyParameter("api_key", NextPay.getApiKey())
                                .addBodyParameter("wid", String.valueOf(NextPay.getWithdrawNumber()))
                                .addBodyParameter("auth", NextPay.getWithdrawAuth())
                                .addBodyParameter("amount", tomanPrice.getText().toString().replace(",", ""))
                                .addBodyParameter("sheba", session.getString("card_shaba"))
                                .addBodyParameter("name", session.getString("card_owner"))
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        loadingDialog.dismiss();
                                        try {
                                            if (response.getInt("code") == 200) {
                                                sellCoin();
                                            }
                                            else if (response.getInt("code") == -500) {
                                                Toast.makeText(SellCoinActivity.this, "شماره شبا معتبر نیست.", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Toast.makeText(SellCoinActivity.this, "خطایی در فروش رخ داد...", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        loadingDialog.dismiss();
                                        Toast.makeText(SellCoinActivity.this, "خطایی در فروش رخ داد...", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }, null, true).show();
                }
            }
            else {
                new SimpleDialog(SellCoinActivity.this, "", "هویت حساب شما احراز نشده است. برای احراز هویت به تنظیمات بروید.", null, true).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}