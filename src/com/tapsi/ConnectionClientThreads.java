package com.tapsi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionClientThreads implements Runnable {

    private int clientID = 0;
    private Socket socket = null;

    private String line = "";

    private BufferedReader inputStream = null;
    private PrintWriter outputStream = null;

    private ClientListener listener;

    public interface ClientListener {
        public void onClientClosed(int id);
        // Todo: Send id from connection to ensure only connection with the same name
        public void onMessage (String msg);
    }

    public ConnectionClientThreads(Socket socket, int clientID) {
        this.socket = socket;
        this.clientID = clientID;
        this.listener = null;
    }

    public void setCustomListener(ClientListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        System.err.println("");
        System.err.println("ClientThread " + clientID + " started ...");

        try {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintWriter(socket.getOutputStream());
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }

        try {
            while (true) {
                line = inputStream.readLine();
                System.err.println(clientID + " -> " + line);
                listener.onMessage(line);
                if (line.compareTo("stop") == 0) {

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
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                System.err.println("\nClientThread stopped from client");
                listener.onClientClosed(clientID);
                socket.close();
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
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            System.err.println("\nClientThread stopped from Server");
            LogHandler.printPrompt();
            socket.close();
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }

    public void sendMessage(String text) {
        outputStream.println(text);
        outputStream.flush();
    }
}
