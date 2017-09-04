package com.tapsi;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHandler {
    private Connection c = null;
    private Statement stmt = null;

    DBHandler() {

        // Connect to database or create if no existent
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:my.db");
        } catch (ClassNotFoundException e) {
            LogHandler.handleError(e);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
        System.err.println("Connected to DB!");

        // Create table if not existent
        try {
            stmt = c.createStatement();
            String sql = "create table if not exists Clients " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "name TEXT NOT NULL UNIQUE, threadID INTEGER NOT NULL UNIQUE, allowed INTEGER DEFAULT 0)";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
        //insertClient("Andreas",1);
    }

    public void insertClient(String name, int threadID) {
        insertClient(name, threadID, 0);
    }

    public void insertClient(String name, int threadID, int allowed) {
        String sql = "insert into Clients(name, threadID, allowed)" +
                "select '" + name + "', " + threadID + ", " + allowed;
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
        updateClient(name, threadID, allowed);
    }

    public void updateClient(String name, int threadId, int allowed) {
        String sqlName = "update Clients set threadID = " + threadId + ", allowed = " + allowed + " where name = '" + name + "'";
        try {
            stmt.executeUpdate(sqlName);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
        String sqlThreadID = "update Clients set name = '" + name + "', allowed = " + allowed + " where threadID = " + threadId;
        try {
            stmt.executeUpdate(sqlThreadID);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
    }

    public void closeDB() {
        try {
            stmt.close();
            c.close();
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
    }
}
