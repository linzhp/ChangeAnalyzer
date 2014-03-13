package edu.ucsc.cs.analysis;

import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.ucsc.cs.utils.DatabaseManager;

public class ChangesPerSpecies implements ChangeReducer {
	private HashMap<BasicDBObject, Integer> counters = new HashMap<BasicDBObject, Integer>();
	private String setName;
	
	public ChangesPerSpecies(String setName) {
		this.setName = setName;
	}

	@Override
	public void add(int epochId, DBObject change) {
		BasicDBObject changeCategory = new BasicDBObject("changeType", change.get("changeType"))
		.append("entity", change.get("entity")).append("changeClass", change.get("changeClass"));
		switch ((String)change.get("changeClass")) {
		case "Update": changeCategory.append("newEntity", change.get("newEntity")); break;
		case "Move": changeCategory.append("newParentEntity", change.get("newParentEntity")); break;
		case "Insert":
		case "Delete":
			changeCategory.append("parentEntity", change.get("parentEntity")); break;
		}
		
		if (counters.containsKey(changeCategory)) {
			counters.put(changeCategory, counters.get(changeCategory) + 1);
		} else {
			counters.put(changeCategory, 1);
		}
	}
	
	@Override
	public void done() {
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection collection = mongo.getCollection("changesPerSpecies");
		for (BasicDBObject key: counters.keySet()) {
			collection.insert(new BasicDBObject("change", key).
					append("freq", counters.get(key))
					.append("set", setName));
		}
	}
}
