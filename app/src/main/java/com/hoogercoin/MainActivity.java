package com.hoogercoin;

import static com.hoogercoin.session.SessionValidator.checkToken;
import static com.hoogercoin.helpers.Graphtil.setWindowFlag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.hoogercoin.adapters.TransactionAdapter;
import com.hoogercoin.dialogs.ImagePreviewDialog;
import com.hoogercoin.dialogs.LoadingDialog;
import com.hoogercoin.dialogs.SimpleDialog;
import com.hoogercoin.fragments.bottomsheet.SwapFragment;
import com.hoogercoin.helpers.APIUtil;
import com.hoogercoin.helpers.MemoHero;
import com.hoogercoin.helpers.TypoHelper;
import com.hoogercoin.models.Transaction;
import com.hoogercoin.session.SessionManager;
import com.hoogercoin.session.SessionValidator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private TextView publicAdress;
    private SessionManager session;
    private ImageView menuSwap;
    private ImageView menuSettings;
    private ImageView priceChartMenu;
    private CircleImageView userProfile;
    private ImageView copyPublicAddress;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView mySupply;
    private TextView username;
    private RecyclerView transactionsView;
    private ArrayList<Transaction> transactions = new ArrayList<>();
    private TransactionAdapter adapter;
    private TextView noTs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        loadingOperations();
    }

    private void loadingOperations() {
        checkToken(this, session);
        getMySupply();
        getMyTransactions();
    }

    private void getMyTransactions() {
        LoadingDialog loadingDialog = new LoadingDialog(this, "", false);
        loadingDialog.show();
        transactions.clear();
        AndroidNetworking.get(APIUtil.getTransactions)
                .addHeaders("Authorization", session.getToken())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingDialog.dismiss();
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray fetchedTransactions = response.getJSONArray("transactions");
                                for (int i = 0; i < fetchedTransactions.length(); i++) {
                                    transactions.add(new Transaction(
                                            fetchedTransactions.getJSONObject(i).getString("sender"),
                                            fetchedTransactions.getJSONObject(i).getString("receiver"),
                                            fetchedTransactions.getJSONObject(i).getString("ts_id"),
                                            fetchedTransactions.getJSONObject(i).getString("ts_time"),
                                            fetchedTransactions.getJSONObject(i).getInt("ts_timestamp"),
                                            fetchedTransactions.getJSONObject(i).getString("ts_type").equals("buy_coin") ? Transaction.TransactionType.BUY_COIN : Transaction.TransactionType.SELL_COIN,
                                            fetchedTransactions.getJSONObject(i).getDouble("hooger_price"),
                                            fetchedTransactions.getJSONObject(i).getDouble("amount")
                                    ));
                                    adapter.notifyDataSetChanged();
                                }
                                if (transactions.size() > 0) {
                                    noTs.setVisibility(View.GONE);
                                    transactionsView.setVisibility(View.VISIBLE);
                                }
                                else {
                                    noTs.setVisibility(View.VISIBLE);
                                    transactionsView.setVisibility(View.GONE);
                                }
                            }
                            else {
                                Toast.makeText(MainActivity.this, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
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
                            if (SessionValidator.fetchNewToken(MainActivity.this, session)) {
                                getMyTransactions();
                            }
                            else {
                                Toast.makeText(MainActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public String getCorrectStyleSupply(String supply){
        String[] octets = supply.split("\\.");
        return octets[0] + "." + (octets[1].length() <= 8 ? octets[1] : octets[1].substring(0, 8));
    }


    private void getMySupply() {
        LoadingDialog loadingDialog = new LoadingDialog(this, "", false);
        loadingDialog.show();
        AndroidNetworking.get(APIUtil.getSupply)
                .addHeaders("Authorization", session.getToken())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingDialog.dismiss();
                        try {
                            if (response.getString("status").equals("success")) {
                                mySupply.setText(TypoHelper.toPersianDigit(getCorrectStyleSupply(String.valueOf(response.getDouble("supply")))));
                            }
                            else {
                                Toast.makeText(MainActivity.this, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
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
                            if (SessionValidator.fetchNewToken(MainActivity.this, session)) {
                                getMySupply();
                            }
                            else {
                                Toast.makeText(MainActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void detectUIElements() {
        publicAdress = findViewById(R.id.publicAdress);
        menuSwap = findViewById(R.id.menu_swap);
        menuSettings = findViewById(R.id.menu_settings);
        priceChartMenu = findViewById(R.id.price_chart_menu);
        userProfile = findViewById(R.id.userProfile);
        copyPublicAddress = findViewById(R.id.copyPublicAddress);
        swipeRefreshLayout = findViewById(R.id.dashboardRefresh);
        mySupply = findViewById(R.id.mySupply);
        username = findViewById(R.id.username);
        transactionsView = findViewById(R.id.transactionsView);
        adapter = new TransactionAdapter(getApplicationContext(), transactions);
        transactionsView.setAdapter(adapter);
        transactionsView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        noTs = findViewById(R.id.noTs);
    }

    private void defineUIBehaviours() {
        noTs.setVisibility(View.GONE);

        publicAdress.setText(session.getString("userId"));
        publicAdress.setOnClickListener(v -> {
            new SimpleDialog(this, "آدرس کیف پول شما", session.getString("userId"), null, true).show();
        });


        menuSwap.setOnClickListener(v -> {
            SwapFragment swapFragment = new SwapFragment();
            swapFragment.show(getSupportFragmentManager(), "SwapFragment");
        });

        menuSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        priceChartMenu.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PriceTrackActivity.class));
        });

        userProfile.setOnClickListener(v -> {
            new ImagePreviewDialog(this, userProfile.getDrawable(), true).show();
        });

        copyPublicAddress.setOnClickListener(v -> {
            MemoHero.setClipboard(getApplicationContext(), session.getString("userId"));
            Toast.makeText(this, "آدرس کیف پول کپی شد.", Toast.LENGTH_SHORT).show();
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadingOperations();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        username.setText(session.getString("username"));
    }
}