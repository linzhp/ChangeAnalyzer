package edu.ucsc.cs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class DistributionImporter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		MongoClient mongo = new MongoClient();
		DB db = mongo.getDB("Evolution");
		DBCollection coll = db.getCollection("distribution");
		
		BufferedReader br = new BufferedReader(new FileReader("../../analysis/" +
				args[0] + "-api-freq.csv"));
		String[] headers = br.readLine().split(","); // read header
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] record = line.split(",");
			BasicDBObject query = new BasicDBObject();
			for (int i = 0; i < 4; i++) {
				query.append(headers[i], record[i]);
			}
			coll.update(query, 
					new BasicDBObject("$set", new BasicDBObject(args[0], record[5])), 
					true, false);
		}
		br.close();
	}

}
