package tapsi.com.database;


import tapsi.com.data.Client;
import tapsi.com.logging.GeoDoorExceptions;
import tapsi.com.logging.LogHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHandler {

    private String dbUrl = "jdbc:sqlite:my.db";
    private Connection c = null;
    private Statement stmt = null;


    public DBHandler() {

        // Connect to database or create if no existent
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(dbUrl);
        } catch (ClassNotFoundException e) {
            LogHandler.handleError(e);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
        System.out.println(new Date() + ": Connected to DB!");

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
        } else {
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

    public void changeClientValues(String name, String phoneID, int allowed) {
        String sqlName = "update Clients set allowed = '" + allowed + "'" + " where name = '" + name + "'";
        try {
            stmt.executeUpdate(sqlName);
        } catch (SQLException e) {
            LogHandler.handleError(e);
        }
        String sqlPhoneID = "update Clients set name = '" + name + "', allowed = '" + allowed + "'" + " where phoneID = '" + phoneID + "'";
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

    public List<Client> readAllObjects() {
        List<Client> clients = new ArrayList<>();
        String sql = "select * from Clients";
        try {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Client client = new Client();
                client.setId(rs.getInt(1));
                client.setName(rs.getString(2));
                client.setPhoneID(rs.getString(3));
                client.setThreadID(rs.getString(4));
                client.setAllowed(rs.getInt(5));
                client.setLastConnection(rs.getString(6));
                clients.add(client);
            }
            rs.close();
            return clients;

        } catch (SQLException e) {
            LogHandler.handleError(e);
            return null;
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
