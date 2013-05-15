package edu.ucsc.cs.utils;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class DatabaseManager {

    private static DatabaseManager instance;
    private String databasename, username, password;
    private Connection conn;
    private DB db;

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
    		MongoClient mongo = new MongoClient();
    		db = mongo.getDB("Evolution");
        } catch (Exception e) {
            LogManager.getLogger().severe(e.toString());
            System.exit(1);
        }
        
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static Connection getMySQLConnection() {
        return getInstance().conn;
    }
    
    public static DB getMongoDB() {
    	return getInstance().db;
    }
}