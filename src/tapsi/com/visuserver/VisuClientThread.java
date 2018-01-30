package tapsi.com.visuserver;

import tapsi.com.logging.LogHandler;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class VisuClientThread implements Runnable {
    private String clientID = null;
    private Socket socket = null;

    private String line = "";

//    private BufferedReader inputStream = null;
    //private PrintWriter outputStream = null;

    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    private VisuClientThread.VisuClientListener listener;

    public interface VisuClientListener {
        public void onVisuClientClosed(String id);
        public void onVisuMessage (String clientID, String msg);
    }

    public String getClientID() {
        return clientID;
    }

    // Implement Listener to send messages to the server thread
    public VisuClientThread(Socket socket, String clientID) {
        this.socket = socket;
        this.clientID = clientID;
        this.listener = null;
    }

    public void setCustomListener(VisuClientListener listener) {
        this.listener = listener;
    }

    // Create a new input and output stream and wait for incoming messages
    @Override
    public void run() {
        //LogHandler.printLog(new Date() + ": VisuClientThread " + clientID + " started ...");
        String socketinputObject;

        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            //inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //outputStream = new PrintWriter(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }

        try {
            while ((socketinputObject = (String) objectInputStream.readObject()) != null) {
                listener.onVisuMessage(clientID, socketinputObject);
            }
        } catch (IOException ex) {
            //LogHandler.printLog(new Date() + ": VisuClientThread stopped from client -> " + clientID);
            LogHandler.handleError(ex);
        } catch (ClassNotFoundException ex) {
            LogHandler.handleError(ex);
        } finally {
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                listener.onVisuClientClosed(clientID);
                socket.close();
            } catch (IOException ex) {
                //LogHandler.printLog(new Date() + ": VisuClientThread stopped from client -> " + clientID);
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
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            LogHandler.printLog(new Date() + ": VisuClientThread stopped from server -> " + clientID);
            LogHandler.printPrompt();
            socket.close();
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }

    public void sendMessage(String text) {
        try {
            objectOutputStream.writeObject(text);
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }

    public void sendObject(String msg) {
        try {
            objectOutputStream.writeObject(msg);
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }
}
