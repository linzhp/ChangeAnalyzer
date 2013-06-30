package edu.ucsc.cs.simulation;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.out;

import org.apache.commons.lang3.StringUtils;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureNode;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.LogManager;

public class Simulator {
	private HashMap<String, ArrayList<JavaStructureNode>> nodeIndex;
	private ASTHelper<JavaStructureNode> astHelper;
	
	public Simulator() throws SQLException {
		nodeIndex = new HashMap<String, ArrayList<JavaStructureNode>>();
		JavaParser parser = new JavaParser();
		astHelper = parser.getASTHelper(new File("TextArea.java"));
		indexNodes(astHelper.createStructureTree());
	}
	
	/**
	 * Traverse the AST using pre-order depth first search to index all tree nodes by their types
	 * @param root
	 * @return
	 */
	public void indexNodes(JavaStructureNode root) {
		Node declarationTree = astHelper.createDeclarationTree(root);
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
		// TODO private, public motifiers?
		return "";
	}
	
	public String getString(JavaStructureNode root) {
		return getString(root, 0);
	}

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		Simulator simulator = new Simulator();
	}

}
