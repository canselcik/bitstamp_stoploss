package com.bitcoin_payment_gateway;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static void l(String s){
        java.util.Date date= new java.util.Date();
        System.out.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] " + s);

    }
}
