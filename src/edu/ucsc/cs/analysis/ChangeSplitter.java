package edu.ucsc.cs.analysis;

import static java.lang.System.out;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.FileUtils;

/**
 * Split changes into training set and test set
 * @author linzhp
 *
 */
public class ChangeSplitter {
	private final int FILE_ID = 2996;
	private List<? extends ChangeReducer> trainingReducers;
	private List<? extends ChangeReducer> testReducers;
	private DBCollection changesColl;
	public ChangeSplitter(List<? extends ChangeReducer> trainingReducers,
			List<? extends ChangeReducer> testReducers) {
		this.trainingReducers = trainingReducers;
		this.testReducers = testReducers;
		DB mongo = DatabaseManager.getMongoDB();
		changesColl = mongo.getCollection("changes");
	}
	
	public void splitByTime(String splitDateStr, int monthsTraining, int monthsTesting) throws SQLException, IOException {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		DateTime splitDate = fmt.parseDateTime(splitDateStr);
		DateTime startTraining = splitDate.minusMonths(monthsTraining);
		DateTime endTesting = splitDate.plusMonths(monthsTesting);
		int cuttingCommit = -1;
		// Collecting training changes
		DBCursor trainingChanges = changesColl.find(new BasicDBObject("fileId", FILE_ID).
				append("date", new BasicDBObject("$lte", splitDateStr)))
				.sort(new BasicDBObject("date", -1));
		while (trainingChanges.hasNext()) {
			DBObject c = trainingChanges.next();
			if (cuttingCommit == -1) {
				cuttingCommit = (int)c.get("commitId");
			}
			if (fmt.parseDateTime((String)c.get("date")).isBefore(startTraining)) {
				break;
			}
			for (ChangeReducer reducer : trainingReducers) {
				reducer.add((int)c.get("commitId"), c);
			}
		}
		// Collecting test changes
		DBCursor testChanges = changesColl.find(new BasicDBObject("fileId", FILE_ID)
		.append("date", new BasicDBObject("$gt", splitDateStr)))
		.sort(new BasicDBObject("date", 1));
		int endCommit = -1;
		while (testChanges.hasNext()) {
			DBObject c = testChanges.next();
			endCommit = (int)c.get("commitId");
			if (fmt.parseDateTime((String)c.get("date")).isAfter(endTesting)) {
				break;
			}
			for (ChangeReducer reducer : testReducers) {
				reducer.add((int)c.get("commitId"), c);
			}
		}
		
		String content = FileUtils.getContent(FILE_ID, cuttingCommit);
		out.println("Last revision (" + cuttingCommit + ") in the training set:");
		FileUtils.printStatics(content);
		if (content != null) {
			FileWriter fw = new FileWriter("TrainingEnd.java");
			fw.write(content);
			fw.close();			
		}
		
		content = FileUtils.getContent(FILE_ID, endCommit);
		out.println("Last revision (" + endCommit + ") in the test set:");
		FileUtils.printStatics(content);
	}
	
	public void splitByCommit(double splitRatio, int trainingSize, int testSize) throws SQLException, IOException {
		TreeSet<Integer> commitIdSet = new TreeSet<Integer>();
		LinkedList<Integer> commitIdList = new LinkedList<>();
		DBCursor allChanges = changesColl.find(new BasicDBObject("fileId", FILE_ID))
				.sort(new BasicDBObject("date", 1));
		for (DBObject c : allChanges) {
			Integer commitId = (Integer)c.get("commitId");
			if (!commitIdSet.contains(commitId)) {
				commitIdSet.add(commitId);
				commitIdList.add(commitId);
			}
		}
		Integer[] commitIds = commitIdList.toArray(new Integer[commitIdSet.size()]);
		double splittingPosition = commitIds.length * splitRatio;
		if (splittingPosition < 1 || splittingPosition > commitIds.length) {
			System.err.println("Invalid splitting position");
			System.exit(1);
		}
		int cuttingCommit = commitIds[(int)splittingPosition - 1], 
				endCommit;
		if (splittingPosition + testSize >= commitIds.length) {
			endCommit = commitIds[commitIds.length - 1];
		} else {
			endCommit = commitIds[(int)splittingPosition + testSize];
		}
		
		// Collecting training commits
		for (int i = 1; i <= trainingSize; i++) {
			int index = (int)splittingPosition - i;
			if (index < 0)
				break;
			int commitId = commitIds[index];
			DBCursor changes = changesColl.find(new BasicDBObject("fileId", FILE_ID).append("commitId", commitId));
			while (changes.hasNext()) {
				DBObject c = changes.next();
				for (ChangeReducer reducer : trainingReducers) {
					reducer.add(commitId, c);
				}
			}
		}
		// Collecting test commits
		for (int i = 0; i < testSize; i++) {
			int index = (int)splittingPosition + i;
			if (index >= commitIds.length) {
				break;
			}
			int commitId = commitIds[index];
			out.println(commitId);
			DBCursor changes = changesColl.find(new BasicDBObject("fileId", FILE_ID).append("commitId", commitId));
			while (changes.hasNext()) {
				DBObject c = changes.next();
				for (ChangeReducer reducer : testReducers) {
					reducer.add(commitId, c);
				}
			}
		}
		
		String content = FileUtils.getContent(FILE_ID, cuttingCommit);
		out.println("Last version in the training set:");
		FileUtils.printStatics(content);
		if (content != null) {
			FileWriter fw = new FileWriter("TrainingEnd.java");
			fw.write(content);
			fw.close();			
		}
		
		content = FileUtils.getContent(FILE_ID, endCommit);
		out.println("Last version in the test set:");
		FileUtils.printStatics(content);
	}


	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws SQLException, IOException {
		ChangesPerTypeEpoch cpte = new ChangesPerTypeEpoch();
		ChangeSplitter splitter = new ChangeSplitter(
				Arrays.asList(
						cpte
						),
				Arrays.asList(
						cpte
						));
		splitter.splitByCommit(1, 400, 20);		
	}

}
