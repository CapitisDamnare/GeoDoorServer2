package com.tapsi;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageHandlerThread implements Runnable {

    BlockingQueue<String> queue;
    private boolean close = true;
    DBHandler dbHandler = null;

    private messageListener listener;

    public interface messageListener {
        public void onClientAnswer(String threadID, String message);
    }

    public void setCustomListener(messageListener listener) {
        this.listener = listener;
    }

    public MessageHandlerThread(DBHandler dbHandler) {
        this.dbHandler = dbHandler;
        queue = new ArrayBlockingQueue<String>(1024);
    }

    // put a message from client Thread in the queue (That's thread safe!)
    public void putMessage(String msg) {
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            LogHandler.handleError(e);
        }
    }

    // Check permanently the queue for new messages to handle
    @Override
    public void run() {
        System.err.println("MessagHandlerThread started ...");
        while (close) {
            if (queue.size() > 0) {
                try {
                    String message = queue.take();
                    System.err.println("Message Queue Size: " + queue.size());
                    System.err.println(" Took message: " + message);

                    String threadID = null;
                    String command = null;
                    if (message.contains("#")) {
                        threadID = message.substring(0, message.indexOf("#"));
                        message = message.replace(threadID + "#", "");
                        command = message.substring(0, message.indexOf(":"));
                        message = message.replace(command + ":", "");

                        switch (command) {
                            case "register":
                                commandRegister(threadID, message);
                                break;
                            default:
                                throw new GeoDoorExceptions("Sent socket command doesn't exist");
                        }
                    } else
                        throw new GeoDoorExceptions("No identifier found!");

                } catch (InterruptedException e) {
                    LogHandler.handleError(e);
                } catch (GeoDoorExceptions geoDoorExceptions) {
                    LogHandler.handleError(geoDoorExceptions);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LogHandler.handleError(e);
            }
        }
    }

    // Todo: Add command handling for different commands
    // Todo: Add commandOutput "output:" to open and close gates "output:Gate1 open"
    // Todo: Check if user is allowed to send commands (If not don't answer)

    void commandRegister(String threadID, String message) {

        String messageTemp = message;
        String name = messageTemp.substring(0, messageTemp.indexOf("-"));
        messageTemp = messageTemp.replace(name + "-", "");

        String phoneId = messageTemp;

        boolean checkPhoneID = dbHandler.checkClientByPhoneID(phoneId);
        boolean checkAllowed = dbHandler.checkAllowedByPhoneID(phoneId);

        if (checkPhoneID) {
            if (!checkAllowed) {
                listener.onClientAnswer(threadID, "answer:not yet allowed");
            }
            else
                listener.onClientAnswer(threadID, "answer:allowed");
        } else {
            listener.onClientAnswer(threadID, "answer:registered ... waiting for permission");
        }
        // Will automatically insert or update
        dbHandler.insertClient(name, phoneId, threadID);
    }

    // Close the thread
    public void quit() {
        close = false;
    }
}
