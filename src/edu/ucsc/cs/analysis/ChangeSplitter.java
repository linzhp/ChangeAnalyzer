package edu.ucsc.cs.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import static java.lang.System.out;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
	private final int FILE_ID = 2996;
	private List<ChangeReducer> traningReducers;
	private List<ChangeReducer> testReducers;
	private DBCollection changesColl;
	public ChangeSplitter(List<ChangeReducer> trainingReducers,
			List<ChangeReducer> testReducers) {
		this.traningReducers = trainingReducers;
		this.testReducers = testReducers;
		DB mongo = DatabaseManager.getMongoDB();
		changesColl = mongo.getCollection("changes");
	}
	
	public void splitByTime(double splitRatio, int trainingSize, int testSize, int daysInEpoch) throws SQLException, IOException {
		@SuppressWarnings("unchecked")
		List<String> dates = changesColl.distinct("date");
		String lastDateStr = Collections.max(dates);
		String firstDateStr = Collections.min(dates);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		DateTime startDate = fmt.parseDateTime(firstDateStr);
		Days totalDays = Days.daysBetween(startDate, fmt.parseDateTime(lastDateStr));
		double daysBeforeSplit = totalDays.getDays() * splitRatio;
		DateTime splitDate = startDate.plusDays((int)daysBeforeSplit);
		String splitDateStr = fmt.print(splitDate);
		int cuttingCommit = -1;
		// Collecting training changes
		DBCursor trainingChanges = changesColl.find(new BasicDBObject("fileId", FILE_ID).
				append("date", new BasicDBObject("$lte", splitDateStr)))
				.sort(new BasicDBObject("date", -1));
		while (trainingChanges.hasNext()) {
			DBObject c = trainingChanges.next();
			if (cuttingCommit == -1) {
				cuttingCommit = (int)c.get("commitId");
			}
			Days days = Days.daysBetween(fmt.parseDateTime((String)c.get("date")), splitDate);
			int epochId = days.getDays() / daysInEpoch + 1;
			if (epochId > trainingSize) {
				break;
			}
			// making the epochId in training set negative
			epochId *= -1;
			for (ChangeReducer reducer : traningReducers) {
				reducer.add(epochId, c);
			}
		}
		// Collecting test changes
		DBCursor testChanges = changesColl.find(new BasicDBObject("fileId", FILE_ID)
		.append("date", new BasicDBObject("$gt", splitDateStr)))
		.sort(new BasicDBObject("date", 1));
		int endCommit = -1;
		while (testChanges.hasNext()) {
			DBObject c = testChanges.next();
			endCommit = (int)c.get("commitId");
			Days days = Days.daysBetween(splitDate, fmt.parseDateTime((String)c.get("date")));
			int epochId = days.getDays() / daysInEpoch + 1;
			if (epochId > testSize) {
				break;
			}
			for (ChangeReducer reducer : testReducers) {
				reducer.add(epochId, c);
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
	
	public void splitByCommit(double splitRatio, int trainingSize, int testSize) throws SQLException, IOException {
		TreeSet<Integer> commitIdSet = new TreeSet<Integer>();
		DBCursor allChanges = changesColl.find(new BasicDBObject("fileId", FILE_ID))
				.sort(new BasicDBObject("date", 1));
		while (allChanges.hasNext()) {
			DBObject c = allChanges.next();
			Integer commitId = (Integer)c.get("commitId");
			commitIdSet.add(commitId);
		}
		Integer[] commitIds = commitIdSet.toArray(new Integer[commitIdSet.size()]);
		double splittingPosition = commitIds.length * splitRatio;
		if (splittingPosition < 1 || splittingPosition >= commitIds.length) {
			System.err.println("Invalid splitting position");
			System.exit(1);
		}
		int cuttingCommit = commitIds[(int)splittingPosition - 1], 
				endCommit;
		if (splittingPosition + testSize >= commitIds.length) {
			endCommit = commitIds[commitIds.length - 1];
		} else {
			endCommit = commitIds[(int)splittingPosition + testSize];
		}
		
		// Collecting training commits
		for (int i = 1; i <= trainingSize; i++) {
			int index = (int)splittingPosition - i;
			if (index < 0)
				break;
			int commitId = commitIds[index];
			DBCursor changes = changesColl.find(new BasicDBObject("fileId", FILE_ID).append("commitId", commitId));
			while (changes.hasNext()) {
				DBObject c = changes.next();
				for (ChangeReducer reducer : traningReducers) {
					reducer.add(commitId, c);
				}
			}
		}
		// Collecting test commits
		for (int i = 0; i < testSize; i++) {
			int index = (int)splittingPosition + i;
			if (index >= commitIds.length) {
				break;
			}
			int commitId = commitIds[index];
			DBCursor changes = changesColl.find(new BasicDBObject("fileId", FILE_ID).append("commitId", commitId));
			while (changes.hasNext()) {
				DBObject c = changes.next();
				for (ChangeReducer reducer : testReducers) {
					reducer.add(commitId, c);
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
		ChangesPerEpoch trainingChangesPerCommit = new ChangesPerEpoch(),
				testChangesPerCommit = new ChangesPerEpoch();
		ChangesPerCategory trainingChangesPerCategory = new ChangesPerCategory(),
				testChangesPerCategory = new ChangesPerCategory();
		ChangesPerCategoryEpoch trainingChangesPerCategoryEpoch = new ChangesPerCategoryEpoch(),
				testChangesPerCategoryEpoch = new ChangesPerCategoryEpoch();
		HashMap<String, Record> counters = new HashMap<String, Record>();
		ChangeTypeCounter trainingCounter = new ChangeTypeCounter("training", counters),
				testCounter = new ChangeTypeCounter("test", counters);
		ChangeSplitter splitter = new ChangeSplitter(
				Arrays.asList(trainingChangesPerCategory, 
						trainingChangesPerCommit, 
						trainingCounter,
						trainingChangesPerCategoryEpoch),
				Arrays.asList(testChangesPerCategory, 
						testChangesPerCommit, 
						testCounter,
						testChangesPerCategoryEpoch));
		splitter.splitByCommit(.4, 40, 40);
		
		DB mongo = DatabaseManager.getMongoDB();
		
		DBCollection collection = mongo.getCollection("changesPerEpoch");
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
		
		collection = mongo.getCollection("changesPerCategoryEpoch");
		HashMap<BasicDBObject, Integer> changesPerCategoryEpoch = trainingChangesPerCategoryEpoch.getCounters();
		for (BasicDBObject key: changesPerCategoryEpoch.keySet()) {
			collection.insert(new BasicDBObject("change", key)
			.append("freq", changesPerCategoryEpoch.get(key))
			.append("set", "training"));			
		}
		
		changesPerCategoryEpoch = testChangesPerCategoryEpoch.getCounters();
		for (BasicDBObject key: changesPerCategoryEpoch.keySet()) {
			collection.insert(new BasicDBObject("change", key)
			.append("freq", changesPerCategoryEpoch.get(key))
			.append("set", "test"));						
		}
	}

}
