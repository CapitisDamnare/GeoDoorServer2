package tapsi.com.data;

import tapsi.com.database.DBHandler;
import tapsi.com.knx.KNXHandler;
import tapsi.com.logging.LogHandler;
import tapsi.com.visuserver.VisuServerThread;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageHandlerThread implements Runnable {

    BlockingQueue<String> queue;
    private boolean close = true;
    DBHandler dbHandler = null;
    KNXHandler knxHandler = null;

    private messageListener listener;

    public interface messageListener {
        void onClientAnswer(String oldThreadID, String threadID, String message);

        void onVisuAnswer(String oldThreadID, String threadID, String message);

        void onSendAllClients(String oldThreadID, String threadID, String message);

        void onSafeClient(String oldThreadID, String threadID, String message);

        void onSendLog(String oldThreadID, String threadID, String message);
    }

    public void setCustomListener(messageListener listener) {
        this.listener = listener;
    }

    public MessageHandlerThread(DBHandler dbHandler, KNXHandler knxHandler) {
        this.dbHandler = dbHandler;
        this.knxHandler = knxHandler;
        queue = new ArrayBlockingQueue<String>(1024);
    }

    public int getQueueSize() {
        return queue.size();
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
        LogHandler.printLog(new Date() + ": MessagHandlerThread started ...");
        while (close) {
            if (queue.size() > 0) {
                try {
                    String message = queue.take();
                    String original = message;

                    String port = null;
                    String threadID = null;
                    String command = null;
                    if (message.contains("#")) {
                        port = message.substring(0, message.indexOf("#"));
                        message = message.replace(port + "#", "");
                        threadID = message.substring(0, message.indexOf("#"));
                        message = message.replace(threadID + "#", "");
                        command = message.substring(0, message.indexOf(":"));
                        message = message.replace(command + ":", "");

                        switch (command) {
                            case "register":
                                LogHandler.printLog(new Date() + ": Took message: " + original);
                                commandRegister(threadID, message, port);
                                break;
                            case "visuRegister":
                                LogHandler.printLog(new Date() + ": Took message: " + original);
                                commandVisuRegister(threadID, message, port);
                                break;
                            case "output":
                                commandOutput(threadID, message, port);
                                break;
                            case "server":
                                LogHandler.printLog(new Date() + ": Took server message: " + original);
                                commandVisuServer(threadID, message, port);
                                break;
                            case "update":
                                LogHandler.printLog(new Date() + ": Got update message: ");
                                updateVisuServer(threadID, message, port);
                                break;
                            case "pong":
                                //LogHandler.printLog(new Date() + ": Took message: " + original);
                                break;
                            default:
                                LogHandler.handleError("Sent socket command doesn't exist: " + original);
                        }
                    } else
                        LogHandler.handleError("No identifier found!");
                } catch (StringIndexOutOfBoundsException e) {
                    LogHandler.handleError(e);
                } catch (InterruptedException e) {
                    LogHandler.handleError(e);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

                LogHandler.handleError(e);
                run();
            }
        }
    }

    // TODO: Uncomment for Release Version
    private void commandOutput(String threadID, String message, String port) {
        String messageTemp = message;
        String msg = messageTemp.substring(0, messageTemp.indexOf("-"));
        messageTemp = messageTemp.replace(msg + "-", "");

        String phoneId = messageTemp;

        boolean checkPhoneID = dbHandler.checkClientByPhoneID(phoneId);
        boolean checkAllowed = dbHandler.checkAllowedByPhoneID(phoneId);

        String oldThreadID = dbHandler.selectThreadIDByPhoneID(phoneId);

        if (checkPhoneID)
            if (checkAllowed) {
                switch (msg) {
                    case "Gate1 open":
                        LogHandler.printLog(new Date() + ": Took message: " + message);
                        listener.onClientAnswer(oldThreadID, threadID, "answer:got Message");
                        try {
                            knxHandler.setItem("eg_tor", "ON");
                        } catch (IOException e) {
                            LogHandler.handleError(e);
                        }
                        break;
                    case "Gate1 open auto":
                        LogHandler.printLog(new Date() + ": Took message: " + message);
                        listener.onClientAnswer(oldThreadID, threadID, "answer:got Message");
                        try {
                            knxHandler.setItem("eg_tor", "ON");
                            knxHandler.startAutoModeTimer();
                        } catch (IOException e) {
                            LogHandler.handleError(e);
                        }
                        break;
                    case "Door1 open":
                        LogHandler.printLog(new Date() + ": Took message: " + message);
                        listener.onClientAnswer(oldThreadID, threadID, "answer:got Message");
                        try {
                            knxHandler.setItem("eg_tuer", "ON");
                        } catch (IOException e) {
                            LogHandler.handleError(e);
                        }
                        break;
                    case "Gate1 status":
                        listener.onClientAnswer(oldThreadID, threadID, "Gate1 status");
                        break;
                    default:
                        LogHandler.handleError("Output command doesn't exist");
                }
            }
    }

    private void commandRegister(String threadID, String message, String port) {

        String messageTemp = message;
        String name = messageTemp.substring(0, messageTemp.indexOf("-"));
        messageTemp = messageTemp.replace(name + "-", "");

        String phoneId = messageTemp;

        boolean checkPhoneID = dbHandler.checkClientByPhoneID(phoneId);
        boolean checkAllowed = dbHandler.checkAllowedByPhoneID(phoneId);

        String oldThreadID = dbHandler.selectThreadIDByName(name);

        if (phoneId.equals("13579") || port.equals(Integer.toString(VisuServerThread.getPORT())))
            return;
        if (checkPhoneID) {
            if (!checkAllowed) {
                listener.onClientAnswer(oldThreadID, threadID, "answer:not yet allowed");
            } else {
                listener.onClientAnswer(oldThreadID, threadID, "answer:allowed");
                //knxHandler.getDoorStatus();
            }
        } else {
            listener.onClientAnswer(oldThreadID, threadID, "answer:registered ... waiting for permission");
        }
        // Will automatically insert or update
        dbHandler.insertClient(name, phoneId, threadID);
    }

    private void commandVisuRegister(String threadID, String message, String port) {

        String messageTemp = message;
        String name = messageTemp.substring(0, messageTemp.indexOf("-"));
        messageTemp = messageTemp.replace(name + "-", "");
        messageTemp = messageTemp.replace("#", "");

        String phoneId = messageTemp;

        boolean checkPhoneID = dbHandler.checkClientByPhoneID(phoneId);
        boolean checkAllowed = dbHandler.checkAllowedByPhoneID(phoneId);

        String oldThreadID = dbHandler.selectThreadIDByName(name);

        if (phoneId.equals("13579") && port.equals(Integer.toString(VisuServerThread.getPORT()))) {
            if (checkPhoneID) {
                if (!checkAllowed) {
                    listener.onVisuAnswer(oldThreadID, threadID, "answer:not yet allowed!");
                } else {
                    listener.onVisuAnswer(oldThreadID, threadID, "answer:allowed!");
                }
            } else {
                listener.onVisuAnswer(oldThreadID, threadID, "answer:registered ... waiting for permission!");
            }
        }
        // Will automatically insert or update
        dbHandler.insertClient(name, phoneId, threadID);
    }

    private void commandVisuServer(String threadID, String message, String port) {

        String messageTemp = message;
        String command = messageTemp.substring(0, messageTemp.indexOf(":"));
        messageTemp = messageTemp.replace(command + ":", "");
        String name = messageTemp.substring(0, messageTemp.indexOf("-"));
        messageTemp = messageTemp.replace(name + "-", "");
        messageTemp = messageTemp.replace("#", "");

        String phoneId = messageTemp;

        boolean checkPhoneID = dbHandler.checkClientByPhoneID(phoneId);
        boolean checkAllowed = dbHandler.checkAllowedByPhoneID(phoneId);

        String oldThreadID = dbHandler.selectThreadIDByName(name);

        if (phoneId.equals("13579") && port.equals(Integer.toString(VisuServerThread.getPORT()))) {
            if (checkPhoneID) {
                if (checkAllowed) {
                    switch (command) {
                        case "clients":
                            listener.onSendAllClients(oldThreadID, threadID, "answer:clients");
                            break;
                        case "log":
                            listener.onSendLog(oldThreadID, threadID, "answer:log!" + LogHandler.getLog());
                            break;
                    }
                }
            }
        }
    }

    private void updateVisuServer(String threadID, String message, String port) {

        String messageTemp = message;
        String name = messageTemp.substring(0, messageTemp.indexOf("-"));
        messageTemp = messageTemp.replace(name + "-", "");
        String phoneId = messageTemp.substring(0, messageTemp.indexOf("#"));
        messageTemp = messageTemp.replace(phoneId + "#", "");

        String data = messageTemp;


        boolean checkPhoneID = dbHandler.checkClientByPhoneID(phoneId);
        boolean checkAllowed = dbHandler.checkAllowedByPhoneID(phoneId);

        String oldThreadID = dbHandler.selectThreadIDByName(name);

        if (phoneId.equals("13579") && port.equals(Integer.toString(VisuServerThread.getPORT()))) {
            if (checkPhoneID) {
                if (checkAllowed) {
                    listener.onSafeClient(oldThreadID, threadID, data);
                }
            }
        }
    }

    // Close the thread
    public void quit() {
        close = false;
    }
}
