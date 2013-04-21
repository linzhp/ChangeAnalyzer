package edu.ucsc.cs.utils;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {

    private static DatabaseManager dbManager;
    private String databasename, username, password;
    private Connection conn = null;
    Statement stmt;

    private DatabaseManager() {
        File file = new File("config/database.properties");

        try {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(fis);
            databasename = (String) prop.get("URL");
            username = (String) prop.get("UserName");
            password = (String) prop.get("UserPass");
            fis.close();
            
            conn = DriverManager
                    .getConnection(databasename, username, password);
            stmt = conn.createStatement();

        } catch (Exception e) {
            LogManager.getLogger().severe(e.toString());
            System.exit(1);
        }
    }

    public static Connection getConnection() {
        if (dbManager == null) {
            dbManager = new DatabaseManager();
        }
        return dbManager.conn;
    }
}