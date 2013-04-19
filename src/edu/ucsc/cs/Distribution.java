package edu.ucsc.cs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.ucsc.cs.DatabaseManager;

public class Distribution {
	public HashMap<String, Integer> changeFrequencies;

	public Distribution(int repoID, List<Integer> excludedFileIDs) throws Exception {
		changeFrequencies = new HashMap<String, Integer>();
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet commitRS = stmt
				.executeQuery("SELECT id AS commit_id FROM scmlog WHERE repository_id = "
						+ repoID + " ORDER BY commit_date ASC");
		while (commitRS.next()) {
			int commitID = commitRS.getInt("commit_id");
			Commit commit = new Commit(commitID, excludedFileIDs);
			commit.extractASTDelta();
			HashMap<String, Integer> frequencies = commit.getFrequencies();
			for (String changeType : frequencies.keySet()) {
				if (changeFrequencies.containsKey(changeType)) {
					changeFrequencies.put(changeType,
							changeFrequencies.get(changeType)
									+ frequencies.get(changeType));
				} else {
					changeFrequencies.put(changeType,
							frequencies.get(changeType));
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
//		Distribution dist = new Distribution(1, Arrays.asList(641, 1165)); // voldemort local
		Distribution dist = new Distribution(9, new ArrayList<Integer>());
		String[] changeTypes = dist.changeFrequencies.keySet().toArray(new String[0]);
		Arrays.sort(changeTypes);
		System.out.println("changeType,freq");
		for(String changeType : changeTypes) {
			System.out.println(changeType + ',' + dist.changeFrequencies.get(changeType));
		}
	}

}
