package edu.ucsc.cs.analysis;

import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import edu.ucsc.cs.utils.DatabaseManager;

public class ZipfTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		Days days = Days.daysBetween(fmt.parseDateTime("2006-10-02 21:33:17"), fmt.parseDateTime("2012-06-18 22:54:17"));
		System.out.println(days.getDays());
	}

}
