package edu.ucsc.cs.simulation;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

import static java.lang.System.out;


import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

import edu.ucsc.cs.utils.LogManager;

public class Simulator {
	private HashMap<String, ArrayList<JavaStructureNode>> nodeIndex;
	private ASTHelper<JavaStructureNode> astHelper;
	
	public Simulator() throws SQLException {
		nodeIndex = new HashMap<String, ArrayList<JavaStructureNode>>();
		JavaParser parser = new JavaParser();
		astHelper = parser.getASTHelper(new File("TextArea.java"));
		JavaStructureNode tree = astHelper.createStructureTree();
		ASTNode classNode = tree.getChildren().get(0).getASTNode();
		ClassModifier modifier = new ClassModifier(classNode);
		modifier.addClass();
		out.print(classNode);
//		indexNodes(tree);
	}
	
	/**
	 * Traverse the AST using pre-order depth first search to index all tree nodes by their types
	 * @param root
	 * @return
	 */
	public void indexNodes(JavaStructureNode root) {
		JavaASTNodeTypeConverter converter = new JavaASTNodeTypeConverter();
		EntityType type = converter.convertNode(root.getASTNode());
		if (type != null) {
			String typeString = type.toString();
			if (nodeIndex.containsKey(typeString)) {
				nodeIndex.get(typeString).add(root);
			} else {
				nodeIndex.put(typeString, new ArrayList<JavaStructureNode>(Arrays.asList(root)));
			}
		} else {
			LogManager.getLogger().info("Invalid ASTNode class: " + root.getASTNode().getClass());
		}
		for (JavaStructureNode child : root.getChildren()) {
			indexNodes(child);
		}
	}
	
	private String getString(JavaStructureNode root, int indent) {
		Node declarationTree = astHelper.createDeclarationTree(root);
		StringBuilder buffer = new StringBuilder();
		// Printing declaration
		EntityType entityType = declarationTree.getEntity().getType();
		if (entityType != null) {
			for (int i = 0; i < indent; i++) {
				buffer.append('\t');
			}
			switch (entityType.toString()) {
			case "CLASS":
				
				break;
			case "METHOD":
				break;
			case "FIELD":
				break;
			}
		}
		for (JavaStructureNode c : root.getChildren()) {
			buffer.append(getString(c, indent + 1));
		}
		return buffer.toString();
	}
	
	public String getString(JavaStructureNode root) {
		return getString(root, 0);
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
