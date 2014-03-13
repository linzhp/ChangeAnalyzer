package edu.ucsc.cs.utils;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class DatabaseManager {

    public static Boolean test = false;
    private static Connection conn;
    private static DB db;

    public static Connection getSQLConnection() {
    	if (conn == null) {
            try {
            	if (test) {
            		Class.forName("org.sqlite.JDBC");
            		conn = DriverManager.getConnection("jdbc:sqlite:test.db");
            	} else {
                    File file = new File("config/database.properties");
                    FileInputStream fis = null;
                    fis = new FileInputStream(file);
                    Properties prop = new Properties();
                    prop.load(fis);
                    String databasename = (String) prop.get("URL");
                    String username = (String) prop.get("UserName");
                    String password = (String) prop.get("UserPass");
                    fis.close();
                    
                    conn = DriverManager
                            .getConnection(databasename, username, password);        		
            	}
            } catch (Exception e) {
                LogManager.getLogger().severe(e.toString());
                System.exit(1);
            }    		
    	}
        return conn;
    }
    
    public static DB getMongoDB() {
    	if (db == null) {
            try {
        		MongoClient mongo = new MongoClient("slamdance.soe.ucsc.edu");
        		db = mongo.getDB("evolution");
            } catch (Exception e) {
                LogManager.getLogger().severe(e.toString());
                System.exit(1);
            }
    	}
    	return db;
    }
}