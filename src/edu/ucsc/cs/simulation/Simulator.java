package edu.ucsc.cs.simulation;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilationUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import edu.ucsc.cs.utils.DatabaseManager;

/**
 * Things need to do to run a different simulation:
 * 1. Update the Sampler parameters.
 * 2. Update the number of commits to simulate
 * @author linzhp
 *
 */
public class Simulator {
	private HashMap<String, ArrayList<ASTNode>> nodeIndex;
	private Indexer indexer;
	private CompilationUnitDeclaration astNode;

	public CompilationUnitDeclaration getAstNode() {
		return astNode;
	}

	public Simulator() throws SQLException {
		String fileName = "TrainingEnd.java";
		astNode = JavaCompilationUtils.compile(
				new File(fileName), ClassFileConstants.JDK1_7).getCompilationUnit();
		indexer = new Indexer();
		astNode.traverse(indexer, astNode.scope);
		nodeIndex = indexer.nodeIndex;
	}

	public void run(int numCommits) {
		Sampler sampler = new Sampler(2.483616, 1.659271);
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
					ASTNode selected = entities
							.get((int) (entities.size() * Math.random()));
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
	 */
	public static void main(String[] args) throws SQLException {
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection collection = mongo.getCollection("simulationResults");
		for (int i = 0; i < 500; i++) {
			Simulator simulator = new Simulator();
			simulator.run(20);
			HashMap<String, ArrayList<ASTNode>> index = simulator.nodeIndex;
			collection.insert(new BasicDBObject("methods", index.get("METHOD").size())
			.append("fields", index.get("FIELD").size())
			.append("classes", index.get("CLASS").size()));
			
		}
	}

}
