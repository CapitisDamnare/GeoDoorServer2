package com.tapsi;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

@DatabaseTable(tableName = "Clients")
public class Client {

    @DatabaseField(generatedId = true, unique = true)
    private int id;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false, unique = true)
    private String phoneID;
    @DatabaseField(unique = true)
    private String threadID;
    @DatabaseField(defaultValue = "0")
    private int allowed;
    @DatabaseField
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