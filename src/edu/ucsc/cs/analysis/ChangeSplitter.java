package edu.ucsc.cs.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static java.lang.System.out;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.ucsc.cs.simulation.Indexer;
import edu.ucsc.cs.simulation.JavaParser;
import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.FileUtils;
import edu.ucsc.cs.utils.LogManager;

/**
 * Split changes into training set and test set
 * @author linzhp
 *
 */
public class ChangeSplitter {
	private final int CUTOFF = 73;
	private final int FILE_ID = 2996;
	private List<ChangeReducer> traningReducers;
	private List<ChangeReducer> testReducers;
	public ChangeSplitter(List<ChangeReducer> trainingReducers,
			List<ChangeReducer> testReducers) {
		this.traningReducers = trainingReducers;
		this.testReducers = testReducers;
	}
	
	public void split() throws SQLException, IOException {
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection changesColl = mongo.getCollection("changes");
		DBCursor allChanges = changesColl.find(new BasicDBObject("fileId", FILE_ID))
				.sort(new BasicDBObject("date", 1));
		HashSet<Integer> traningCommitIds = new HashSet<Integer>();
		int trainingChanges = 0, testingChanges = 0;
		int cuttingCommit = 0, endCommit = 0;
		while (allChanges.hasNext()) {
			DBObject c = allChanges.next();
			Integer commitId = (Integer)c.get("commitId");
			if (traningCommitIds.size() < CUTOFF || traningCommitIds.contains(commitId)) {
				trainingChanges++;
				traningCommitIds.add(commitId);
				if (traningCommitIds.size() == CUTOFF) {
					cuttingCommit = commitId;
				}
				for (ChangeReducer r : traningReducers) {
					r.add(c);
				}
			} else {
				endCommit = commitId;
				testingChanges++;
				for (ChangeReducer r : testReducers) {
					r.add(c);
				}
			}
		}
		LogManager.getLogger().info("Training changes: " + trainingChanges +
				"\nTesting changes: " + testingChanges);
		
		String content = FileUtils.getContent(FILE_ID, cuttingCommit);
		out.println("Last version in the training set:");
		printStatics(content);
		if (content != null) {
			FileWriter fw = new FileWriter("TrainingEnd.java");
			fw.write(content);
			fw.close();			
		}
		
		content = FileUtils.getContent(FILE_ID, endCommit);
		out.println("Last version in the test set:");
		printStatics(content);
	}
	
	private void printStatics(String content) throws SQLException {
		JavaParser parser = new JavaParser();
		ASTHelper<JavaStructureNode> astHelper = parser.getASTHelper(content, "File.java");
		JavaStructureNode tree = astHelper.createStructureTree();
		CompilationUnitDeclaration astNode = (CompilationUnitDeclaration) tree.getASTNode();
		Indexer indexer = new Indexer();
		astNode.traverse(indexer, astNode.scope);
		out.println(String.valueOf(indexer.nodeIndex.get("CLASS").size()) + " classes");
		out.println(String.valueOf(indexer.nodeIndex.get("FIELD").size()) + " fields");
		out.println(String.valueOf(indexer.nodeIndex.get("METHOD").size()) + " methods");
	}

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws SQLException, IOException {
		ChangesPerCommit trainingChangesPerCommit = new ChangesPerCommit(),
				testChangesPerCommit = new ChangesPerCommit();
		ChangesPerCategory trainingChangesPerCategory = new ChangesPerCategory(),
				testChangesPerCategory = new ChangesPerCategory();
		HashMap<String, Record> counters = new HashMap<String, Record>();
		ChangeTypeCounter trainingCounter = new ChangeTypeCounter("training", counters),
				testCounter = new ChangeTypeCounter("test", counters);
		ChangeSplitter splitter = new ChangeSplitter(
				Arrays.asList(trainingChangesPerCategory, trainingChangesPerCommit, trainingCounter),
				Arrays.asList(testChangesPerCategory, testChangesPerCommit, testCounter));
		splitter.split();
		
		DB mongo = DatabaseManager.getMongoDB();
		
		DBCollection collection = mongo.getCollection("changesPerCommit");
		HashMap<Integer, Integer> changesPerCommit = trainingChangesPerCommit.getCounters();
		for (Integer key: changesPerCommit.keySet()) {
			collection.insert(new BasicDBObject("commit_id", key)
			.append("freq", changesPerCommit.get(key))
			.append("set", "training"));
		}
		
		changesPerCommit = testChangesPerCommit.getCounters();
		for (Integer key: changesPerCommit.keySet()) {
			collection.insert(new BasicDBObject("commit_id", key)
			.append("freq", changesPerCommit.get(key))
			.append("set", "test"));
		}
		
		collection = mongo.getCollection("changesPerCategory");
		HashMap<BasicDBObject, Integer> changesPerCatergory = trainingChangesPerCategory.getCounters();
		for (BasicDBObject key: changesPerCatergory.keySet()) {
			collection.insert(new BasicDBObject("change", key).
					append("freq", changesPerCatergory.get(key))
					.append("set", "training"));
		}

		changesPerCatergory = testChangesPerCategory.getCounters();
		for (BasicDBObject key: changesPerCatergory.keySet()) {
			collection.insert(new BasicDBObject("change", key).
					append("freq", changesPerCatergory.get(key))
					.append("set", "test"));
		}
		
		collection = mongo.getCollection("contingency");
		for (String changeType: counters.keySet()) {
			Record freq = counters.get(changeType);
			collection.insert(new BasicDBObject("_id", changeType)
			.append("training", freq.training)
			.append("test", freq.test));
		}
	}

}
