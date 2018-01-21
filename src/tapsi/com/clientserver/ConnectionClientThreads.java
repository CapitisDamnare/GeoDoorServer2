package tapsi.com.clientserver;

import tapsi.com.logging.LogHandler;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ConnectionClientThreads implements Runnable {

    private String clientID = null;
    private Socket socket = null;

    private String line = "";

    private BufferedReader inputStream = null;
    private PrintWriter outputStream = null;

    private ClientListener listener;

    public interface ClientListener {
        public void onClientClosed(String id);

        public void onMessage(String clientID, String msg);
    }

    public String getClientID() {
        return clientID;
    }

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
        //System.out.println(new Date() + ": ClientThread " + clientID + " started ...");

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
                } else
                    inputStream.close();
            }
        } catch (IOException ex) {
            //System.out.println(new Date() + ": ClientThread stopped from client -> " + clientID);
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
                //System.out.println(new Date() + ": ClientThread stopped from client -> " + clientID);
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
            System.out.println(new Date() + ": ClientThread stopped from server -> " + clientID);
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
