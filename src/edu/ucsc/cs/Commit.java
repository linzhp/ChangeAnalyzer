package edu.ucsc.cs;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.*;

public class Commit {
	private int id;
	private Logger logger;
	private Connection conn;
	private List<Integer> excludedFileIDs;
	private RepoFileDistiller distiller;
	private BasicFreqCounter reducer;

	public Commit(int commitID, List<Integer> excludedFileIDs) {
		this.id = commitID;
		logger = LogManager.getLogger();
		this.excludedFileIDs = excludedFileIDs;
		reducer = new BasicFreqCounter();
		distiller = new RepoFileDistiller(reducer);
		conn = DatabaseManager.getConnection();
	}

	public void extractASTDelta() throws Exception {
		try {
			Statement stmt = conn.createStatement();
			ResultSet file = stmt.executeQuery("select * " + "from actions "
					+ "where commit_id=" + id
					+ " and current_file_path like '%.java'");
			while (file.next()) {
				int fileID = file.getInt("file_id");
				if (excludedFileIDs.contains(fileID)) {
					continue;
				}
				char actionType = file.getString("type").charAt(0);
				distiller.extractASTDelta(fileID, id, actionType);
			}
			stmt.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error in distilling commit " + id, e);
			throw e;
		}
	}
	
	public HashMap<String, Integer> getFrequencies() {
		return reducer.changeFrequencies;
	}
}
