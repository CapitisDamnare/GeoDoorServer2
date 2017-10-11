package com.tapsi;

import java.io.IOException;
import java.sql.SQLException;

public class LogHandler {

    private static boolean debugMode = false;

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean debugMode) {
        LogHandler.debugMode = debugMode;
    }

    public static void handleError(GeoDoorExceptions ex) {
        if (debugMode)
            ex.printStackTrace();
    }

    public static void handleError(InterruptedException ex) {
        if (debugMode)
            ex.printStackTrace();
    }

    public static void handleError(IOException ex) {
        if (debugMode)
            ex.printStackTrace();
    }

    public static void handleError(ClassNotFoundException ex) {
        if (debugMode)
            ex.printStackTrace();
    }

    public static void handleError(SQLException ex) {
        if (debugMode)
            ex.printStackTrace();
    }

    public static void handleError(Exception ex) {
        if (debugMode)
            ex.printStackTrace();
    }

    public static void printPrompt() {
        System.out.println("");
        System.out.print("GeoDoorServer:> ");
    }
}
