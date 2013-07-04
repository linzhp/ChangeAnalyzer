package edu.ucsc.cs.analysis;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.FileUtils;
import edu.ucsc.cs.utils.LogManager;

public class RepoFileDistiller {
	private ChangeProcessor reducer;
	private Logger logger;
	private Connection conn;
	public static HashMap<Integer, TreeSet<Integer>> previousCommits = new HashMap<Integer, TreeSet<Integer>>();
	private static HashMap<Integer, FileContent> fileContentCache = new HashMap<Integer, FileContent>();

	public RepoFileDistiller(ChangeProcessor reducer) {
		this.reducer = reducer;
		logger = LogManager.getLogger();
		conn = DatabaseManager.getSQLConnection();
	}

	public void extractASTDelta(int fileId, int commitId, char actionType)
			throws SQLException, IOException {
		logger.info("Extracting AST difference for file " + fileId + "@commit "
				+ commitId + " with action type " + actionType);
		switch (actionType) {
		case 'C':
			// the file is created by copying from another file
			processCopy(fileId, commitId);
			break;
		case 'M':
			processModify(fileId, commitId);
			break;
		case 'D':
			// a file is deleted
			processDelete(fileId, commitId);
			break;
		case 'A':
			// a file is added
			processAdd(fileId, commitId);
			break;
		case 'V':
			processRename(fileId, commitId);
			break;
		}
		TreeSet<Integer> c = previousCommits.get(fileId);
		if (c == null) {
			c = new TreeSet<Integer>();
			previousCommits.put(fileId, c);
		}
		c.add(commitId);
	}

	private void processDelete(int fileID, int commitID) {
		fileContentCache.remove(fileID);
	}

	private void processAdd(int fileID, int commitID) throws SQLException {
		String newContent = getContent(fileID, commitID);
		if (newContent == null)
			logger.warning("Content for file " + fileID + " at commit_id "
					+ commitID + " not found");
		fileContentCache.put(fileID, new FileContent(commitID, newContent));
	}

	private void processCopy(int fileID, int commitID) throws SQLException {
		processAdd(fileID, commitID);
	}

	private void processModify(int fileId, int commitId) throws SQLException,
			IOException {
		String newContent = getContent(fileId, commitId);
		int previousCommitId = findPreviousCommitId(fileId, commitId);
		FileContent fileContent = fileContentCache.get(fileId);
		String oldContent = null;
		if (fileContent != null && fileContent.commitID == previousCommitId) {
			oldContent = fileContent.content;
		} else if (previousCommitId != -1){
			 oldContent = getContent(fileId, previousCommitId);			
		}
		List<SourceCodeChange> changes = extractDiff(oldContent, newContent);
		if (changes == null || changes.size() == 0) {
			logger.warning("No changes distilled for file " + fileId
					+ " at commit_id " + commitId + " from previous commit id " + previousCommitId);
		} else {
			this.reducer.add(changes, fileId, commitId);			
		}
		if (newContent != null)
			fileContentCache.put(fileId, new FileContent(commitId, newContent));
	}

	private String getContent(int fileID, int commitID) throws SQLException {
		Statement stmt = conn.createStatement();
		String query = "select content from content where file_id=" + fileID
				+ " and commit_id=" + commitID;
		logger.fine(query);
		ResultSet rs = stmt.executeQuery(query);
		String result;
		if (!rs.next()) {
			result = null;
			logger.warning("Content for file " + fileID + " at commit_id "
					+ commitID + " not found");
		} else {
			result = rs.getString("content");
		}
		stmt.close();
		return result;
	}

	/**
	 * For each previous commit of this file (fileId), using Breath First Search
	 * to see if it is the ancestor of commitId
	 * 
	 * @param fileId
	 * @param commitId
	 * @return
	 * @throws SQLException
	 */
	public int findPreviousCommitId(int fileId, int commitId)
			throws SQLException {
		TreeSet<Integer> pc = previousCommits.get(fileId);
		Iterator<Integer> it = pc.descendingIterator();
		while (it.hasNext()) {
			LinkedList<Integer> queue = new LinkedList<Integer>();
			Integer prevCommit = it.next();
			queue.add(prevCommit);
			currentSearch: while (!queue.isEmpty()) {
				Integer currentCommit = queue.pop();
				if (currentCommit == commitId) {
					// find an ancestor Commit
					return prevCommit;
				}

				if (currentCommit > commitId) {
					/*
					 * Optimization: As the commit ids are in topological order,
					 * if current commit id is greater than the target commit
					 * ID, it is not possible for the former to be the parent of
					 * the later.
					 */
					continue;
				}
				// add children to the queue
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt
						.executeQuery("SELECT * from commit_graph where parent_id="
								+ currentCommit);
				while (rs.next()) {
					int child = rs.getInt("commit_id");
					if (pc.contains(child)) {
						/*
						 * Optimization Because commitId is not a merge commit,
						 * it should only has one parent. Thus if currentCommit
						 * has a child already in previousCommits, it is not the
						 * preceding commit of commitId.
						 */
						break currentSearch;
					} else {
						queue.add(child);
					}
				}
				stmt.close();

			}
		}
		logger.warning("No previous commit found for file " + fileId + "@ commit " + commitId);
		return -1;
	}

	private void processRename(int fileID, int commitID) throws SQLException,
			IOException {
		processModify(fileID, commitID);
	}

	private List<SourceCodeChange> extractDiff(String oldContent,
			String newContent) throws IOException {
		if (newContent == null || oldContent == null) {
			return null;
		}

		File newFile = FileUtils.javaFileFromString("New", newContent);
		File oldFile = FileUtils.javaFileFromString("Old", oldContent);
		FileDistiller distiller = ChangeDistiller
				.createFileDistiller(Language.JAVA);
		distiller.extractClassifiedSourceCodeChanges(oldFile, newFile);

		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		if (changes == null) {
			logger.info("No AST difference found");
		}
		return changes;
	}
}