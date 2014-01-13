package edu.ucsc.cs.analysis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.LogManager;

public class Commit {
	private int id;
	private Logger logger;
	private Connection conn;
	private List<Integer> includedFileIds;
	private RepoFileDistiller distiller;
	
	public Commit(int commitID, List<Integer> fildIds, ChangeProcessor processor) {
		this.id = commitID;
		logger = LogManager.getLogger();
		this.includedFileIds = fildIds;
		distiller = new RepoFileDistiller(processor);
		conn = DatabaseManager.getSQLConnection();
	}

	public void extractASTDelta() throws Exception {
		try {
			Statement stmt1 = conn.createStatement();
			Statement stmt2 = conn.createStatement();
			ResultSet file = stmt1.executeQuery("select * from actions "
					+ "where commit_id=" + id);
			while (file.next()) {
				int fileId = file.getInt("file_id");
				if (includedFileIds == null || includedFileIds.contains(fileId)) {
					ResultSet fileInfo = stmt2.executeQuery("SELECT * from files "
							+ "where id = " + fileId);
					if (fileInfo.next()) {
						String fileName = fileInfo.getString("file_name");
						if (!fileName.endsWith(".java")) {
							continue;
						}
					} else {
						logger.warning("File " + fileId + " not found in files table!");
						continue;
					}
					
					String changeStatus = file.getString("type");
					if (changeStatus.length() == 1) {
						// ignore merges
						char actionType = changeStatus.charAt(0);						
						distiller.extractASTDelta(fileId, id, actionType);
					}
				}
			}
			stmt1.close();
			stmt2.close();
		} catch (IOException | SQLException e) {
			logger.log(Level.WARNING, "Error in distilling commit " + id, e);
			throw e;
		} 
	}
}
