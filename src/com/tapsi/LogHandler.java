package com.tapsi;

import java.io.IOException;

public class LogHandler {

    public static void handleError(GeoDoorExceptions ex) {
        ex.printStackTrace();
    }

    static void handleError(InterruptedException ex) {
        ex.printStackTrace();
    }

    static void handleError(IOException ex) {
        ex.printStackTrace();
    }

    public static void printPrompt() {
        System.out.println("");
        System.out.print("GeoDoorServer:> ");
    }
}
