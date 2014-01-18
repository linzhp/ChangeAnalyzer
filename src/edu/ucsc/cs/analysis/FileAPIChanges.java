package edu.ucsc.cs.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.ucsc.cs.utils.DatabaseManager;

public class FileAPIChanges {

	public FileAPIChanges(int fileID, Writer writer) throws SQLException, IOException {
		RepoFileDistiller distiller = new RepoFileDistiller(new APIChangeExtractor(writer));
		Connection conn = DatabaseManager.getSQLConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT commit_id, type FROM actions a" +
				" JOIN scmlog s ON s.id=a.commit_id" +
				" WHERE file_id=" + fileID +
				" ORDER BY commit_date");
		while (rs.next()) {
			distiller.extractASTDelta(rs);
		}
	}
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException, IOException {
		FileWriter writer = new FileWriter(new File("12246.csv"));
		new FileAPIChanges(12246, writer);
		writer.close();
	}

}
