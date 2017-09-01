/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tapsi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeoDoorServer2 {
    
    private static InterfaceToVisu inVisu = null;
    private static boolean quit = false;
    private static ServerThread serverThread= null;
    private static Thread tServerThread = null;

    /** 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        GeoDoorServer2 server = new GeoDoorServer2();
        inVisu = new InterfaceToVisu(server);
        
        // Console
        while (!quit) {
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(System.in));
            LogHandler.printPrompt();
            String command = bufreader.readLine();
            try {
                checkCommand(command);
            } catch (GeoDoorExceptions ex) {
                LogHandler.handleError(ex);
            }          
        }
    }
    
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
            case "test":
                serverThread.test();
                break;
            default:
                throw new GeoDoorExceptions("Command not found");
        }
    }

    public static void startServer() throws InterruptedException, IOException {
        serverThread = new ServerThread();
        tServerThread = new Thread(serverThread);
        tServerThread.start();
    }
    
    public static void stopServer() throws GeoDoorExceptions {
        if (serverThread != null)
            serverThread.quit();
        else
            throw new GeoDoorExceptions("Can't stop not started server!");
    }
    
    private static void setQuit(boolean val) {
        try {
            inVisu.closeVisualisation();
            stopServer();
            quit = val;
        } catch (GeoDoorExceptions ex) {
            LogHandler.handleError(ex);
        }
    }
    
    private static void showHideVisu(boolean val) {
        inVisu.showVisualization(val);
    }
}
