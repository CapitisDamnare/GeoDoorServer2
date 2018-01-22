package tapsi.com.visuserver;

import tapsi.com.data.Client;
import tapsi.com.data.MessageHandlerThread;
import tapsi.com.data.XMLReader;
import tapsi.com.data.XMLWriter;
import tapsi.com.database.DBHandler;
import tapsi.com.logging.GeoDoorExceptions;
import tapsi.com.logging.LogHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VisuServerThread implements Runnable {

    private static final int PORT = 5678;

    // Temp Variable ... i know bad design i guess
    Map<String,List<String>> tempClient;

    // Message Handler for incoming messages
    private MessageHandlerThread msgHandler = null;

    // Database Handler to save Names and allowed usage of the KNX Handler
    DBHandler dbHandler = null;

    // Client ExecutorService for the client threads
    private final ExecutorService visuPool;
    private VisuClientThread visuClient;

    // Socket for the Server
    private final ServerSocket visuServerSocket;
    private Socket visuSocket;

    // Thread Safe HashMap to safe and get access to the Client Threads
    private ConcurrentHashMap<String, VisuClientThread> visuClientMap;

    // boolean to close the Thread
    private boolean close = true;

    public VisuServerThread(DBHandler dbHandler, MessageHandlerThread msgHandler) throws IOException {
        // Create or open DB
        this.dbHandler = dbHandler;
        this.msgHandler = msgHandler;

        visuClientMap = new ConcurrentHashMap<>();

        // Start the Server
        visuServerSocket = new ServerSocket(PORT);
        visuPool = Executors.newFixedThreadPool(10);
        new XMLWriter(dbHandler);
        List<Client> clients = dbHandler.readAllObjects();
        if (clients != null) {
            VisuSocketObject visuSocketObject = new VisuSocketObject(clients,"whatever");
        } else {
            VisuSocketObject visuSocketObject = new VisuSocketObject("no Clients");
        }
        try {
            XMLWriter.saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(new Date() + ": Visu Server started...");
    }

    public static int getPORT() {
        return PORT;
    }

    // If a new client connects to the socket a new Thread will be started for the connection
    @Override
    public void run() {
        while (close) {
            try {
                visuSocket = visuServerSocket.accept();
                visuSocket.setSoTimeout(5000);
                visuClient = new VisuClientThread(visuSocket, visuSocket.getRemoteSocketAddress().toString());
                initVisuClientListener(visuClient);
                visuPool.execute(visuClient);
                visuClientMap.put(visuSocket.getRemoteSocketAddress().toString(), visuClient);

                //System.out.println(new Date() + ": Visu Thread Map Size: " + visuClientMap.size());
            } catch (IOException ex) {
                LogHandler.handleError(ex);
            }
        }
        System.out.println(new Date() + ": Visu Server stopped");
        shutdownAndAwaitTermination(visuPool);
    }

    // Client listener for every thread from the connected clients
    public void initVisuClientListener(VisuClientThread client) {
        client.setCustomListener(new VisuClientThread.VisuClientListener() {
            @Override
            public void onVisuClientClosed(String id) {
                deleteVisuClient(id);
            }

            @Override
            public void onVisuMessage(String clientID, String msg) {
                String messageTemp = msg;
                String command = messageTemp.substring(0, messageTemp.indexOf("!"));
                messageTemp = messageTemp.replace(command + "!", "");

                String value = messageTemp;
                msgHandler.putMessage( PORT + "#" + clientID + "#" + command + "#" + value);
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
                    System.out.println(new Date() + ": Visu Pool did not terminate");
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
            visuServerSocket.close();
            closeVisuClientThreads();
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }

    // Close all client Threads registered in the map
    public void closeVisuClientThreads() {
        for (Map.Entry<String, VisuClientThread> entry : visuClientMap.entrySet()) {
            VisuClientThread mapClient = entry.getValue();
            mapClient.closeThread();
        }
    }

    // Delete a specific client Thread in the map
    public void deleteVisuClient(String threadId) {
        if (visuClientMap.containsKey(threadId)) {
            visuClientMap.remove(threadId);
        }
        //System.out.println(new Date() + ": Visu thread Map Size: " + visuClientMap.size());
    }

    public void sendVisuMessageToDevice(String oldThreadID, String threadID, String msg) {
        // old ThreadID ist still active - close and delete it
        if (visuClientMap.containsKey(oldThreadID)) {
            VisuClientThread mapClient = visuClientMap.get(oldThreadID);
            mapClient.closeThread();
            visuClientMap.remove(oldThreadID);
            System.out.println(new Date() + ": Closed old active visu connection: " + oldThreadID);
        } else
            sendMessageToDevice(threadID, msg);
    }

    public void sendVisuObjectToDevice(String oldThreadID, String threadID, String msg) {
        String socketOutput;
        List<Client> clients = dbHandler.readAllObjects();
        if (clients != null) {
            VisuSocketObject visuSocketObject = new VisuSocketObject(clients,msg);
            socketOutput = visuSocketObject.getContainer();
        } else {
            VisuSocketObject visuSocketObject = new VisuSocketObject("no Clients");
            socketOutput = visuSocketObject.getContainer();
        }

        // old ThreadID ist still active - close and delete it
        if (visuClientMap.containsKey(oldThreadID)) {
            VisuClientThread mapClient = visuClientMap.get(oldThreadID);
            mapClient.closeThread();
            visuClientMap.remove(oldThreadID);
            System.out.println(new Date() + ": Closed old active visu connection: " + oldThreadID);
        } else
            sendObjectToDevice(threadID, socketOutput);
    }

    // send a message to e specific client
    private void sendMessageToDevice(String threadID, String msg) {
        System.out.println(new Date() + ": Sending Visu Message '" + msg +"' to device -> " + threadID);
        if (visuClientMap.containsKey(threadID)) {
            VisuClientThread currentUser = visuClientMap.get(threadID);
            currentUser.sendMessage(msg);
        } else {
            try {
                throw new GeoDoorExceptions("null - key doesn't exist anymore");
            } catch (GeoDoorExceptions geoDoorExceptions) {
                geoDoorExceptions.printStackTrace();
            }
        }
    }

    // send a message to e specific client
    private void sendObjectToDevice(String threadID, String msg) {
        System.out.println(new Date() + ": Sending Object Message to device -> " + threadID);
        if (visuClientMap.containsKey(threadID)) {
            VisuClientThread currentUser = visuClientMap.get(threadID);
            currentUser.sendObject(msg);
        } else {
            try {
                throw new GeoDoorExceptions("null - key doesn't exist anymore");
            } catch (GeoDoorExceptions geoDoorExceptions) {
                geoDoorExceptions.printStackTrace();
            }
        }
    }

    public String getMessageQueueSize() {
        return String.valueOf(msgHandler.getQueueSize());
    }

    public void getVisuUsers() {
        for (Map.Entry<String, VisuClientThread> entry : visuClientMap.entrySet()) {
            VisuClientThread mapClient = entry.getValue();
            String name = dbHandler.selectNameByThreadID(mapClient.getClientID());
            String lastConnection = dbHandler.selectLastConnectionByThreadID(mapClient.getClientID());
            System.out.println("Name: " + name + " - last connection at: " + lastConnection);
        }
    }

    public void safeClient(String oldThreadID, String threadID, String message) {

        List<Client> clients = XMLReader.readConfig(message);

        ListIterator<Client> listIterator = clients.listIterator();

        while (listIterator.hasNext()) {
            Client client = listIterator.next();
            dbHandler.changeClientValues(client.getName(), client.getPhoneID(), client.getAllowed());
        }

        // old ThreadID ist still active - close and delete it
        if (visuClientMap.containsKey(oldThreadID)) {
            VisuClientThread mapClient = visuClientMap.get(oldThreadID);
            mapClient.closeThread();
            visuClientMap.remove(oldThreadID);
            System.out.println(new Date() + ": Closed old active visu connection: " + oldThreadID);
        } else
            sendMessageToDevice(threadID, "answer:ok");

    }
}
