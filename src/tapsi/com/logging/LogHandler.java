package tapsi.com.logging;

import sun.rmi.runtime.Log;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public class LogHandler {

    private static List<String> ArrayLog = new ArrayList<>();
    private static String Log = "";

    private static boolean debugMode = false;

    public static String getLog() {
        return Log;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean debugMode) {
        LogHandler.debugMode = debugMode;
    }

    public static void handleError(GeoDoorExceptions ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": got Exception:");
            ex.printStackTrace();
        }
    }

    public static void handleError(InterruptedException ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": got Exception:");
            ex.printStackTrace();
        }
    }

    public static void handleError(IOException ex) {
        if (debugMode) {
            //LogHandler.printLog(new Date() + ": got Exception:");
            //ex.printStackTrace();
        }
    }

    public static void handleError(ClassNotFoundException ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": got Exception:");
            ex.printStackTrace();
        }
    }

    public static void handleError(SQLException ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": got Exception:");
            ex.printStackTrace();
        }
    }

    public static void handleError(Exception ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": got Exception:");
            ex.printStackTrace();
        }
    }

    public static void printPrompt() {
        LogHandler.printLog("");
        System.out.print("GeoDoorServer:> ");
    }

    private static void updateString() {
        ListIterator iter = ArrayLog.listIterator();
        Log = "";
        while (iter.hasNext()) {
            Log += iter.next().toString() + "\n";
        }
    }

    public static void printLog(String message) {
        if (ArrayLog.size() > 200) {
            ArrayLog.remove(0);
            ArrayLog.add(message);
        }
        else {
            ArrayLog.add(message);
        }

        updateString();

        System.out.println(message);
    }
}
