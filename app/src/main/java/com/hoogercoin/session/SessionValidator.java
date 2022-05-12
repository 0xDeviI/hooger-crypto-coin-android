package com.hoogercoin.session;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.hoogercoin.BuyCoinActivity;
import com.hoogercoin.LoginActivity;
import com.hoogercoin.MainActivity;
import com.hoogercoin.datastructs.Pointer;
import com.hoogercoin.dialogs.LoadingDialog;
import com.hoogercoin.helpers.APIUtil;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;

public class SessionValidator {
    public static boolean isLoggedIn(SessionManager session) {
        return session.isLoggedIn();
    }

    public static boolean isSessionExpired(Response serverResponse) {
        String stringResponse = serverResponse.message();
        try {
            JSONObject objectResponse = new JSONObject(stringResponse);
            return !objectResponse.isNull("msg") && objectResponse.getString("msg").equals("Token has expired");
        } catch (JSONException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static void checkToken(Activity activity, SessionManager session) {
        isSessionExpired(activity, session);
    }

    public static boolean fetchNewToken(Activity activity, SessionManager session) {
        Pointer fetchSuccess = new Pointer(false);
        JSONObject requestParam = new JSONObject();
        try {
            requestParam.put("username", session.getString("username"));
            requestParam.put("password", session.getString("password"));

            LoadingDialog ld = new LoadingDialog(activity, "درحال نوسازی توکن منقضی شده ...", false);
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
                                    session.setString("token", response.getString("token"));
                                    fetchSuccess.setObject(true);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(activity, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            ld.dismiss();
                            Toast.makeText(activity, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                        }
                    });
            return (Boolean) fetchSuccess.getObject();
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isSessionExpired(Activity activity, SessionManager session) {
        Pointer isExpired = new Pointer(false);
        LoadingDialog ld = new LoadingDialog(activity, "درحال بررسی توکن ...", false);
        ld.show();
        AndroidNetworking.post(APIUtil.checkJWTToken)
                .addHeaders("Authorization", session.getToken())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ld.dismiss();
                        try {
                            if (response.getString("status").equals("success")) {
                                isExpired.setObject(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(activity, "خطایی رخ داد...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        ld.dismiss();
                        if (SessionValidator.isSessionExpired(anError.getResponse())) {
                            if (SessionValidator.fetchNewToken(activity, session)) {
                                isExpired.setObject(isSessionExpired(activity, session));
                            }
                            else {
                                Toast.makeText(activity, "خطایی رخ داد...", Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            Toast.makeText(activity, "خطایی رخ داد...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        return (Boolean) isExpired.getObject();
    }
}
