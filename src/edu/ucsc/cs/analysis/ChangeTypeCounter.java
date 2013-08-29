package edu.ucsc.cs.analysis;

import java.util.HashMap;

import com.mongodb.DBObject;

public class ChangeTypeCounter implements ChangeReducer {

	private HashMap<String, Record> counters;
	private String setName;

	
	public ChangeTypeCounter(String setName, HashMap<String, Record> counters) {
		this.setName = setName;
		this.counters = counters;
	}
	
	@Override
	public void add(int epochId, DBObject change) {
		String changeType = (String)change.get("changeType");
		if (counters.containsKey(changeType)) {
			counters.get(changeType).increase(setName, 1);
		} else {
			Record r = new Record();
			r.increase(setName, 1);
			counters.put(changeType, r);			
		}
	}

}
