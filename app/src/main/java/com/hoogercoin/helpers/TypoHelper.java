package com.hoogercoin.helpers;

public class TypoHelper {
    public static boolean isInt(String str){
        try{
            int x = Integer.parseInt(str);
            return true;
        }
        catch (Exception ex){
            return false;
        }
    }

    private static String[] persianNumbers = new String[]{ "۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹" };

    public static String persianDigitMoney(String str){
        return toPersianDigit(new MoneyTextWatcher(null).formatStaticMoney(str));
    }

    public static String toPersianDigit(String text) {
        if (text == null){
            return null;
        }
        if (text.length() == 0) {
            return "";
        }
        String out = "";
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if ('0' <= c && c <= '9') {
                int number = Integer.parseInt(String.valueOf(c));
                out += persianNumbers[number];
            } else if (c == '٫') {
                out += '،';
            } else {
                out += c;
            }
        }
        return out;
    }
}
