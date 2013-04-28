package edu.ucsc.cs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import edu.ucsc.cs.utils.DatabaseManager;

public class Repository {
	public Repository(int repoID, List<Integer> excludedFileIDs, ChangeReducer reducer) throws Exception  {
		Connection conn = DatabaseManager.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet commitRS = stmt
				.executeQuery("SELECT id AS commit_id FROM scmlog WHERE repository_id = "
						+ repoID + " ORDER BY commit_date ASC");
		while (commitRS.next()) {
			int commitID = commitRS.getInt("commit_id");
			Commit commit = new Commit(commitID, excludedFileIDs, reducer);
			commit.extractASTDelta();
		}
		stmt.close();
	}


}
