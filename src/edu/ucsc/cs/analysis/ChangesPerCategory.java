package edu.ucsc.cs.analysis;

import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ChangesPerCategory implements ChangeReducer {
	private HashMap<BasicDBObject, Integer> counters = new HashMap<BasicDBObject, Integer>();
	public HashMap<BasicDBObject, Integer> getCounters() {
		return counters;
	}
	@Override
	public void add(DBObject change) {
		BasicDBObject changeCategory = new BasicDBObject("changeType", change.get("changeType"))
		.append("entity", change.get("entity")).append("changeClass", change.get("changeClass"));
		switch ((String)change.get("changeClass")) {
		case "Update": changeCategory.append("newEntity", change.get("newEntity")); break;
		case "Move": changeCategory.append("newParentEntity", change.get("newParentEntity")); break;
		case "Insert": changeCategory.append("parentEntity", change.get("parentEntity")); break;
		}
		
		if (counters.containsKey(changeCategory)) {
			counters.put(changeCategory, counters.get(changeCategory) + 1);
		} else {
			counters.put(changeCategory, 1);
		}
	}
	
	

}
