package edu.ucsc.cs.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.LogManager;

public class CommitGraph {
	private HashMap<Integer, Set<Integer>> previousCommits;
	private Connection conn;
	private Logger logger;

	public CommitGraph() {
		conn = DatabaseManager.getSQLConnection();
		logger = LogManager.getLogger();
		previousCommits = new HashMap<>();
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
		Set<Integer> pc = previousCommits.get(fileId);
		if (pc == null) {
			return -1;
		}
		HashSet<Integer> visited = new HashSet<>();
		PreparedStatement stmt = conn.prepareStatement(
				"SELECT parent_id FROM commit_graph WHERE commit_id=?");
		ArrayDeque<Integer> frontier = new ArrayDeque<>();
		frontier.add(commitId);
		while (!frontier.isEmpty()) {
			int currentCommitId = frontier.poll();
			if (pc.contains(currentCommitId)) {
				// found it!
				stmt.close();
				return currentCommitId;
			}
			stmt.setInt(1, currentCommitId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int parentId = rs.getInt("parent_id");
				if (!visited.contains(parentId)) {
					frontier.add(parentId);
				}
			}
			visited.add(currentCommitId);
		}	
		logger.warning("No previous commit found for file " + fileId + "@ commit " + commitId);
		stmt.close();
		return -1;
	}

	public void addCommit(int fileId, int commitId) {
		Set<Integer> c = previousCommits.get(fileId);
		if (c == null) {
			c = new HashSet<Integer>();
			previousCommits.put(fileId, c);
		}
		c.add(commitId);
	}
}
