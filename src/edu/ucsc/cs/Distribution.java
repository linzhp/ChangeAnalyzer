package edu.ucsc.cs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import edu.ucsc.cs.DatabaseManager;

public class Distribution {
	public Distribution(int repoID) throws Exception {
		Connection conn = DatabaseManager.getConnection();
		Statement stmt1 = conn.createStatement();
		ResultSet commitRS = stmt1.executeQuery("SELECT id AS commit_id FROM scmlog WHERE repository_id = " + repoID);
		while(commitRS.next()) {
			
		}
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
