package edu.ucsc.cs.analysis;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.FileUtils;
import edu.ucsc.cs.utils.LogManager;

public class RepoFileDistiller {
	private ChangeReducer reducer;
	private Logger logger;
	private Connection conn;

	public RepoFileDistiller(ChangeReducer reducer) {
		this.reducer = reducer;
		logger = LogManager.getLogger();
		conn = DatabaseManager.getMySQLConnection();
	}
	
	public void extractASTDelta(int fileID, int commitID, char actionType) throws SQLException, IOException  {
		logger.info("Extracting AST difference for file " + fileID + 
				"@commit " + commitID + 
				" with action type " + actionType);
		switch (actionType) {
		case 'C':
			// the file is created by copying from another file
			processCopy(fileID, commitID);
			break;
		case 'M':
			processModify(fileID, commitID);
			break;
		case 'D':
			// a file is deleted
			processDelete(fileID, commitID);
			break;
		case 'A':
			// a file is added
			processAdd(fileID, commitID);
			break;
		case 'V':
			processRename(fileID, commitID);
			break;
		}
	}
	
	private void processDelete(int fileID, int commitID) {
		FileContent.previousContent.remove(fileID);
	}
	
	private void processAdd(int fileID, int commitID) throws SQLException {
		String newContent = getNewContent(fileID, commitID);
		if (newContent == null)
			logger.warning("Content for file " + fileID + " at commit_id " + commitID
					+ " not found");
		FileContent.previousContent.put(fileID, new FileContent(commitID, newContent));
	}
	
	private void processCopy(int fileID, int commitID) throws SQLException {
		processAdd(fileID, commitID);
	}

	private void processModify(int fileID, int commitID) throws SQLException, IOException  {
		String newContent = getNewContent(fileID, commitID);
		String oldContent = getOldContent(fileID);
		List<SourceCodeChange> changes = extractDiff(oldContent, newContent);
		if (changes.size() == 0) {
			logger.warning("No changes distilled for file "+ fileID + " at commit_id " + commitID);
		}
		this.reducer.add(changes, fileID, commitID);
		FileContent.previousContent.put(fileID, new FileContent(commitID, newContent));
	}

	private String getNewContent(int fileID, int commitID) throws SQLException  {
		Statement stmt = conn.createStatement();
		String query = "select content from content where file_id=" + fileID
				+ " and commit_id=" + commitID;
		logger.fine(query);
		ResultSet rs = stmt.executeQuery(query);
		String result;
		if (!rs.next()) {
			result = null;
			logger.warning("Content for file " + fileID + " at commit_id " + commitID
					+ " not found");
		} else {
			result = rs.getString("content");
		}
		stmt.close();
		return result;
	}


	private void processRename(int fileID, int commitID) throws SQLException, IOException {
		processModify(fileID, commitID);
	}

	private List<SourceCodeChange> extractDiff(String oldContent, String newContent) throws IOException {
		if (newContent == null || oldContent == null) {
			return null;
		}

		File newFile = FileUtils.javaFileFromString("New", newContent);
		File oldFile = FileUtils.javaFileFromString("Old", oldContent);
		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		distiller.extractClassifiedSourceCodeChanges(oldFile, newFile);

		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		if(changes == null) {
			logger.info("No AST difference found");
		} 
		return changes;
	}

	private String getOldContent(int fileID) {
		FileContent content = FileContent.previousContent.get(fileID);
		if(content == null) {
			logger.info("No previous revision of file " + fileID + " found!");
			return null;
		} else {
			logger.info("Previous revision of file " + fileID + " found at commit " + content.commitID);
			return content.content;
		}
	}

}
