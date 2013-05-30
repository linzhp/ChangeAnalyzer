package edu.ucsc.cs.simulation;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import edu.ucsc.cs.utils.DatabaseManager;

public class Simulation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection collection = mongo.getCollection("changes");
		collection.mapReduce(
				"function() {" +
				"emit({changeType: this.changeType, entity: this.entity, changeClass: this.changeClass}" +
				", 1);}", 
				"function(key, values) {return Array.sum(values);}", 
				"contingency", new BasicDBObject("fileId", 2996));
	}

}
