package edu.ucsc.cs.utils;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class FileUtils {
	public static File javaFileFromString(String content, String fileName) {
		File temp = null;
		try {
			temp = File.createTempFile(fileName, ".java");
			temp.deleteOnExit();
			BufferedWriter out = new BufferedWriter(new FileWriter(temp));
			out.write(content);
			out.close();			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return temp;
	}
	
	public static String getContent(int fileId, int commitId) throws SQLException {
		Connection conn = DatabaseManager.getSQLConnection();
		Statement stmt = conn.createStatement();
		String query = "select content from content where file_id=" + fileId
				+ " and commit_id=" + commitId;
		Logger logger = LogManager.getLogger();
		logger.fine(query);
		ResultSet rs = stmt.executeQuery(query);
		String result;
		if (!rs.next()) {
			result = null;
			logger.warning("Content for file " + fileId + " at commit_id "
					+ commitId + " not found");
		} else {
			result = rs.getString("content");
		}
		stmt.close();
		return result;
	}
}
