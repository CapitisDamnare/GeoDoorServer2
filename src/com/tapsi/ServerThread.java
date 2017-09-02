package com.tapsi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerThread implements Runnable {

    private final ServerSocket serverSocket;
    private Socket socket;
    private final ExecutorService pool;
    private MessageHandlerThread msgHandler = null;
    private static Thread tHandlerThread = null;
    private ConnectionClientThreads client;
    private ConcurrentHashMap<Integer,ConnectionClientThreads> clientMap;
    private int threadIter;

    private boolean close = true;

    public ServerThread() throws IOException {
        threadIter = 0;
        msgHandler = new MessageHandlerThread();
        tHandlerThread = new Thread(msgHandler);
        tHandlerThread.start();
        clientMap = new ConcurrentHashMap<>();
        serverSocket = new ServerSocket(1234);
        pool = Executors.newFixedThreadPool(10);
        System.out.println("Server started...");
    }

    @Override
    public void run() {
        while(close) {
            try {
                // If a new socket connection comes a ClienThread will be created
                socket = serverSocket.accept();
                client = new ConnectionClientThreads(socket, threadIter);
                pool.execute(client);
                clientMap.put(threadIter, client);
                initClientListener(client);
                threadIter++;
                System.err.println("Size: " + clientMap.size());
            } catch (IOException ex) {
                LogHandler.handleError(ex);
            }
        }
        System.err.println("Server stopped");
        shutdownAndAwaitTermination(pool);
    }

    public void initClientListener (ConnectionClientThreads client) {
        client.setCustomListener(new ConnectionClientThreads.ClientListener() {
            @Override
            public void onClientClosed(int id) {
                deleteClient(id);
            }

            @Override
            public void onMessage(String msg) {
                msgHandler.putMessage(msg);
            }
        });
    }

    // Todo: Implement method to send a message to a specific thread or connected device

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

    public void test() {
        System.err.println("Shut down client");
        client.closeThread();
    }

    public void quit() {
        close = false;
        try {
            serverSocket.close();
            closeClientThreads();
        } catch (IOException ex) {
            LogHandler.handleError(ex);
        }
    }

    public void closeClientThreads () {
        for (Map.Entry<Integer,ConnectionClientThreads> entry : clientMap.entrySet() ) {
            ConnectionClientThreads mapClient = entry.getValue();
            mapClient.closeThread();
        }
    }

    public void  deleteClient (int threadId) {
        if(clientMap.containsKey(threadId)) {
            clientMap.remove(threadId);
        }
        threadIter--;
        System.err.println("Size: " + clientMap.size());
    }
}