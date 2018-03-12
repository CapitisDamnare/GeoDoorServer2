package tapsi.com;

import javafx.application.Application;
import tapsi.com.clientserver.ServerThread;
import tapsi.com.logging.LogHandler;
import tapsi.com.visualization.InterfaceToVisu;
import tapsi.com.visualization.Visualization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Main class of the application and handles all command line user inputs.
 */
public class GeoDoorServer2 {

    private static InterfaceToVisu inVisu = null;
    private static ServerThread serverThread = null;
    private static boolean quit = false;
    private static Visualization visu = null;

    /**
     * Main Method of the application
     *
     * @param args
     */
    public static void main(String[] args) {

        LogHandler.setDebugMode(true);

//        new Thread(() -> Application.launch(Visualization.class)).start();
//        visu = Visualization.waitForStartUpTest();
//        initVisuListener(visu);
//        visu.printSomething();
//
//        // Create instance of the server
//        inVisu = new InterfaceToVisu();

        // Console Prompt
        LogHandler.printPrompt();
        while (!quit) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            try {
                String command = bufferedReader.readLine();
                LogHandler.printCommand(command);
                checkCommand(command);
            } catch (IOException e) {
                LogHandler.handleError(e);
            } catch (InterruptedException e) {
                LogHandler.handleError(e);
            }
        }
        LogHandler.printLog(new Date() + ": Prompt closed");
    }

    // Todo: add console command: set permission
    // Todo: add setPort Command

    /**
     * Checks the command provided in the command line and calls the corresponding method
     *
     * @param command
     */
    private static void checkCommand(String command) throws InterruptedException {
        String com = command;
        String arg = "";
        if (command.contains(":")) {
            String temp = com;
            com = temp.substring(0, temp.indexOf(":"));
            temp = temp.replace(com + ":", "");
            arg = temp;
        }

        switch (com) {
            case "quit":
                setQuit(true);
                break;
            case "start":
                startServer();
                break;
            case "restart":
                restart();
                break;
            case "setServerPort":
                ServerThread.setPORT(Integer.parseInt(arg));
                restart();
                break;
            case "setVisuPort":
                ServerThread.setVisuPort(Integer.parseInt(arg));
                restart();
                break;
            case "show":
                showHideVisu(false);
                visu.hideVisualization(false);
                break;
            case "hide":
                showHideVisu(true);
                visu.hideVisualization(true);
                break;
            case "debug on":
                LogHandler.setDebugMode(true);
                break;
            case "debug off":
                LogHandler.setDebugMode(false);
                break;
            case "knx on":
                serverThread.startKNXTimer();
                break;
            case "knx off":
                serverThread.stopKNXHandler();
                break;
            case "set door open":
                serverThread.setDoorStatus(true);
                break;
            case "set door closed":
                serverThread.setDoorStatus(false);
                break;
            case "message queue":
                LogHandler.printLog(new Date() + ": Message queue Size: " + serverThread.getMessageQueueSize());
                break;
            case "help":
                LogHandler.printLog("quit                - disables the server and ends the program");
                LogHandler.printLog("start               - starts the server");
                LogHandler.printLog("restart             - restarts the server");
                LogHandler.printLog("setServerPort:xxxx  - sets the server PORT where xxxx is the PORT. Restarts automatically!");
                LogHandler.printLog("setVisuPort:xxxx    - sets the Visu PORT where xxxx is the PORT. Restarts automatically!");
                LogHandler.printLog("show                - show visualisation (not available on a raspberry PI)");
                LogHandler.printLog("hide                - hide visualisation (not available on a raspberry PI)");
                LogHandler.printLog("debug on            - enables error debug messages");
                LogHandler.printLog("debug off           - disable error debug messages");
                LogHandler.printLog("set door open       - set door to open state");
                LogHandler.printLog("set door closed     - set door to closed state");
                LogHandler.printLog("message queue       - gets the current message queue size");
                break;
            default:
                LogHandler.handleError("Command not found");
        }
    }

    /**
     * Not usable in a raspberry PI3 application
     * Custom listener for a JavaFx visualization
     *
     * @param visu
     */
    static void initVisuListener(Visualization visu) {
        visu.setCustomListener(new Visualization.VisListener() {
            @Override
            public void onStart() {
                LogHandler.printLog("got onStart!");
            }
        });
    }

    /**
     * Command to start the server. Starts the server thread.
     *
     */
    public static void startServer() {
        try {
            serverThread = new ServerThread();
            Thread tServerThread = new Thread(serverThread);
            tServerThread.start();
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }

    /**
     * Command to stop the server. Ends the server thread.
     */
    public static void stopServer() {
        if (serverThread != null)
            serverThread.quit();
        else
            LogHandler.handleError("Can't stop not started server!");
    }

    /**
     * Command to quit the application and calls stopServer.
     *
     * @param val
     */
    private static void setQuit(boolean val) {
        //visu.closeVisualization();
        //inVisu.closeVisualisation();
        stopServer();
        quit = val;
    }

    public static void restart() {
        stopServer();
        startServer();
    }

    /**
     * @param val
     * @Version Windows
     * Not usable in a raspberry PI3 application.
     * Hides the JavaFX visualization.
     */
    private static void showHideVisu(boolean val) {
        inVisu.hideVisualization(val);
    }
}
