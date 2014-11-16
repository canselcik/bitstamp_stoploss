package com.bitcoin_payment_gateway;


import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static void l(String s){
        java.util.Date date = new java.util.Date();
        System.out.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] " + s);
    }

    public static void green(String s){
        l(ANSI_GREEN + s + ANSI_RESET);
    }

    public static void red(String s){
        l(ANSI_RED + s + ANSI_RESET);
    }

    public static void cyan(String s){
        l(ANSI_CYAN + s + ANSI_RESET);
    }

    public static void blue(String s){
        l(ANSI_BLUE + s + ANSI_RESET);
    }

    public static void yellow(String s){
        l(ANSI_YELLOW + s + ANSI_RESET);
    }
}
