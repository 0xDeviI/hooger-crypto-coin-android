package com.hoogercoin.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validity {

    public static boolean isValidUsername(String username){
        Pattern p = Pattern.compile("^(?=.*[a-zA-Z])\\w{3,25}$");

        if (username == null) {
            return false;
        }
        Matcher m = p.matcher(username);
        return m.matches();
    }

    public static boolean isValidPassphrase(String passphrase){
        Pattern p = Pattern.compile("^(?=.*[a-zA-Z])\\s\\w");

        if (passphrase == null) {
            return false;
        }
        Matcher m = p.matcher(passphrase);
        return m.matches();
    }

}
