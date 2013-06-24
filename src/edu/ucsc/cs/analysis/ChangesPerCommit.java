package edu.ucsc.cs.analysis;

import java.util.HashMap;

import com.mongodb.DBObject;

public class ChangesPerCommit implements ChangeReducer {
	private HashMap<Integer, Integer> counters = new HashMap<Integer, Integer>(); 

	@Override
	public void add(DBObject change) {
		Integer commitId = (Integer) change.get("commitId");
		if (counters.containsKey(commitId)) {
			counters.put(commitId, counters.get(commitId) + 1);
		} else {
			counters.put(commitId, 1);
		}
	}

	HashMap<Integer, Integer> getCounters() {
		return this.counters;
	}
}
