package edu.ucsc.cs.analysis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import edu.ucsc.cs.utils.DatabaseManager;

public class Repository {
	private int repoID;
	private ChangeProcessor reducer;
	
	public Repository(int repoID, ChangeProcessor reducer){
		this.repoID = repoID;
		this.reducer = reducer;
	}
	
	public void extractChanges(List<Integer> fileIDs) throws Exception  {
		Connection conn = DatabaseManager.getSQLConnection();
		Statement stmt = conn.createStatement();
		// commit id is in topological order
		ResultSet commitRS = stmt
				.executeQuery("SELECT id AS commit_id FROM scmlog WHERE repository_id = "
						+ repoID + " ORDER BY id ASC");
		while (commitRS.next()) {
			int commitID = commitRS.getInt("commit_id");
			Commit commit = new Commit(commitID, fileIDs, reducer);
			commit.extractASTDelta();
		}
		stmt.close();		
	}
}