package com.tapsi;


import java.sql.*;

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

        // Todo: insert register Date Time and insert last register command date time
        // Create table if not existent
        try {
            stmt = c.createStatement();
            String sql = "create table if not exists Clients " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "name TEXT NOT NULL, phoneID TEXT NOT NULL UNIQUE, threadID TEXT UNIQUE, allowed INTEGER DEFAULT 0)";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
        //insertClient("Andreas",1);
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
            String sql = "insert into Clients(name, phoneID, threadID, allowed)" +
                    " select '" + name + "', '" + phoneID + "', '" + threadID + "', " + allowed;
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
        String sqlName = "update Clients set threadID = '" + threadID + "', allowed = " + allowed + " where name = '" + name + "'";
        try {
            stmt.executeUpdate(sqlName);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
        String sqlPhoneID = "update Clients set name = '" + name + "', threadID = '" + threadID + "', allowed = " + allowed + " where phoneID = '" + phoneID + "'";
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

    public void closeDB() {
        try {
            stmt.close();
            c.close();
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
    }
}
