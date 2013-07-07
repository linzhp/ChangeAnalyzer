package edu.ucsc.cs.simulation;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

import static java.lang.System.out;


import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;

public class Simulator {
	private ASTHelper<JavaStructureNode> astHelper;
	private HashMap<String, ArrayList<ASTNode>> nodeIndex;
	
	public Simulator() throws SQLException {
		JavaParser parser = new JavaParser();
		astHelper = parser.getASTHelper(new File("TextArea.java"));
		JavaStructureNode tree = astHelper.createStructureTree();
		CompilationUnitDeclaration astNode = (CompilationUnitDeclaration)tree.getASTNode();
		Indexer indexer = new Indexer();
		astNode.traverse(indexer, astNode.scope);
		nodeIndex = indexer.nodeIndex;
		out.println(nodeIndex);
	}

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws SQLException, NoSuchAlgorithmException {
		Simulator simulator = new Simulator();
	}

}
