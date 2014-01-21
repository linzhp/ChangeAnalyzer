package edu.ucsc.cs.simulation;

import java.sql.SQLException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.FileUtils;

public class ChangeValidator {

	public static void main(String[] args) throws SQLException {
		int[] commitIds = { 3545, 3559, 3565, 3566, 3611, 3683, 3685, 3708,
				3719, 3760, 3763, 3771, 3836, 3866, 3869, 3949, 3973, 3975,
				3976, 3977 };
		int fileId = 2996;
		DB mongo = DatabaseManager.getMongoDB();
		DBCollection changesColl = mongo.getCollection("changes");

		for (int cId : commitIds) {
			DBCursor changes = changesColl.find(new BasicDBObject("commitId",
					cId));
			int numAdded = 0;
			int numRemoved = 0;
			for (DBObject c : changes) {
				String changeType = (String) c.get("changeType");
				if (changeType.equals("ADDITIONAL_FUNCTIONALITY")) {
					numAdded++;
				} else if (changeType.equals("REMOVED_FUNCTIONALITY")) {
					numRemoved++;
				}
			}
			System.out.println("Commit " + cId);
			System.out.println("Added " + numAdded + " method, removed "
					+ numRemoved + " methods");
			String content = FileUtils.getContent(fileId, cId);
			FileUtils.printStatics(content);
		}
	}

}
