package tapsi.com.data;


import tapsi.com.logging.LogHandler;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class Client {

    private int id;
    private String name;
    private String phoneID;
    private String threadID;
    private int allowed;
    private String lastConnection;


    public Client() {
    }

    public Client(List<String> data) {
        ListIterator iterator = data.listIterator();
        id = (Integer) iterator.next();
        name = (String) iterator.next();
        phoneID = (String) iterator.next();
        threadID = (String) iterator.next();
        allowed = (Integer) iterator.next();
        lastConnection = (String) iterator.next();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneID() {
        return phoneID;
    }

    public void setPhoneID(String phoneID) {
        this.phoneID = phoneID;
    }

    public String getThreadID() {
        return threadID;
    }

    public void setThreadID(String threadID) {
        this.threadID = threadID;
    }

    public int getAllowed() {
        return allowed;
    }

    public void setAllowed(int allowed) {
        this.allowed = allowed;
    }

    public String getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(String lastConnection) {
        this.lastConnection = lastConnection;
    }

    public void printData() {
        LogHandler.printLog("_id: " + id);
        LogHandler.printLog("_name: " + name);
        LogHandler.printLog("_phoneID: " + phoneID);
        LogHandler.printLog("_threadID: " + threadID);
        LogHandler.printLog("_allowed: " + allowed);
        LogHandler.printLog("_lastConnection: " + lastConnection);
    }

    public List<String> getData() {
        return Arrays.asList(Integer.toString(id), name, phoneID, threadID, Integer.toString(allowed), lastConnection);
    }
}