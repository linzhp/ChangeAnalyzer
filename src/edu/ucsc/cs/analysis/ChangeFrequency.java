package edu.ucsc.cs.analysis;

import java.util.HashMap;
import java.util.HashSet;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.LogManager;

public class ChangeFrequency {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int cutOff = 100;
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection changesColl = mongo.getCollection("changes");
		DBCursor allChanges = changesColl.find(new BasicDBObject("fileId", 2996))
				.sort(new BasicDBObject("date", 1));
		HashSet<Integer> traningCommitIds = new HashSet<Integer>();
		HashMap<String, Integer> training = new HashMap<String, Integer>(),
				testing = new HashMap<String, Integer>();
		int trainingChanges = 0, testingChanges = 0;
		while (allChanges.hasNext()) {
			DBObject c = allChanges.next();
			BasicDBObject changeCategory = new BasicDBObject("changeType", c.get("changeType"))
			.append("entity", c.get("entity")).append("changeClass", c.get("changeClass"));
			switch ((String)c.get("changeClass")) {
			case "Update": changeCategory.append("newEntity", c.get("newEntity")); break;
			case "Move": changeCategory.append("newParentEntity", c.get("newParentEntity")); break;
			case "Insert": changeCategory.append("parentEntity", c.get("parentEntity")); break;
			}
			String json = changeCategory.toString();
			Integer commitId = (Integer)c.get("commitId");
			if (traningCommitIds.size() < cutOff || traningCommitIds.contains(commitId)) {
				trainingChanges++;
				if (training.containsKey(json)) {
					training.put(json, training.get(json) + 1);
				} else {
					training.put(json, 1);
				}
				traningCommitIds.add(commitId);
			} else {
				testingChanges++;
				if (testing.containsKey(json)) {
					testing.put(json, testing.get(json) + 1);
				} else {
					testing.put(json, 1);
				}				
			}
		}
		DBCollection trainingColl = mongo.getCollection("training");
		for (String key: training.keySet()) {
			trainingColl.insert(new BasicDBObject("_id", key)
			.append("freq", training.get(key)));
		}
		
		DBCollection testingColl = mongo.getCollection("testing");
		for (String key: testing.keySet()) {
			testingColl.insert(new BasicDBObject("_id", key).
					append("freq", testing.get(key)));
		}
		LogManager.getLogger().info("Training changes: " + trainingChanges +
				"\nTesting changes: " + testingChanges);
	}

}
