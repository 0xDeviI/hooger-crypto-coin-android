package com.hoogercoin;

import static com.hoogercoin.helpers.Graphtil.setWindowFlag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.hoogercoin.datastructs.Pointer;
import com.hoogercoin.dialogs.LoadingDialog;
import com.hoogercoin.helpers.APIUtil;
import com.hoogercoin.helpers.TypoHelper;
import com.hoogercoin.session.SessionManager;
import com.hoogercoin.session.SessionValidator;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class PriceTrackActivity extends AppCompatActivity implements SlidrInterface {

    SessionManager session;
    private ImageView back;
    private SlidrInterface slidr;
    private LineChart lineChart;
    JSONArray prices;
    private Pointer tomanPerHooger = new Pointer(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_track);

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
        calculateOneHoogerPrice();
    }

    private void getTodayPrices(){
        AndroidNetworking.get(APIUtil.getTodayPrices)
                .addHeaders("Authorization", session.getToken())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            prices = response.getJSONArray("prices");
                            drawLineChart();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (SessionValidator.isSessionExpired(anError.getResponse())) {
                            if (SessionValidator.fetchNewToken(PriceTrackActivity.this, session)) {
                                getTodayPrices();
                            }
                        }
                    }
                });
    }

    private ArrayList<Double> getPricesValue() {
        ArrayList<Double> values = new ArrayList<>();
        values.add(0d);
        for (int i = 0; i < prices.length(); i++) {
            try {
                values.add(prices.getJSONObject(i).getDouble("price"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return values;
    }

    private int findPriceIndex(ArrayList<Double> _prices, double price){
        for (int i = 0;i < _prices.size(); i++)
        {
            if (_prices.get(i) == price)
                return i;
        }
        return -1;
    }

    private float findXAxis(int hour, int min) {
        double minInHundred = (min * 100 / 60);
        minInHundred = minInHundred / 100;
        return Float.parseFloat(hour + minInHundred + "");
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
                                tomanPerHooger.setObject(response.getInt("price"));
                                getTodayPrices();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loadingDialog.dismiss();
                        if (SessionValidator.isSessionExpired(anError.getResponse())) {
                            if (SessionValidator.fetchNewToken(PriceTrackActivity.this, session)) {
                                calculateOneHoogerPrice();
                            }
                            else {
                                Toast.makeText(PriceTrackActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        else {
                            Toast.makeText(PriceTrackActivity.this, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    private List<Entry> getDataSet(ArrayList<Double> _prices) {
        List<Entry> lineEntries = new ArrayList<>();
        for (int i = 0; i < prices.length(); i++) {
            try {
                String[] dateOctets = prices.getJSONObject(i).getString("ts_time").split(" ");
                String time = dateOctets[1].split("\\.")[0];
                int hour = Integer.valueOf(time.split(":")[0]);
                int minute = Integer.valueOf(time.split(":")[1]);
                Log.i("SMTHING", "getDataSet: " + findXAxis(hour, minute) + ", " + findPriceIndex(_prices, prices.getJSONObject(i).getDouble("price")));
                lineEntries.add(new Entry(Float.parseFloat(findXAxis(hour, minute) + ""), findPriceIndex(_prices, prices.getJSONObject(i).getDouble("price"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        Log.i("SMTHING", "getDataSet: " + (int) tomanPerHooger.getObject());
        lineEntries.add(new Entry(Float.parseFloat(findXAxis(hour, min) + ""), findPriceIndex(_prices, (int) tomanPerHooger.getObject())));
        return lineEntries;
    }

    private void drawLineChart() {
        ArrayList<Double> _prices = getPricesValue();
        List<Entry> lineEntries = getDataSet(_prices);
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "هوگر");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setLineWidth(3);
        lineDataSet.setDrawValues(false);
        lineDataSet.setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary_button_color_light));
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleHoleRadius(3);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(16);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(lineDataSet);
        lineChart.getDescription().setTextSize(12);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateY(1000);
        lineChart.setData(lineData);

        // Setup X Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1.0f);
        xAxis.setXOffset(1f);
        xAxis.setLabelCount(25);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(24);

        // Setup Y Axis
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(_prices.size());
        yAxis.setGranularity(1f);

        ArrayList<String> yAxisLabel = new ArrayList<>();
        for (int i = 0;i < _prices.size(); i++) {
            if (!yAxisLabel.contains(String.valueOf(_prices.get(i))))
                yAxisLabel.add(String.valueOf(_prices.get(i)));
        }

        lineChart.getAxisLeft().setCenterAxisLabels(true);
        lineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if(value == -1 || value >= yAxisLabel.size()) return "";
                return yAxisLabel.get((int) value);
            }
        });

        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getXAxis().setTextColor(Color.WHITE);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.invalidate();
    }


    private void detectUIElements(){
        back = findViewById(R.id.back);
        lineChart = findViewById(R.id.price_chart);
    }

    private void defineUIBehaviours() {
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