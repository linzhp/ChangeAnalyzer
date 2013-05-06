package edu.ucsc.cs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class FrequencyImporter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		MongoClient mongo = new MongoClient();
		DB db = mongo.getDB("Evolution");
		DBCollection coll = db.getCollection("frequencies");
		
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		String[] headers = br.readLine().split(","); // read header
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] record = line.split(",");
			BasicDBObject query = new BasicDBObject();
			int numCol = headers.length;
			int i;
			for (i = 0; i < numCol - 1; i++) {
				query.append(headers[i], record[i]);
			}
			coll.update(query, new BasicDBObject("$set", new BasicDBObject(headers[i], record[i])), true, false);
		}
		br.close();
	}

}
