package edu.ucsc.cs.analysis;

import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.ucsc.cs.utils.DatabaseManager;

public class ChangesPerTypeEpoch implements ChangeReducer {
	HashMap<Integer, HashMap<String, Integer>> epoches = new HashMap<>();
	
	@Override
	public void add(int epochId, DBObject change) {
		HashMap<String, Integer> changeCounts;
		if (epoches.containsKey(epochId)) {
			changeCounts = epoches.get(epochId);
		} else {
			changeCounts = new HashMap<>();
			epoches.put(epochId, changeCounts);
		}
		String changeType = change.get("changeType").toString();
		if (changeCounts.containsKey(changeType)) {
			changeCounts.put(changeType, changeCounts.get(changeType) + 1);
		} else {
			changeCounts.put(changeType, 1);
		}
	}

	@Override
	public void done() {
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection collection = mongo.getCollection("changesPerTypeEpoch");
		for (int commitId : epoches.keySet()) {
			HashMap<String, Integer> changeCounts = epoches.get(commitId);
			BasicDBObject dbObject = new BasicDBObject("_id", commitId);
			for (String changeType : changeCounts.keySet()) {
				dbObject.append(changeType, changeCounts.get(changeType));
			}
			collection.insert(dbObject);
		}
	}

	public static void main(String[] args) {
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection collection = mongo.getCollection("changes");
		DBCursor allChanges = collection.find(new BasicDBObject("repoId", Integer.valueOf(args[0])));
		ChangesPerTypeEpoch cpte = new ChangesPerTypeEpoch();
		for (DBObject c : allChanges) {
			int commitId = (int)c.get("commitId");
			cpte.add(commitId, c);
		}
		cpte.done();
	}
}
