package com.tapsi;


import java.sql.*;
import java.util.Date;

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
        System.out.println("Connected to DB!");

        // Create table if not existent
        try {
            stmt = c.createStatement();
            String sql = "create table if not exists Clients " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "name TEXT NOT NULL, phoneID TEXT NOT NULL UNIQUE, threadID TEXT UNIQUE, allowed INTEGER DEFAULT 0, lastConnection TEXT)";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
    }

    public void insertClient(String name, String phoneID, String threadID) {
        boolean checkAllowed = checkAllowedByPhoneID(phoneID);

        if (checkAllowed)
            insertClient(name, phoneID, threadID, 1);
        else
            insertClient(name, phoneID, threadID, 0);
    }

    public void insertClient(String name, String phoneID, String threadID, int allowed) {

        boolean checkPhoneID = checkClientByPhoneID(phoneID);
        //boolean checkName = checkClientByName(name);

        if (!checkPhoneID) {
            String sql = "insert into Clients(name, phoneID, threadID, allowed, lastConnection)" +
                    " select '" + name + "', '" + phoneID + "', '" + threadID + "', " + allowed + ", '" + new Date() + "'";
            try {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                LogHandler.handleError(e);
            }
        }
        else {
            // If insert is not possible just try tp update the client
            updateClient(name, phoneID, threadID, allowed);
        }
    }

    public void updateClient(String name, String phoneID, String threadID, int allowed) {
        String sqlName = "update Clients set threadID = '" + threadID + "', allowed = " + allowed + ", lastConnection = '" + new Date() + "'" + " where name = '" + name + "'";
        try {
            stmt.executeUpdate(sqlName);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
        String sqlPhoneID = "update Clients set name = '" + name + "', threadID = '" + threadID + "', allowed = " + allowed + ", lastConnection = '" + new Date() + "'" + " where phoneID = '" + phoneID + "'";
        try {
            stmt.executeUpdate(sqlPhoneID);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
    }

    public boolean checkClientByPhoneID(String phoneID) {
        String sql = "select count(*) from Clients where phoneID = '" + phoneID + "'";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int result = rs.getInt(1);
            rs.close();

            if (result == 1)
                return true;
            else if (result != 0) {
                throw new GeoDoorExceptions("Invalid Database! row:[phoneID]");
            }

        } catch (SQLException e) {
            LogHandler.handleError(e);
        } catch (GeoDoorExceptions geoDoorExceptions) {
            LogHandler.handleError(geoDoorExceptions);
        }
        return false;
    }

    public boolean checkClientByName(String name) {
        String sql = "select count(*) from Clients where name = '" + name + "'";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int result = rs.getInt(1);
            rs.close();

            if (result == 1)
                return true;
            else if (result != 0) {
                throw new GeoDoorExceptions("Invalid Database! row:[name]");
            }

        } catch (SQLException e) {
            LogHandler.handleError(e);
        } catch (GeoDoorExceptions geoDoorExceptions) {
            LogHandler.handleError(geoDoorExceptions);
        }
        return false;
    }

    public boolean checkAllowedByPhoneID(String phoneID) {
        String sql = "select allowed from Clients where phoneID = '" + phoneID + "'";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int result = rs.getInt(1);
            rs.close();

            if (result == 1)
                return true;
            else if (result != 0) {
                throw new GeoDoorExceptions("Invalid Database row:[allowed]!");
            }

        } catch (SQLException e) {
            LogHandler.handleError(e);
        } catch (GeoDoorExceptions geoDoorExceptions) {
            LogHandler.handleError(geoDoorExceptions);
        }
        return false;
    }

    public String selectThreadIDByName(String name) {
        String sql = "select threadID from Clients where name = '" + name + "'";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            String result = rs.getString(1);
            rs.close();

            return result;

        } catch (SQLException e) {
            LogHandler.handleError(e);
            return "";
        }
    }

    public String selectNameByThreadID(String threadID) {
        String sql = "select name from Clients where threadID = '" + threadID + "'";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            String result = rs.getString(1);
            rs.close();

            return result;

        } catch (SQLException e) {
            LogHandler.handleError(e);
            return "";
        }
    }

    public String selectThreadIDByPhoneID(String phoneID) {
        String sql = "select threadID from Clients where phoneID = '" + phoneID + "'";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            String result = rs.getString(1);
            rs.close();

            return result;

        } catch (SQLException e) {
            LogHandler.handleError(e);
            return "";
        }
    }

    public String selectLastConnectionByThreadID(String threadID) {
        String sql = "select lastConnection from Clients where threadID = '" + threadID + "'";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            String result = rs.getString(1);
            rs.close();

            return result;

        } catch (SQLException e) {
            LogHandler.handleError(e);
            return "";
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
