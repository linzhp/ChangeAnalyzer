package edu.ucsc.cs;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.*;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class Commit {
	private int id;
	private Logger logger;
	private FileDistiller distiller;
	private Connection conn;
	public HashMap<String, Integer> changeFrequency = new HashMap<String, Integer>();

	public Commit(int commitID) {
		this.id = commitID;
		logger = LogManager.getLogger();
		distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		conn = DatabaseManager.getConnection();
		extractASTDelta();
	}

	private void extractASTDelta() {
		try {
			Statement stmt = conn.createStatement();
			ResultSet file = stmt.executeQuery("select * " + "from actions "
					+ "where commit_id=" + id
					+ " and current_file_path like '%.java'");
			while (file.next()) {
				int fileID = file.getInt("file_id");
				char actionType = file.getString("type").charAt(0);
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
		} catch (Exception e) {
			logger.warning(e.toString());
		}
	}

	private void processModify(int fileID) throws Exception {
		String newContent = getNewContent(fileID);
		if (newContent == null)
			logger.warning("Content for file " + fileID + " at commit_id " + id
					+ " not found");
		String oldContent = getOldContent(fileID);
		extractDiff(oldContent, newContent, fileID);
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
	}

	private void processRename(int fileID) throws Exception {
		processModify(fileID);
	}

	private void extractDiff(String oldContent, String newContent, int fileID) {
		if (newContent == null) {
			return;
		}
		if (oldContent == null) {
			return;
		}

		try {
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
					Integer count = changeFrequency.get(category);
					if (count == null) {
						count = 0;
					}
					count++;
					changeFrequency.put(category, count);
				}
			}
		} catch (Exception e) {
			logger.warning(e.toString());
		}
	}

	private String getOldContent(int fileID) throws Exception {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select content "
				+ "from content where file_id=" + fileID + " and commit_id<"
				+ this.id + " order by commit_id desc limit 1");
		String result;
		if (!rs.next()) {
			logger.warning("No content for previous version of " + fileID
					+ " at commit_id " + id + " found");
			result = null;
		} else {
			result = rs.getString("content");
		}
		stmt.close();
		return result;
	}
}
