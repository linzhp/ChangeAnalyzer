package edu.ucsc.cs.analysis;

import java.util.*;

import com.mongodb.*;

import edu.ucsc.cs.utils.DatabaseManager;

public class StochasticGrammarBuilder {

	public StochasticGrammarBuilder() {
	}
	
	@SuppressWarnings("unchecked")
	public void build(int fileId) {
		DBCollection collection = DatabaseManager.getMongoDB().getCollection("changes");
		List<Integer> commitIds = collection.distinct("commitId");
		Collections.sort(commitIds);
		for (int commitId : commitIds) {
			DBCursor cursor = collection.find(new BasicDBObject("commitId", commitId));
			while (cursor.hasNext()) {
				DBObject change = cursor.next();
				
			}
		}
	}

}
