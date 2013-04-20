package edu.ucsc.cs;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class RepoFileDistiller {
	private ChangeReducer reducer;
	private Logger logger;
	private Connection conn;
	private FileDistiller distiller;

	public RepoFileDistiller(ChangeReducer reducer) {
		this.reducer = reducer;
		logger = LogManager.getLogger();
		conn = DatabaseManager.getConnection();
		distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
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
			processModify(fileID, commitID);
			break;
		case 'D':
			processDelete(fileID);
			break;
		case 'A':
			processAdd(fileID, commitID);
			break;
		case 'V':
			processRename(fileID, commitID);
			break;
		}
	}

	private void processModify(int fileID, int commitID) throws Exception {
		String newContent = getNewContent(fileID, commitID);
		if (newContent == null)
			logger.warning("Content for file " + fileID + " at commit_id " + commitID
					+ " not found");
		String oldContent = getOldContent(fileID);
		extractDiff(oldContent, newContent);
		FileContent.previousContent.put(fileID, new FileContent(commitID, newContent));
	}

	private String getNewContent(int fileID, int commitID) throws Exception {
		Statement stmt = conn.createStatement();
		String query = "select content from content where file_id=" + fileID
				+ " and commit_id=" + commitID;
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
		extractDiff(oldContent, "");
	}

	private void processAdd(int fileID, int commitID) throws Exception {
		String newContent = getNewContent(fileID, commitID);
		extractDiff("", newContent);
		FileContent.previousContent.put(fileID, new FileContent(commitID, newContent));
	}

	private void processRename(int fileID, int commitID) throws Exception {
		processModify(fileID, commitID);
	}

	private void extractDiff(String oldContent, String newContent) throws IOException {
		if (newContent == null || oldContent == null) {
			return;
		}

		File newFile = FileUtils.javaFileFromString("New", newContent);
		File oldFile = FileUtils.javaFileFromString("Old", oldContent);
		distiller.extractClassifiedSourceCodeChanges(oldFile, newFile);

		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		if(changes == null) {
			logger.info("No AST difference found");
		} else {
			this.reducer.add(changes);			
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
