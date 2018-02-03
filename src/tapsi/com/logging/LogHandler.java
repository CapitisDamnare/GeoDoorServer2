package tapsi.com.logging;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public class LogHandler {

    private static List<String> ArrayLog = new ArrayList<>();
    private static String Log = "";

    private static boolean debugMode = false;

    private static StringWriter sw = new StringWriter();
    private static PrintWriter pw = new PrintWriter(sw);


    public static String getLog() {
        return Log;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean debugMode) {
        LogHandler.debugMode = debugMode;
    }

    public static void handleError(String ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": Exception! --> GeoDoorExceptions:");
            LogHandler.printLog(ex);
        }
    }

    public static void handleError(InterruptedException ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": Exception! --> InterruptedException:");
            ex.printStackTrace(pw);
            String exceptionAsString = sw.toString();
            LogHandler.printLog(exceptionAsString);
        }
    }

    public static void handleError(IOException ex) {
        if (debugMode) {
//            LogHandler.printLog(new Date() + ": Exception! --> IOException:");
//            ex.printStackTrace(pw);
//            String exceptionAsString = sw.toString();
//            LogHandler.printLog(exceptionAsString);
        }
    }

    public static void handleError(ClassNotFoundException ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": Exception! --> ClassNotFoundException:");
            ex.printStackTrace(pw);
            String exceptionAsString = sw.toString();
            LogHandler.printLog(exceptionAsString);
        }
    }

    public static void handleError(SQLException ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": Exception! --> SQLException:");
            ex.printStackTrace(pw);
            String exceptionAsString = sw.toString();
            LogHandler.printLog(exceptionAsString);
        }
    }

    public static void handleError(StringIndexOutOfBoundsException ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": Exception! --> StringIndexOutOfBoundsException:");
            ex.printStackTrace(pw);
            String exceptionAsString = sw.toString();
            LogHandler.printLog(exceptionAsString);
        }
    }

    public static void handleError(XMLStreamException ex) {
        if (debugMode) {
            LogHandler.printLog(new Date() + ": Exception! --> XMLStreamException:");
            ex.printStackTrace(pw);
            String exceptionAsString = sw.toString();
            LogHandler.printLog(exceptionAsString);
        }
    }

    public static void printPrompt() {
        System.out.print("GeoDoorServer:> ");
    }

    public static void printCommand(String message) {
        if (ArrayLog.size() > 200) {
            ArrayLog.remove(0);
            ArrayLog.add("GeoDoorServer:> " + message);
        }
        else {
            ArrayLog.add("GeoDoorServer:> " + message);
        }

        updateString();
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
