package tapsi.com.clientserver;

import tapsi.com.data.MessageHandlerThread;
import tapsi.com.database.DBHandler;
import tapsi.com.knx.KNXHandler;
import tapsi.com.logging.LogHandler;
import tapsi.com.visuserver.VisuServerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ServerThread implements Runnable {

    private static final int PORT = 1234;

    // Gate Status
    private String gateStatus = "answer:door1 closed";

    // Message Handler for incoming messages
    private MessageHandlerThread msgHandler = null;
    private Thread tHandlerThread = null;

    // Database Handler to save Names and allowed usage of the KNX Handler
    DBHandler dbHandler = null;

    // KNXHandler to connect to the OpenHAB API
    KNXHandler knxHandler = null;

    // Start the Visu Server Thread for all Visu Connections
    private VisuServerThread visuServerThread = null;
    private Thread tVisuServerThread = null;

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

        // Start the VisuServer
        visuServerThread = new VisuServerThread(dbHandler, msgHandler);
        tVisuServerThread = new Thread(visuServerThread);
        tVisuServerThread.start();

        // Start the Server
        serverSocket = new ServerSocket(PORT);
        pool = Executors.newFixedThreadPool(10);
        LogHandler.printLog(new Date() + ": Client Server started...");
    }

    public static int getPORT() {
        return PORT;
    }

    // If a new client connects to the socket a new Thread will be started for the connection
    @Override
    public void run() {
        while (close) {
            try {
                socket = serverSocket.accept();
                socket.setSoTimeout(5000);
                client = new ConnectionClientThreads(socket, socket.getRemoteSocketAddress().toString());
                initClientListener(client);
                pool.execute(client);
                clientMap.put(socket.getRemoteSocketAddress().toString(), client);

                //LogHandler.printLog(new Date() + ": Thread Map Size: " + clientMap.size());
            } catch (IOException ex) {
                LogHandler.handleError(ex);
            }
        }
        LogHandler.printLog(new Date() + ": Server stopped");
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
                msgHandler.putMessage(PORT + "#" + clientID + "#" + msg);
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
                    LogHandler.printLog(new Date() + ": Closed old active connection: " + oldThreadID);
                }
                if (message.equals("Gate1 status")) {
                    knxHandler.getDoorStatus();
                    sendMessageToDevice(threadID, gateStatus);
                } else
                    sendMessageToDevice(threadID, message);
            }

            @Override
            public void onVisuAnswer(String oldThreadID, String threadID, String message) {
                visuServerThread.sendVisuMessageToDevice(oldThreadID, threadID, message);
            }

            @Override
            public void onSendAllClients(String oldThreadID, String threadID, String message) {
                visuServerThread.sendVisuObjectToDevice(oldThreadID, threadID, message);
            }

            @Override
            public void onSafeClient(String oldThreadID, String threadID, String message) {
                visuServerThread.safeClient(oldThreadID, threadID, message);
            }

            @Override
            public void onSendLog(String oldThreadID, String threadID, String message) {
                visuServerThread.sendVisuMessageToDevice(oldThreadID, threadID, message);
            }
        });
    }

    public void initKNXListener(KNXHandler knxHandler) {
        knxHandler.setCustomListener(new KNXHandler.KNXListener() {
            @Override
            public void onDoorStatChanged(int value) {
                for (Map.Entry<String, ConnectionClientThreads> entry : clientMap.entrySet()) {
                    ConnectionClientThreads mapClient = entry.getValue();
                    if (value == 4) {
                        gateStatus = "answer:door1 open";
                        mapClient.sendMessage("answer:door1 open");
                    } else if (value == 0) {
                        gateStatus = "answer:door1 closed";
                        mapClient.sendMessage("answer:door1 closed");
                    } else if (value == 2) {
                        gateStatus = "answer:door1 stopped";
                        mapClient.sendMessage("answer:door1 stopped");
                    }
                }
            }

            @Override
            public void onAutomaticDoorClose() {
                try {
                    knxHandler.setItem("eg_tor", "ON");
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
                    LogHandler.printLog(new Date() + ": Pool did not terminate");
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

    // Close all clientThreads, Server Thread and MessageHandler Thread
    public void quit() {
        close = false;
        try {
            serverSocket.close();
            closeClientThreads();
            msgHandler.quit();
            dbHandler.closeDB();
            knxHandler.stopTimer();
            visuServerThread.closeVisuClientThreads();
            //stopPingTimer();
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
        //LogHandler.printLog(new Date() + ": Thread Map Size: " + clientMap.size());
    }

    // send a message to e specific client
    public void sendMessageToDevice(String threadID, String msg) {
        //LogHandler.printLog(new Date() + ": Sending Message '" + msg + "' to device -> " + threadID);
        if (clientMap.containsKey(threadID)) {
            ConnectionClientThreads currentUser = clientMap.get(threadID);
            currentUser.sendMessage(msg);
        } else {
            LogHandler.handleError("null - key doesn't exist anymore");
        }
    }

    public void broadcastMessage() {
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

    public void getKNXItem() {
        knxHandler.getItem("eg_tor_stat");
    }

    public void setDoorStatus(boolean value) {
        knxHandler.setOpenDoorStatus(value);
    }

    public String getMessageQueueSize() {
        return String.valueOf(msgHandler.getQueueSize());
    }

    public void getUsers() {
        for (Map.Entry<String, ConnectionClientThreads> entry : clientMap.entrySet()) {
            ConnectionClientThreads mapClient = entry.getValue();
            String name = dbHandler.selectNameByThreadID(mapClient.getClientID());
            String lastConnection = dbHandler.selectLastConnectionByThreadID(mapClient.getClientID());
            LogHandler.printLog("Name: " + name + " - last connection at: " + lastConnection);
        }
    }
}