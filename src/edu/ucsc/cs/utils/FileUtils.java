package edu.ucsc.cs.utils;

import static java.lang.System.out;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilationUtils;
import edu.ucsc.cs.simulation.Indexer;

public class FileUtils {
	public static File javaFileFromString(String content, String fileName) {
		File temp = null;
		try {
			temp = File.createTempFile(fileName, ".java");
			temp.deleteOnExit();
			BufferedWriter out = new BufferedWriter(new FileWriter(temp));
			out.write(content);
			out.close();			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return temp;
	}
	
	public static String getContent(int fileId, int commitId) throws SQLException {
		Connection conn = DatabaseManager.getSQLConnection();
		Statement stmt = conn.createStatement();
		String query = "select content from content where file_id=" + fileId
				+ " and commit_id=" + commitId;
		Logger logger = LogManager.getLogger();
		logger.fine(query);
		ResultSet rs = stmt.executeQuery(query);
		String result;
		if (!rs.next()) {
			result = null;
			logger.severe("Content for file " + fileId + " at commit_id "
					+ commitId + " not found");
		} else {
			result = rs.getString("content");
		}
		stmt.close();
		return result;
	}
	
	
	public static void printStatics(String content) throws SQLException {
		CompilationUnitDeclaration astNode = JavaCompilationUtils.compile(
				content, "File.java", ClassFileConstants.JDK1_7).getCompilationUnit();
		Indexer indexer = new Indexer();
		astNode.traverse(indexer, astNode.scope);
		out.println(String.valueOf(indexer.nodeIndex.get("CLASS").size()) + " classes");
		out.println(String.valueOf(indexer.nodeIndex.get("FIELD").size()) + " fields");
		out.println(String.valueOf(indexer.nodeIndex.get("METHOD").size()) + " methods");
	}
}
