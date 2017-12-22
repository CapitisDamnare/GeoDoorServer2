package com.tapsi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

public class GeoDoorServer2 {

    private static InterfaceToVisu inVisu = null;
    private static ServerThread serverThread = null;
    private static boolean quit = false;
    private static Visualization visu = null;

    public static void main(String[] args) {

        LogHandler.setDebugMode(true);

        new Thread() {
            @Override
            public void run() {
                javafx.application.Application.launch(Visualization.class);
            }
        }.start();
        visu = Visualization.waitForStartUpTest();
        initVisuListener(visu);
        visu.printSomething();

        // Create instance of the server
        inVisu = new InterfaceToVisu();

        // Console Prompt
        while (!quit) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            LogHandler.printPrompt();
            try {
                String command = bufferedReader.readLine();
                checkCommand(command);
            } catch (GeoDoorExceptions ex) {
                LogHandler.handleError(ex);
            } catch (IOException e) {
                LogHandler.handleError(e);
            }
        }
    }

    // Todo: add console command to list and give permission to user
    // Console Commands
    private static void checkCommand(String command) throws GeoDoorExceptions {
        switch (command) {
            case "quit":
                setQuit(true);
                break;
            case "show":
                //showHideVisu(true);
                visu.hideVisualization(false);
                break;
            case "hide":
                //showHideVisu(false);
                visu.hideVisualization(true);
                break;
            case "start": {
                try {
                    startServer();
                } catch (InterruptedException ex) {
                    LogHandler.handleError(ex);
                } catch (IOException ex) {
                    LogHandler.handleError(ex);
                }
                break;
            }
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
                System.out.println(new Date() + ": Message queue Size: " + serverThread.getMessageQueueSize());
                break;
            case "connected users":
                System.out.println(new Date() + ": Connected users:\n");
                serverThread.getUsers();
                break;
            case "help":
                System.out.println("quit                - disables the server and ends the program");
                System.out.println("show                - show visualisation");
                System.out.println("hide                - hide visualisation");
                System.out.println("start               - starts the server");
                System.out.println("debug on            - enables error debug messages");
                System.out.println("debug off           - disable error debug messages");
                System.out.println("knx on              - enables knx thread (door status)");
                System.out.println("knx off             - disable knx thread (door status)");
                System.out.println("set door open       - set door to open state");
                System.out.println("set door closed     - set door to closed state");
                System.out.println("message queue       - gets the current message queue size");
                System.out.println("connected users     - shows a list of the current connected users");
                break;

            // Test commands delete later
            case "knx get":
                serverThread.getKNXItem();
                break;
            case "test1":
                serverThread.test("ON");
                break;
            case "test2":
                serverThread.test("OFF");
                break;
            default:
                throw new GeoDoorExceptions("Command not found");
        }
    }

    static void initVisuListener (Visualization visu) {
        visu.setCustomListener(new Visualization.VisListener() {
            @Override
            public void onStart() {
                System.out.println("got onStart!");
            }
        });
    }

    static void startServer() throws InterruptedException, IOException {
        serverThread = new ServerThread();
        Thread tServerThread = new Thread(serverThread);
        tServerThread.start();
    }

    static void stopServer() throws GeoDoorExceptions {
        if (serverThread != null)
            serverThread.quit();
        else
            throw new GeoDoorExceptions("Can't stop not started server!");
    }

    private static void setQuit(boolean val) {
        try {
            visu.closeVisualization();
            inVisu.closeVisualisation();
            stopServer();
        } catch (GeoDoorExceptions ex) {
            LogHandler.handleError(ex);
        }
        quit = val;
    }

    private static void showHideVisu(boolean val) {
        inVisu.showVisualization(val);
    }
}
