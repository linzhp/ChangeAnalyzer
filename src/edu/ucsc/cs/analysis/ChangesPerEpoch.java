package edu.ucsc.cs.analysis;

import java.util.HashMap;

import com.mongodb.DBObject;

public class ChangesPerEpoch implements ChangeReducer {
	private HashMap<Integer, Integer> counters = new HashMap<Integer, Integer>(); 

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
}
