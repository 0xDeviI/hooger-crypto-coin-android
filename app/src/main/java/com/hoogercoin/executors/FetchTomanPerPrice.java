package com.hoogercoin.executors;

import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.hoogercoin.BuyCoinActivity;
import com.hoogercoin.datastructs.Pointer;
import com.hoogercoin.dialogs.LoadingDialog;
import com.hoogercoin.helpers.APIUtil;
import com.hoogercoin.helpers.TypoHelper;
import com.hoogercoin.session.SessionManager;
import com.hoogercoin.session.SessionValidator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimerTask;

public class FetchTomanPerPrice extends TimerTask {

    private Activity activity;
    private SessionManager session;
    private Pointer pointer;
    private TextView oneHoogerPrice;

    public FetchTomanPerPrice(Activity activity, SessionManager sessionManager, Pointer pointer, TextView oneHoogerPrice){
        this.activity = activity;
        this.session = sessionManager;
        this.pointer = pointer;
        this.oneHoogerPrice = oneHoogerPrice;
    }

    @Override
    public void run() {
        int oldPrice = (Integer) pointer.getObject();
        AndroidNetworking.get(APIUtil.getCurrentCoinPrice)
                .addHeaders("Authorization", session.getToken())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                int newPrice = response.getInt("price");
                                if (oldPrice != newPrice) {
                                    pointer.setObject(response.getInt("price"));
                                    oneHoogerPrice.setText("قیمت هوگر: " + TypoHelper.persianDigitMoney(String.valueOf(pointer.getObject())) + " تومان");
                                }
                            }
                            else {
                                Toast.makeText(activity, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                                activity.finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (SessionValidator.isSessionExpired(anError.getResponse())) {
                            if (!SessionValidator.fetchNewToken(activity, session)) {
                                Toast.makeText(activity, "خطایی در ارتباط با سرور رخ داد...", Toast.LENGTH_SHORT).show();
                                activity.finish();
                            }
                        }
                    }
                });
    }
}
