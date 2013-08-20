package edu.ucsc.cs.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

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

/**
 * Split changes into training set and test set
 * @author linzhp
 *
 */
public class ChangeSplitter {
	private final double SPLITTING_RATIO = .2;
	private final int TRAINING_SIZE = 40;
	private final int TEST_SIZE = 40;
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
		
		TreeSet<Integer> commitIdSet = new TreeSet<Integer>();
		while (allChanges.hasNext()) {
			DBObject c = allChanges.next();
			Integer commitId = (Integer)c.get("commitId");
			commitIdSet.add(commitId);
		}
		Integer[] commitIds = commitIdSet.toArray(new Integer[commitIdSet.size()]);
		double splittingPosition = commitIds.length * SPLITTING_RATIO;
		if (splittingPosition < 1 || splittingPosition >= commitIds.length) {
			System.err.println("Invalid splitting position");
			System.exit(1);
		}
		int cuttingCommit = commitIds[(int)splittingPosition - 1], 
				endCommit;
		if (splittingPosition + TEST_SIZE >= commitIds.length) {
			endCommit = commitIds[commitIds.length - 1];
		} else {
			endCommit = commitIds[(int)splittingPosition + TEST_SIZE];
		}
		
		// Collecting training commits
		for (int i = 1; i <= TRAINING_SIZE; i++) {
			int index = (int)splittingPosition - i;
			if (index < 0)
				break;
			int commitId = commitIds[index];
			DBCursor changes = changesColl.find(new BasicDBObject("fileId", FILE_ID).append("commitId", commitId));
			while (changes.hasNext()) {
				DBObject c = changes.next();
				for (ChangeReducer reducer : traningReducers) {
					reducer.add(c);
				}
			}
		}
		// Collecting test commits
		for (int i = 0; i < TEST_SIZE; i++) {
			int index = (int)splittingPosition + i;
			if (index >= commitIds.length) {
				break;
			}
			int commitId = commitIds[index];
			DBCursor changes = changesColl.find(new BasicDBObject("fileId", FILE_ID).append("commitId", commitId));
			while (changes.hasNext()) {
				DBObject c = changes.next();
				for (ChangeReducer reducer : testReducers) {
					reducer.add(c);
				}
			}
		}
		
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
