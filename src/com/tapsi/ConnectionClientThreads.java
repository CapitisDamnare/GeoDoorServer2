/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tapsi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author a.tappler
 */
public class ConnectionClientThreads implements Runnable {

    private String line = "";
    private Socket socket = null;
    private BufferedReader inputStream = null;
    private PrintWriter outputStream = null;

    public ConnectionClientThreads(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("\nClientThread started...");
        LogHandler.printPrompt();

        try {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintWriter(socket.getOutputStream());
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }

        try {
            while (true) {
                line = inputStream.readLine();
                System.out.println(line);
                if (line.compareTo("stop") == 0) {
                    //System.err.println("what");
                    sendMessage("warum?");
                }
                if (line.contains("cmnd:")) {
                    if (line.compareTo("cmnd:Andreas") == 0) {
                        sendMessage("cmnd-name:true");
                    }
                    else
                        sendMessage("cmnd-name:false");

                }
            }
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        } finally {
            try {
                if (inputStream != null) {
                    System.out.println("Input Stream Closed");
                    inputStream.close();
                }
                if (outputStream != null) {
                    System.out.println("Output Stream Closed");
                    outputStream.close();
                }
                //if (socket == null) {
                System.out.println("ClientThread stopped from client");
                socket.close();
                //}
            } catch (IOException ex) {
                LogHandler.handleError(ex);
            }
        }

    }

    public void closeThread() {
        try {
            sendMessage("quit");
            socket.close();
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
        try {
            if (inputStream != null) {
                System.out.println("Input Stream Closed");
                inputStream.close();
            }
            if (outputStream != null) {
                System.out.println("Output Stream Closed");
                outputStream.close();
            }
            //if (socket != null) {
            System.out.println("ClientThread stopped from Server");
            socket.close();
            //}
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }

    public void sendMessage(String text) {
        outputStream.println(text);
        outputStream.flush();
    }

}
