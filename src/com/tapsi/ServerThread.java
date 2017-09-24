package com.tapsi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerThread implements Runnable {

    // Message Handler for incoming messages
    private MessageHandlerThread msgHandler = null;
    private Thread tHandlerThread = null;

    // Database Handler to save Names and allowed usage of the KNX Handler
    DBHandler dbHandler = null;

    // KNXHandler to connect to the OpenHAB API
    // Todo: Create KNXHandler

    // Client ExecutorService for the client threads
    private final ExecutorService pool;
    private ConnectionClientThreads client;

    // Socket for the Server
    private final ServerSocket serverSocket;
    private Socket socket;

    // Thread Safe HashMap to safe and get access to the Client Threads
    private ConcurrentHashMap<String,ConnectionClientThreads> clientMap;

    // boolean to close the Thread
    private boolean close = true;

    public ServerThread() throws IOException {
        // Create or open DB
        dbHandler = new DBHandler();

        clientMap = new ConcurrentHashMap<>();

        // Create and start MessageHandler
        msgHandler = new MessageHandlerThread(dbHandler);
        tHandlerThread = new Thread(msgHandler);
        tHandlerThread.start();

        // Start the Server
        serverSocket = new ServerSocket(1234);
        pool = Executors.newFixedThreadPool(10);
        System.out.println("Server started...");
    }

    // If a new client connects to the socket a new Thread will be started for the connection
    @Override
    public void run() {
        while(close) {
            try {
                socket = serverSocket.accept();
                client = new ConnectionClientThreads(socket, socket.getRemoteSocketAddress().toString());
                pool.execute(client);
                clientMap.put(socket.getRemoteSocketAddress().toString(), client);
                initClientListener(client);

                System.err.println("Thread Map Size: " + clientMap.size());
            } catch (IOException ex) {
                LogHandler.handleError(ex);
            }
        }
        System.err.println("Server stopped");
        shutdownAndAwaitTermination(pool);
    }

    // Client listener for every thread from the connected clients
    public void initClientListener (ConnectionClientThreads client) {
        client.setCustomListener(new ConnectionClientThreads.ClientListener() {
            @Override
            public void onClientClosed(String id) {
                deleteClient(id);
            }

            @Override
            public void onMessage(String clientID, String msg) {
                msgHandler.putMessage(clientID + "#" + msg);
            }
        });
    }

    // Todo: Implement method to send a message to a specific thread or connected device

    // This wil shutdown all Threads for sure if they are finished with their tasks
    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException ex) {
            // (Re-)Cancel if current thread also interrupted
            LogHandler.handleError(ex);
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    // Just a small test method.
    // Todo: Delete if not necessary anymore
    public void test() {
        dbHandler.insertClient("Andreas2","89014103211118510720", "0");
    }

    // Close all clientThreads, Server Thread and MessageHandler Thread
    public void quit() {
        close = false;
        try {
            serverSocket.close();
            closeClientThreads();
            msgHandler.quit();
            dbHandler.closeDB();
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }

    // Close all client Threads registered in the map
    public void closeClientThreads () {
        for (Map.Entry<String,ConnectionClientThreads> entry : clientMap.entrySet() ) {
            ConnectionClientThreads mapClient = entry.getValue();
            mapClient.closeThread();
        }
    }

    // Delete a specific client Thread in the map
    public void  deleteClient (String threadId) {
        if(clientMap.containsKey(threadId)) {
            clientMap.remove(threadId);
        }
    }

    // send a message to e specific client
    public void sendMessagetoDevice (String sAddresse, String msg) {
        ConnectionClientThreads currentUser = clientMap.get(sAddresse);
        currentUser.sendMessage(msg);
    }
}