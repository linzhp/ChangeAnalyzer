package edu.ucsc.cs.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.LogManager;

/**
 * Split changes into training set and test set
 * @author linzhp
 *
 */
public class ChangeSplitter {
	private int cutOff;
	private List<ChangeReducer> traningReducers;
	private List<ChangeReducer> testReducers;
	public ChangeSplitter(int cutOff, 
			List<ChangeReducer> trainingReducers,
			List<ChangeReducer> testReducers) {
		this.cutOff = cutOff;
		this.traningReducers = trainingReducers;
		this.testReducers = testReducers;
	}
	
	public void split() {
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection changesColl = mongo.getCollection("changes");
		DBCursor allChanges = changesColl.find(new BasicDBObject("fileId", 2996))
				.sort(new BasicDBObject("date", 1));
		HashSet<Integer> traningCommitIds = new HashSet<Integer>();
		int trainingChanges = 0, testingChanges = 0;
		while (allChanges.hasNext()) {
			DBObject c = allChanges.next();
			Integer commitId = (Integer)c.get("commitId");
			if (traningCommitIds.size() < cutOff || traningCommitIds.contains(commitId)) {
				trainingChanges++;
				traningCommitIds.add(commitId);
				for (ChangeReducer r : traningReducers) {
					r.add(c);
				}
			} else {
				testingChanges++;
				for (ChangeReducer r : testReducers) {
					r.add(c);
				}
			}
		}
		LogManager.getLogger().info("Training changes: " + trainingChanges +
				"\nTesting changes: " + testingChanges);
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ChangesPerCommit trainingChangesPerCommit = new ChangesPerCommit(),
				testChangesPerCommit = new ChangesPerCommit();
		ChangesPerCategory trainingChangesPerCategory = new ChangesPerCategory(),
				testChangesPerCategory = new ChangesPerCategory();
		ChangeSplitter splitter = new ChangeSplitter(100, 
				Arrays.asList(trainingChangesPerCategory, trainingChangesPerCommit),
				Arrays.asList(testChangesPerCategory, testChangesPerCommit));
		splitter.split();
		
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection collection = mongo.getCollection("trainingChangesPerCommit");
		HashMap<Integer, Integer> changesPerCommit = trainingChangesPerCommit.getCounters();
		for (Integer key: changesPerCommit.keySet()) {
			collection.insert(new BasicDBObject("_id", key)
			.append("freq", changesPerCommit.get(key)));
		}
		
		collection = mongo.getCollection("testChangesPerCommit");
		changesPerCommit = testChangesPerCommit.getCounters();
		for (Integer key: changesPerCommit.keySet()) {
			collection.insert(new BasicDBObject("_id", key)
			.append("freq", changesPerCommit.get(key)));
		}
		
		collection = mongo.getCollection("trainingChangesPerCategory");
		HashMap<BasicDBObject, Integer> changesPerCatergory = trainingChangesPerCategory.getCounters();
		for (BasicDBObject key: changesPerCatergory.keySet()) {
			collection.insert(new BasicDBObject("_id", key).
					append("freq", changesPerCatergory.get(key)));
		}

		collection = mongo.getCollection("testChangesPerCategory");
		changesPerCatergory = testChangesPerCategory.getCounters();
		for (BasicDBObject key: changesPerCatergory.keySet()) {
			collection.insert(new BasicDBObject("_id", key).
					append("freq", changesPerCatergory.get(key)));
		}
	}

}
