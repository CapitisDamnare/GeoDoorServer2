package com.tapsi;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageHandlerThread implements Runnable {

    BlockingQueue<String> queue;
    private boolean close = true;
    DBHandler dbHandler = null;

    public MessageHandlerThread(DBHandler dbHandler) {
        this.dbHandler = dbHandler;
        queue = new ArrayBlockingQueue<String>(1024);
    }

    // put a message from client Thread in the queue (That's thread safe!)
    public void putMessage (String msg) {
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
            if(queue.size() > 0) {
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
                    }
                    else
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

    void commandRegister(String threadID, String message) {

        String messageTemp = message;
        String name = messageTemp.substring(0, messageTemp.indexOf("-"));
        messageTemp = messageTemp.replace(name + "-", "");

        String phoneId = messageTemp;

        boolean checkPhoneID = dbHandler.checkClientByPhoneID(phoneId);
        boolean checkName = dbHandler.checkClientByName(name);
        boolean checkAllowed = dbHandler.checkAllowedByPhoneID(phoneId);

        if (checkPhoneID) {
            if (checkName) {
                if (!checkAllowed) {
                    System.out.println("phoneID and Name exists in DB");
                    // Todo: send "not yet allowed"
                    System.out.println("not yet allowed");
                }
            }
            else {
                // Todo: Update Name with new one
                System.out.println("update new Name");
                if (!checkAllowed) {
                    // Todo: send "not yet allowed"
                    System.out.println("not yet allowed2");
                }
            }
        }
        else {
            // Todo: Create a new DB entry, send "registered wait for allowance" back
            System.out.println("new DB entry");
        }
    }

    // Close the thread
    public void quit() {
        close = false;
    }
}
