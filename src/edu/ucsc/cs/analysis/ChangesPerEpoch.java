package edu.ucsc.cs.analysis;

import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.ucsc.cs.utils.DatabaseManager;

public class ChangesPerEpoch implements ChangeReducer {
	private HashMap<Integer, Integer> counters = new HashMap<Integer, Integer>(); 
	private String setName;
	
	public ChangesPerEpoch(String setName) {
		this.setName = setName;
	}
	
	
	@Override
	public void add(int epochId, DBObject change) {
		if (counters.containsKey(epochId)) {
			counters.put(epochId, counters.get(epochId) + 1);
		} else {
			counters.put(epochId, 1);
		}
	}

	HashMap<Integer, Integer> getCounters() {
		return this.counters;
	}

	@Override
	public void done() {
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection collection = mongo.getCollection("changesPerEpoch");
		for (Integer key: counters.keySet()) {
			collection.insert(new BasicDBObject("commit_id", key)
			.append("freq", counters.get(key))
			.append("set", setName));
		}
	}
}
