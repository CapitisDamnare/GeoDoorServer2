package com.tapsi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
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
    KNXHandler knxHandler = null;

    // Client ExecutorService for the client threads
    private final ExecutorService pool;
    private ConnectionClientThreads client;

    // Socket for the Server
    private final ServerSocket serverSocket;
    private Socket socket;

    // Thread Safe HashMap to safe and get access to the Client Threads
    private ConcurrentHashMap<String, ConnectionClientThreads> clientMap;

    // boolean to close the Thread
    private boolean close = true;

    // PingTimer for periddic connection checks
    public Timer timer = null;
    public PingTimer pingTimerTask = null;

    public ServerThread() throws IOException {
        // Create or open DB
        dbHandler = new DBHandler();

        clientMap = new ConcurrentHashMap<>();

        // Create KNX Handler and init Listener
        knxHandler = new KNXHandler();
        initKNXListener(knxHandler);

        // Create and start MessageHandler
        msgHandler = new MessageHandlerThread(dbHandler, knxHandler);
        initMessageListener(msgHandler);
        tHandlerThread = new Thread(msgHandler);
        tHandlerThread.start();

        // Start the PingTimer Thread
        startPingTimer();

        // Start the Server
        serverSocket = new ServerSocket(1234);
        pool = Executors.newFixedThreadPool(10);
        System.out.println("Server started...");
    }

    // If a new client connects to the socket a new Thread will be started for the connection
    @Override
    public void run() {
        while (close) {
            try {
                socket = serverSocket.accept();
                client = new ConnectionClientThreads(socket, socket.getRemoteSocketAddress().toString());
                initClientListener(client);
                pool.execute(client);
                clientMap.put(socket.getRemoteSocketAddress().toString(), client);

                System.out.println(new Date() + ": Thread Map Size: " + clientMap.size());
            } catch (IOException ex) {
                LogHandler.handleError(ex);
            }
        }
        System.out.println("Server stopped");
        shutdownAndAwaitTermination(pool);
    }

    // Client listener for every thread from the connected clients
    public void initClientListener(ConnectionClientThreads client) {
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

    public void initMessageListener(MessageHandlerThread messageHandler) {
        messageHandler.setCustomListener(new MessageHandlerThread.messageListener() {
            @Override
            public void onClientAnswer(String oldThreadID, String threadID, String message) {

                // old ThreadID ist still active - close and delete it
                if (clientMap.containsKey(oldThreadID)) {
                    ConnectionClientThreads mapClient = clientMap.get(oldThreadID);
                    mapClient.closeThread();
                    clientMap.remove(oldThreadID);
                    System.out.println(new Date() + ": Closed old active connection: " + oldThreadID);
                }
                sendMessageToDevice(threadID, message);
            }
        });
    }

    public void initKNXListener(KNXHandler knxHandler) {
        knxHandler.setCustomListener(new KNXHandler.KNXListener() {
            @Override
            public void onDoorStatChanged(int value) {
                for (Map.Entry<String, ConnectionClientThreads> entry : clientMap.entrySet()) {
                    ConnectionClientThreads mapClient = entry.getValue();
                    if(value == 4)
                        mapClient.sendMessage("answer:door1 open");
                    else if (value == 0)
                        mapClient.sendMessage("answer:door1 close");
                    else if (value == 2)
                        mapClient.sendMessage("answer:door1 stopped");
                }
            }

            @Override
            public void onAutomaticDoorClose() {
                try {
                    knxHandler.setItem("eg_tor","ON");
                } catch (IOException e) {
                    LogHandler.handleError(e);
                }
            }
        });
    }

    // This wil shutdown all Threads for sure if they are finished with their tasks
    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.out.println("Pool did not terminate");
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
    public void test(String value) {
        try {
            knxHandler.setItem("eg_buero",value);
        } catch (IOException e) {
            LogHandler.handleError(e);
        }
        //dbHandler.insertClient("xxx", "89014103211118510720", "0");
    }

    // Close all clientThreads, Server Thread and MessageHandler Thread
    public void quit() {
        close = false;
        try {
            serverSocket.close();
            closeClientThreads();
            msgHandler.quit();
            dbHandler.closeDB();
            knxHandler.stopTimer();
            stopPingTimer();
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }

    // Close all client Threads registered in the map
    public void closeClientThreads() {
        for (Map.Entry<String, ConnectionClientThreads> entry : clientMap.entrySet()) {
            ConnectionClientThreads mapClient = entry.getValue();
            mapClient.closeThread();
        }
    }

    // Delete a specific client Thread in the map
    public void deleteClient(String threadId) {
        if (clientMap.containsKey(threadId)) {
            clientMap.remove(threadId);
        }
        System.out.println(new Date() + ": Thread Map Size: " + clientMap.size());
    }

    // send a message to e specific client
    public void sendMessageToDevice(String threadID, String msg) {
        if (clientMap.containsKey(threadID)) {
            ConnectionClientThreads currentUser = clientMap.get(threadID);
            currentUser.sendMessage(msg);
        }
        else {
            try {
                throw new GeoDoorExceptions("null - key doesn't exist anymore");
            } catch (GeoDoorExceptions geoDoorExceptions) {
                geoDoorExceptions.printStackTrace();
            }
        }
    }

    public void broadcastMessage () {
        for (Map.Entry<String, ConnectionClientThreads> entry : clientMap.entrySet()) {
            ConnectionClientThreads mapClient = entry.getValue();
            mapClient.sendMessage("answer:ping");
        }
    }

    // Starts or stops the server
    public void startKNXTimer() {
        knxHandler.startTimer();
    }

    public void stopKNXHandler() {
        knxHandler.stopTimer();
    }

    public  void getKNXItem() {
        knxHandler.getItem("eg_tor_stat");
    }

    public void stopPingTimer () {
        timer.cancel();
    }

    public  void startPingTimer() {
        pingTimerTask = new PingTimer();
        timer = new Timer(true);
        timer.schedule(pingTimerTask,0,20000);
    }

    public class PingTimer extends TimerTask {

        @Override
        public void run() {
            broadcastMessage();
        }
    }

    public void setDoorStatus (boolean value) {
        knxHandler.setOpenDoorStatus(value);
    }

    public String getMessageQueueSize () {
        return String.valueOf(msgHandler.getQueueSize());
    }

    public void getUsers () {
        for (Map.Entry<String, ConnectionClientThreads> entry : clientMap.entrySet()) {
            ConnectionClientThreads mapClient = entry.getValue();
            String name = dbHandler.selectNameByThreadID(mapClient.getClientID());
            String lastConnection = dbHandler.selectLastConnectionByThreadID(mapClient.getClientID());
            System.out.println("Name: " + name + " - last connection at: " + lastConnection);
        }
    }

}