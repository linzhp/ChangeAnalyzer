package edu.ucsc.cs.analysis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.*;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.LogManager;

public class CommitGraph {
	private static HashMap<Integer, TreeSet<Integer>> previousCommits = new HashMap<Integer, TreeSet<Integer>>();
	private Connection conn;
	private Logger logger;

	public CommitGraph() {
		conn = DatabaseManager.getSQLConnection();
		logger = LogManager.getLogger();
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
		if (pc == null) {
			return -1;
		}
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

	public void addCommit(int fileId, int commitId) {
		TreeSet<Integer> c = previousCommits.get(fileId);
		if (c == null) {
			c = new TreeSet<Integer>();
			previousCommits.put(fileId, c);
		}
		c.add(commitId);
	}
}
