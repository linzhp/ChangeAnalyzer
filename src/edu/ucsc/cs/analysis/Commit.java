package edu.ucsc.cs.analysis;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.logging.*;

import org.apache.commons.lang3.StringUtils;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.LogManager;

public class Commit {
	private int id;
	private Logger logger;
	private Connection conn;
	private List<Integer> includedFileIds;
	private RepoFileDistiller distiller;
	
	public Commit(int commitID, List<Integer> fildIds, ChangeReducer reducer) {
		this.id = commitID;
		logger = LogManager.getLogger();
		this.includedFileIds = fildIds;
		distiller = new RepoFileDistiller(reducer);
		conn = DatabaseManager.getSQLConnection();
	}

	public void extractASTDelta() throws Exception {
		try {
			Statement stmt = conn.createStatement();
			String fileIds = StringUtils.join(includedFileIds, ',');
			ResultSet file = stmt.executeQuery("select * " + "from actions "
					+ "where commit_id=" + id
					+ " and file_id in (" + fileIds + ")");
			while (file.next()) {
				int fileID = file.getInt("file_id");
				if (includedFileIds == null || includedFileIds.contains(fileID)) {
					char actionType = file.getString("type").charAt(0);
					distiller.extractASTDelta(fileID, id, actionType);
				}
			}
			stmt.close();
		} catch (IOException | SQLException e) {
			logger.log(Level.WARNING, "Error in distilling commit " + id, e);
			throw e;
		} 
	}
}
