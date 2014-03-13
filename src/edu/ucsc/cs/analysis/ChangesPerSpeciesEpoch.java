package edu.ucsc.cs.analysis;

import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.ucsc.cs.utils.DatabaseManager;

public class ChangesPerSpeciesEpoch implements ChangeReducer {
	private HashMap<BasicDBObject, Integer> counters = new HashMap<BasicDBObject, Integer>();
	private String setName;
	public ChangesPerSpeciesEpoch(String setName) {
		this.setName = setName;
	}

	@Override
	public void add(int epochId, DBObject change) {
		BasicDBObject changeSpecies = new BasicDBObject("changeType", change.get("changeType"))
		.append("entity", change.get("entity")).append("changeClass", change.get("changeClass"))
		.append("commitId", change.get("commitId"));
		switch ((String)change.get("changeClass")) {
		case "Update": changeSpecies.append("newEntity", change.get("newEntity")); break;
		case "Move": changeSpecies.append("newParentEntity", change.get("newParentEntity")); break;
		case "Insert":
		case "Delete":
			changeSpecies.append("parentEntity", change.get("parentEntity")); break;
		}
		
		if (counters.containsKey(changeSpecies)) {
			counters.put(changeSpecies, counters.get(changeSpecies) + 1);
		} else {
			counters.put(changeSpecies, 1);
		}
	}
	@Override
	public void done() {
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection collection = mongo.getCollection("changesPerSpeciesEpoch");
		for (BasicDBObject key: counters.keySet()) {
			collection.insert(new BasicDBObject("change", key)
			.append("freq", counters.get(key))
			.append("set", setName));			
		}
		
	}

}
