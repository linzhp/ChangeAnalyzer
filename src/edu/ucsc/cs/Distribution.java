package edu.ucsc.cs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import edu.ucsc.cs.DatabaseManager;

public class Distribution {
	public HashMap<String, Integer> changeFrequencies;

	public Distribution(int repoID) throws Exception {
		changeFrequencies = new HashMap<String, Integer>();
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet commitRS = stmt
				.executeQuery("SELECT id AS commit_id FROM scmlog WHERE repository_id = "
						+ repoID);
		while (commitRS.next()) {
			int commitID = commitRS.getInt("commit_id");
			Commit commit = new Commit(commitID);
			commit.extractASTDelta();
			for (String changeType : commit.changeFrequencies.keySet()) {
				if (changeFrequencies.containsKey(changeType)) {
					changeFrequencies.put(changeType,
							changeFrequencies.get(changeType)
									+ commit.changeFrequencies.get(changeType));
				} else {
					changeFrequencies.put(changeType,
							commit.changeFrequencies.get(changeType));
				}
			}
		}
		stmt.close();
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Distribution dist = new Distribution(1);
		StringBuilder header = new StringBuilder();
		StringBuilder freq = new StringBuilder();
		for(String changeType : dist.changeFrequencies.keySet()) {
			header.append(changeType).append(',');
			freq.append(dist.changeFrequencies.get(changeType)).append(',');
		}
		header.deleteCharAt(header.length() - 1);
		freq.deleteCharAt(freq.length() - 1);
		System.out.println(header.toString());
		System.out.print(freq);
	}

}
