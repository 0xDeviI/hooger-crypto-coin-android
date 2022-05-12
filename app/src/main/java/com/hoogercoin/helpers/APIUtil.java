package com.hoogercoin.helpers;

public class APIUtil {
    public static final String server = "http://192.168.43.17:5432/api/v1/";
    public static final String registerAccount = server + "create_account";
    public static final String accountLogin = server + "login";
    public static final String getCurrentCoinPrice = server + "get_current_price";
    public static final String nextPayTokenGeneration = "https://nextpay.org/nx/gateway/token";
    public static final String nextPayPaymentVerification = "https://nextpay.org/nx/gateway/verify";
    public static final String nextPayWithdrawCheckout = "https://nextpay.org/nx/gateway/checkout";
    public static final String buyCoin = server + "buy_coin";
    public static final String sellCoin = server + "sell_coin";
    public static final String checkJWTToken = server + "check_jwt_token";
    public static final String getSupply = server + "get_supply";
    public static final String getTransactions = server + "get_transactions";
    public static final String savePublicKey = server + "save_public_key";
    public static final String getServerInfo = server + "get_server_info";
    public static final String getTodayPrices = server + "get_today_prices";
}
