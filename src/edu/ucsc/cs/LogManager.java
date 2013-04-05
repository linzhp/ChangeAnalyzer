package edu.ucsc.cs;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogManager {
	private static Logger logger;
	
	public static Logger getLogger() {
		if(logger == null){
			logger = Logger.getLogger("ChangeAnalyzer");
			FileHandler fh;
			try {
				FileInputStream fis = new FileInputStream("config/logger.properties");
	            Properties prop = new Properties();
	            prop.load(fis);
				fh = new FileHandler(prop.getProperty("LogFile"), false);
				fh.setFormatter(new SimpleFormatter());
				logger.addHandler(fh);
				String logLevel = prop.getProperty("LogLevel");
				if(logLevel.equals("SEVERE")) {
					logger.setLevel(Level.SEVERE);
				} else if(logLevel.equals("WARNING")) {
					logger.setLevel(Level.WARNING);
				} else if(logLevel.equals("INFO")) {
					logger.setLevel(Level.INFO);
				} else if(logLevel.equals("CONFIG")) {
					logger.setLevel(Level.CONFIG);
				}
				fis.close();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return logger;
	}
}
