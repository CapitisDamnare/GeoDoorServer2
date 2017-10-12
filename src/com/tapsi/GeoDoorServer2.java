package com.tapsi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GeoDoorServer2 {
    
    private static InterfaceToVisu inVisu = null;
    private static ServerThread serverThread= null;
    private static boolean quit = false;

    public static void main(String[] args) {

        LogHandler.setDebugMode(true);

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
    // Todo: add command to check connected users
    // Todo: add command to set debug mode on or off
    // Todo: add command to check message queue
    // Console Commands
    private static void checkCommand(String command) throws GeoDoorExceptions{
        switch (command) {
            case "quit":
                setQuit(true);
                break;
            case "show":
                showHideVisu(true);
                break;
            case "hide":
                showHideVisu(false);
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
