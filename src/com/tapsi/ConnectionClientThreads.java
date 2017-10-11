package com.tapsi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Todo: Add send status door signal after successful connection to the server

public class ConnectionClientThreads implements Runnable {

    private String clientID = null;
    private Socket socket = null;

    private String line = "";

    private BufferedReader inputStream = null;
    private PrintWriter outputStream = null;

    private ClientListener listener;

    public interface ClientListener {
        public void onClientClosed(String id);
        public void onMessage (String clientID, String msg);
    }

    // Implement Listener to send messages to the server thread
    public ConnectionClientThreads(Socket socket, String clientID) {
        this.socket = socket;
        this.clientID = clientID;
        this.listener = null;
    }

    public void setCustomListener(ClientListener listener) {
        this.listener = listener;
    }

    // Create a new input and output stream and wait for incoming messages
    @Override
    public void run() {
        System.out.println("");
        System.out.println("ClientThread " + socket.getRemoteSocketAddress().toString() + " started ...");

        try {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintWriter(socket.getOutputStream());
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }

        try {
            while (true) {
                line = inputStream.readLine();

                if (line != null) {
                    listener.onMessage(clientID, line);
                }
                else
                    inputStream.close();
            }
        } catch (IOException ex) {
            System.out.println("\nClientThread stopped from client");
            LogHandler.handleError(ex);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                listener.onClientClosed(clientID);
                socket.close();
            } catch (IOException ex) {
                System.out.println("\nClientThread stopped from client");
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
            System.out.println("\nClientThread stopped from Server");
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
