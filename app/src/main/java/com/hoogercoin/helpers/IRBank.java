package com.hoogercoin.helpers;

public class IRBank {
    public static Boolean isValidBankShabaId(String shabaId){
        return shabaId.matches("^(?:IR)(?=.{24}$)[0-9]*$");
    }
}
