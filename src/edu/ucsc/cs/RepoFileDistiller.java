package edu.ucsc.cs;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class RepoFileDistiller {
	private ChangeReducer reducer;
	private Logger logger;
	private Connection conn;

	public RepoFileDistiller(ChangeReducer reducer) {
		this.reducer = reducer;
		logger = LogManager.getLogger();
		conn = DatabaseManager.getConnection();
	}
	
	public void extractASTDelta(int fileID, int commitID, char actionType) throws Exception {
		logger.info("Extracting AST difference for file " + fileID + 
				"@commit " + commitID + 
				" with action type " + actionType);
		switch (actionType) {
		case 'C':
			// the file is copied
			break;
		case 'M':
			processModify(fileID);
			break;
		case 'D':
			processDelete(fileID);
			break;
		case 'A':
			processAdd(fileID);
			break;
		case 'V':
			processRename(fileID);
			break;
		}
	}

	private void processModify(int fileID) throws Exception {
		String newContent = getNewContent(fileID);
		if (newContent == null)
			logger.warning("Content for file " + fileID + " at commit_id " + id
					+ " not found");
		String oldContent = getOldContent(fileID);
		extractDiff(oldContent, newContent, fileID);
		FileContent.previousContent.put(fileID, new FileContent(id, newContent));
	}

	private String getNewContent(int fileID) throws Exception {
		Statement stmt = conn.createStatement();
		String query = "select content from content where file_id=" + fileID
				+ " and commit_id=" + this.id;
		logger.fine(query);
		ResultSet rs = stmt.executeQuery(query);
		String result;
		if (!rs.next()) {
			result = null;
		} else {
			result = rs.getString("content");
		}
		stmt.close();
		return result;
	}

	private void processDelete(int fileID) throws Exception {
		String oldContent = getOldContent(fileID);
		extractDiff(oldContent, "", fileID);
	}

	private void processAdd(int fileID) throws Exception {
		String newContent = getNewContent(fileID);
		extractDiff("", newContent, fileID);
		FileContent.previousContent.put(fileID, new FileContent(id, newContent));
	}

	private void processRename(int fileID) throws Exception {
		processModify(fileID);
	}

	private void extractDiff(String oldContent, String newContent, int fileID) {
		if (newContent == null || oldContent == null) {
			return;
		}

		File newFile = FileUtils.javaFileFromString("New", newContent);
		File oldFile = FileUtils.javaFileFromString("Old", oldContent);
		distiller.extractClassifiedSourceCodeChanges(oldFile, newFile,
				"commit_id " + id);

		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		if (changes == null) {
			logger.config("No diff of file " + fileID + " at commit " + id
					+ " found");
		} else {
			for (SourceCodeChange c : changes) {
				String category = c.getLabel();
				Integer count = changeFrequencies.get(category);
				if (count == null) {
					count = 0;
				}
				count++;
				changeFrequencies.put(category, count);
			}
		}
	}

	private String getOldContent(int fileID) {
		FileContent content = FileContent.previousContent.get(fileID);
		if(content == null) {
			logger.info("No previous revision of file " + fileID + "found!");
			return null;
		} else {
			logger.info("Previous revision of file " + fileID + " found at commit " + content.commitID);
			return content.content;
		}
	}

}
