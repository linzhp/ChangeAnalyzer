package edu.ucsc.cs.simulation;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

import com.mongodb.BasicDBObject;

import static java.lang.System.out;


import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;

public class Simulator {
	private ASTHelper<JavaStructureNode> astHelper;
	private HashMap<String, ArrayList<ASTNode>> nodeIndex;
	private Indexer indexer;
	private CompilationUnitDeclaration astNode;
	
	public CompilationUnitDeclaration getAstNode() {
		return astNode;
	}

	public Simulator() throws SQLException {
		JavaParser parser = new JavaParser();
		astHelper = parser.getASTHelper(new File("TextArea.java"));
		JavaStructureNode tree = astHelper.createStructureTree();
		astNode = (CompilationUnitDeclaration)tree.getASTNode();
		indexer = new Indexer();
		astNode.traverse(indexer, astNode.scope);
		nodeIndex = indexer.nodeIndex;
	}
	
	public void simulate(int numCommits, int numRuns) {
		Sampler sampler = new Sampler(1.503785, 1.215351);
		for (int i = 0; i < numCommits; i++) {
			List<BasicDBObject> changes = sampler.generateCommit();
			for (BasicDBObject c : changes) {
				String entityType = null;
				switch (c.getString("changeClass")) {
				case "Insert":
				case "Delete":
					entityType = c.getString("parentEntity");
					break;
				case "Update":
					entityType = c.getString("entity");
					break;
				case "Move":
					entityType = c.getString("newParentEntity");
					break;
				}
				ArrayList<ASTNode> entities = nodeIndex.get(entityType);
				if (entities != null && entities.size() > 0) {
					ASTNode selected = entities.get((int)(entities.size()*Math.random()));
					Modifier m;
					switch (entityType) {
					case "CLASS":
						m = new ClassModifier(selected, indexer);
						break;
					case "FIELD":
						m = new FieldModifier(indexer);
						break;
					case "METHOD":
						m = new MethodModifier(selected, indexer);
						break;
					default:
						m = null;
					}
					m.modify(c);
					
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws SQLException {
		Simulator simulator = new Simulator();
		simulator.simulate(20, 1);
		out.print(simulator.getAstNode());
	}

}
