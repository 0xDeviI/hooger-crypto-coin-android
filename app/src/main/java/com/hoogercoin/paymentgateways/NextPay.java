package com.hoogercoin.paymentgateways;

public class NextPay {
    private final static String API_KEY = "990aa584-7f2b-42aa-a499-3e63303b8562";
    private final static String WITHDRAW_AUTH = "dfb5bc431d86ed04021f40d409656b90b484a4ab";
    private final static int WITHDRAW_NUMBER = 3207;

    public static String getApiKey() {
        return API_KEY;
    }

    public static String getWithdrawAuth() {
        return WITHDRAW_AUTH;
    }

    public static int getWithdrawNumber() {
        return WITHDRAW_NUMBER;
    }
}
