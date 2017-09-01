/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tapsi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author a.tappler
 */
public class ServerThread implements Runnable {

    private final ServerSocket serverSocket;
    private Socket socket;
    private final ExecutorService pool;
    private ClientHandler clientHandler;
    private ConnectionClientThreads client;
    private HashMap<Integer,ConnectionClientThreads> clientMap;
    private int threadIter;

    private boolean close = true;

    public ServerThread() throws IOException {
        threadIter = 0;
        clientMap = new HashMap<>();
        serverSocket = new ServerSocket(1234);
        pool = Executors.newFixedThreadPool(10);
        System.out.println("\nServer started...");
        LogHandler.printPrompt();
    }

    @Override
    public void run() {
        while(close) {
            try {
                socket = serverSocket.accept();
                client = new ConnectionClientThreads(socket);
                pool.execute(client);
                clientMap.put(threadIter, client);
                threadIter++;
                System.err.println("Size: " + clientMap.size());
            } catch (IOException ex) {
                LogHandler.handleError(ex);
            }
        }
        System.err.println("\nServer stopped");
        shutdownAndAwaitTermination(pool);
    }

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
}

