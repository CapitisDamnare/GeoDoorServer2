package com.tapsi;

import java.io.IOException;
import java.sql.SQLException;

public class LogHandler {

    public static void handleError(GeoDoorExceptions ex) {
        ex.printStackTrace();
    }

    public static void handleError(InterruptedException ex) {
        ex.printStackTrace();
    }

    public static void handleError(IOException ex) {
        ex.printStackTrace();
    }

    public static void handleError(ClassNotFoundException ex) {
        ex.printStackTrace();
    }

    public static void handleError(SQLException ex) {
        ex.printStackTrace();
    }

    public static void handleError(Exception ex) {
        ex.printStackTrace();
    }

    public static void printPrompt() {
        System.out.println("");
        System.out.print("GeoDoorServer:> ");
    }
}
