package edu.ucsc.cs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class DistributionExporter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("distributions.csv"));
		String[] fields = {"ChangeType", "SourceCodeEntity", "SourceCodeChange", "NewSourceCodeEntity"};
		StringBuilder buffer = new StringBuilder();
		for (String field : fields) {
			buffer.append(field);
			buffer.append('/');			
		}
		buffer.replace(buffer.length()-1, buffer.length(), ",");
		for (String field : args) {
			buffer.append(field);
			buffer.append(',');
		}
		buffer.replace(buffer.length()-1, buffer.length(), "\n");
		bw.write(buffer.toString());

		MongoClient mongo = new MongoClient();
		DB db = mongo.getDB("Evolution");
		DBCollection coll = db.getCollection("contingency");
		DBCursor cursor = coll.find();
		while (cursor.hasNext()) {
			DBObject object = cursor.next();
			buffer = new StringBuilder();
			for (String field : fields) {
				buffer.append(object.get(field));
				buffer.append('/');				
			}
			buffer.replace(buffer.length()-1, buffer.length(), ",");
			for (String field : args) {
				if (object.containsField(field)) {
					buffer.append(object.get(field));
					buffer.append(',');
				} else {
					buffer.append("0,");
				}
			}
			buffer.replace(buffer.length()-1, buffer.length(), "\n");
			bw.write(buffer.toString());
		}
		bw.close();
	}

}
