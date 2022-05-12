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
import com.hoogercoin.helpers.MemoHero;
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

public class BuyCoinActivity extends AppCompatActivity implements SlidrInterface {

    private SlidrInterface slidr;
    private ImageView back;
    private EditText tomanPrice;
    private EditText hoogerPrice;
    private AppCompatButton buyHooger;
    private SessionManager session;
    private TextView oneHoogerPrice;
    private boolean tomanPerHoogerCalculated = false;
    private Timer timer;
    private Pointer tomanPerHooger = new Pointer(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coin);

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
        timer.schedule(new FetchTomanPerPrice(BuyCoinActivity.this, session, tomanPerHooger, oneHoogerPrice), 5000, 5000);

        checkDeepLinkingOperations();
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
                                Toast.makeText(BuyCoinActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
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
                            if (SessionValidator.fetchNewToken(BuyCoinActivity.this, session)) {
                                calculateOneHoogerPrice();
                            }
                            else {
                                Toast.makeText(BuyCoinActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        else {
                            Toast.makeText(BuyCoinActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    private void detectUIElements() {
        back = findViewById(R.id.back);
        tomanPrice = findViewById(R.id.tomanPrice);
        hoogerPrice = findViewById(R.id.hoogerPrice);
        buyHooger = findViewById(R.id.buyHooger);
        oneHoogerPrice = findViewById(R.id.oneHoogerPrice);
    }

    private void recalculateCoinPrice() {
        try {
            if (tomanPerHoogerCalculated) {
                int price = Integer.parseInt(tomanPrice.getText().toString().replace(",", "").replace(".", ""));
                double hooger = (double) price / (Integer) tomanPerHooger.getObject();
                hoogerPrice.setText(String.valueOf(hooger));
            }
        }
        catch (NumberFormatException nfe) {
        }
    }


    private void defineUIBehaviours() {
        back.setOnClickListener(v -> {
            finish();
        });

        tomanPrice.addTextChangedListener(new MoneyTextWatcher(tomanPrice));
        tomanPrice.addTextChangedListener(new TextWatcher() {
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

        buyHooger.setOnClickListener(v -> {
            if (hoogerPrice.getText().length() != 0 && tomanPrice.getText().length() != 0) {
                new YesNoDialog(BuyCoinActivity.this, "تایید خرید", "آیا مایل به دریافت " + hoogerPrice.getText().toString() + " هوگر با پرداخت " + tomanPrice.getText().toString() + " تومان هستید؟", v1 -> {
                    JSONObject paymentData = new JSONObject();
                    try {
                        paymentData.put("price", tomanPrice.getText().toString().replace(",", "").replace(".", ""));
                        paymentData.put("amount", hoogerPrice.getText().toString().replace(",", ""));
                        paymentData.put("hooger_price", String.valueOf((Integer) tomanPerHooger.getObject()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    LoadingDialog loadingDialog = new LoadingDialog(this, "درحال انتقال به درگاه پرداخت...", false);
                    loadingDialog.show();
                    AndroidNetworking.post(APIUtil.nextPayTokenGeneration)
                            .addBodyParameter("api_key", NextPay.getApiKey())
                            .addBodyParameter("order_id", Paymentic.generatePaymentID(10))
                            .addBodyParameter("amount", tomanPrice.getText().toString().replace(",", "").replace(".", ""))
                            .addBodyParameter("callback_uri", "huger://nextpay.org")
                            .addBodyParameter("custom_json_fields", paymentData.toString())
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    loadingDialog.dismiss();
                                    try {
                                        if (response.getInt("code") == -1) {
                                            String trans_id = response.getString("trans_id");
                                            session.setString("trans_id", trans_id);
                                            CustomTabsIntent.Builder customIntent = new CustomTabsIntent.Builder();
                                            customIntent.setToolbarColor(ContextCompat.getColor(getApplicationContext(), R.color.deprimary_button_color));
                                            openCustomTab(BuyCoinActivity.this, customIntent.build(), Uri.parse("https://nextpay.org/nx/gateway/payment/" + trans_id));
                                        }
                                        else {
                                            Toast.makeText(BuyCoinActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {
                                    loadingDialog.dismiss();
                                    Toast.makeText(BuyCoinActivity.this, anError.getErrorBody(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }, null, true).show();
            }
        });
    }

    private boolean checkDeepLinkingOperations(){
        Uri uri = getIntent().getData();
        if (uri != null) {
            MemoHero.setClipboard(getApplicationContext(), uri.toString());
            String trans_id = "";
            String order_id = "";
            String amount = "";
            String np_status = "";
            String[] fields = uri.toString().replace("huger://nextpay.org?", "").split("&");
            for (int i = 0; i < fields.length; i++) {
                String[] currentFields = fields[i].split("=");
                if (currentFields[0].equals("trans_id")) {
                    trans_id = currentFields[1];
                }
                else if (currentFields[0].equals("order_id")) {
                    order_id = currentFields[1];
                }
                else if (currentFields[0].equals("amount")) {
                    amount = currentFields[1];
                }
                else if (currentFields[0].equals("np_status")) {
                    np_status = currentFields[1];
                }
            }
            if (np_status.equals("OK")) {
                verifyPayment(trans_id, amount);
            }
        }
        return uri != null;
    }

    private void verifyPayment(String trans_id, String amount) {
        LoadingDialog loadingDialog = new LoadingDialog(this, "درحال تایید تراکنش ...", false);
        loadingDialog.show();
        AndroidNetworking.post(APIUtil.nextPayPaymentVerification)
                .addBodyParameter("api_key", NextPay.getApiKey())
                .addBodyParameter("trans_id", trans_id)
                .addBodyParameter("amount", amount)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingDialog.dismiss();
                        try {
                            if (response.getInt("code") == 0) {
                                buyCoin(response);
                            }
                            else {
                                Toast.makeText(BuyCoinActivity.this, "خطایی در تایید تراکنش رخ داد. اگر مبلغی از حساب شما کسر شده است، تا ساعات آینده عودت داده می شود.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loadingDialog.dismiss();
                        Toast.makeText(BuyCoinActivity.this, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void buyCoin(JSONObject response) {
        try {
            JSONObject requestParam = new JSONObject();
            JSONObject customData = new JSONObject(response.getString("custom"));
            requestParam.put("receiver", session.getString("userId"));
            requestParam.put("hooger_price", customData.getString("hooger_price"));
            requestParam.put("amount", customData.getString("amount"));
            LoadingDialog ld = new LoadingDialog(this, "درحال خرید هوگر کوین ...", false);
            ld.show();
            AndroidNetworking.post(APIUtil.buyCoin)
                    .addJSONObjectBody(requestParam)
                    .addHeaders("Authorization", session.getToken())
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            ld.dismiss();
                            try {
                                if (response.getString("status").equals("success")) {
                                    new SimpleDialog(BuyCoinActivity.this, "خرید موفق", "خرید هوگر کوین موفقیت آمیز بود.", v -> {
                                        finish();
                                    }, false).show();
                                }
                                else {
                                    Toast.makeText(BuyCoinActivity.this, "خطایی در تایید تراکنش رخ داد. اگر مبلغی از حساب شما کسر شده است، تا ساعات آینده عودت داده می شود.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            ld.dismiss();
                            if (SessionValidator.isSessionExpired(anError.getResponse())) {
                                if (SessionValidator.fetchNewToken(BuyCoinActivity.this, session)) {
                                    buyCoin(response);
                                }
                                else {
                                    Toast.makeText(BuyCoinActivity.this, "خطایی رخ داد...", Toast.LENGTH_LONG).show();
                                }
                            }
                            else {
                                Toast.makeText(BuyCoinActivity.this, "خطایی رخ داد...", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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