package tapsi.com.database;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import tapsi.com.data.Client;
import tapsi.com.logging.GeoDoorExceptions;
import tapsi.com.logging.LogHandler;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public class DBHandler {

    private String dbUrl = "jdbc:sqlite:my.db";
    private Connection c = null;
    private Statement stmt = null;

    Dao<Client, String> clientDao;
    ConnectionSource connectionSource;


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

        try {
            connectionSource = new JdbcConnectionSource(dbUrl);
            clientDao = DaoManager.createDao(connectionSource, Client.class);
        } catch (SQLException e) {
            e.printStackTrace();
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

    public List<Client> readAllObjects() {
        try {
            List<Client> clients = clientDao.queryForAll();

            ListIterator iterator = clients.listIterator();
            while (iterator.hasNext()) {
                Client client = (Client) iterator.next();
                //client.printData();

            }
            return clients;
        } catch (SQLException e) {
            e.printStackTrace();
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
