package edu.ucsc.cs.analysis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.LogManager;

public class Commit {
	private int id;
	private Logger logger;
	private Connection sqlConn;
	private List<Integer> includedFileIds;
	private RepoFileDistiller distiller;
	private DB mongoConn;
	private CommitGraph commitGraph;
	
	public Commit(int commitID, List<Integer> fildIds, ChangeProcessor processor, CommitGraph commitGraph) {
		this.id = commitID;
		logger = LogManager.getLogger();
		this.includedFileIds = fildIds;
		this.commitGraph = commitGraph;
		distiller = new RepoFileDistiller(processor, commitGraph);
		sqlConn = DatabaseManager.getSQLConnection();
		mongoConn = DatabaseManager.getMongoDB();
	}

	public void extractASTDelta() throws Exception {
		try {
			Statement stmt1 = sqlConn.createStatement();
			PreparedStatement fileTypeStmt = sqlConn.prepareStatement("SELECT * FROM file_types WHERE file_id = ?");
			PreparedStatement fileNameStmt = sqlConn.prepareStatement("SELECT * FROM files WHERE id = ?");
			ResultSet fileAction = stmt1.executeQuery("select * from actions "
					+ "where commit_id=" + id);
			DBCollection extractedChanges = mongoConn.getCollection("changes");
			while (fileAction.next()) {
				int fileId = fileAction.getInt("file_id");
				if (includedFileIds != null && !includedFileIds.contains(fileId)) {
					continue;
				}
				fileTypeStmt.setInt(1, fileId);
				ResultSet fileType = fileTypeStmt.executeQuery();
				if (!fileType.next()) {
					logger.warning("Cannot find file type for file " + fileId);
					continue;
				}
				String type = fileType.getString("type");
				if (!type.equals("code")) {
					continue;
				}
				
				fileNameStmt.setInt(1, fileId);
				ResultSet fileInfo = fileNameStmt.executeQuery();
				
				if (!fileInfo.next()) {
					logger.warning("File " + fileId + " not found in files table!");
					continue;
				} 
				String fileName = fileInfo.getString("file_name");
				if (!fileName.endsWith(".java")) {
					continue;
				}

				DBCursor cursor = extractedChanges.find(
						new BasicDBObject("commitId", id).append("fileId", fileId));
				if (!cursor.hasNext()) {
					distiller.extractASTDelta(fileAction);
				}
				commitGraph.addCommit(fileId, id);
			}
			stmt1.close();
			fileTypeStmt.close();
			fileNameStmt.close();
		} catch (IOException | SQLException e) {
			logger.log(Level.WARNING, "Error in distilling commit " + id, e);
			throw e;
		} 
	}
}
