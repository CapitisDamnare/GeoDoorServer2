package tapsi.com.data;


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
        System.out.println("_id: " + id);
        System.out.println("_name: " + name);
        System.out.println("_phoneID: " + phoneID);
        System.out.println("_threadID: " + threadID);
        System.out.println("_allowed: " + allowed);
        System.out.println("_lastConnection: " + lastConnection);
    }

    public List<String> getData() {
        return Arrays.asList(Integer.toString(id), name, phoneID, threadID, Integer.toString(allowed), lastConnection);
    }
}