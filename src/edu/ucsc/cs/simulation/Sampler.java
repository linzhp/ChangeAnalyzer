package edu.ucsc.cs.simulation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.util.Pair;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.ucsc.cs.utils.DatabaseManager;

public class Sampler {
	private LogNormalDistribution changesPerCommit;
	private EnumeratedDistribution<BasicDBObject> changeTypes;
	
	/**
	 * Assuming the number of changes per commit follows a log normal distribution
	 * @param mean mean of the normal distribution
	 * @param std standard deviation of the normal distribution
	 */
	public Sampler(double mean, double std) {
		changesPerCommit = new LogNormalDistribution(mean, std);
		ArrayList<Pair<BasicDBObject, Double>> freqs = new ArrayList<Pair<BasicDBObject, Double>>();
		DBCollection collection = DatabaseManager.getMongoDB().getCollection("trainingChangesPerCategory");
		DBCursor cursor = collection.find();
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			freqs.add(new Pair<BasicDBObject, Double>((BasicDBObject)obj.get("_id"), (Double)obj.get("freq")));
		}
		changeTypes = new EnumeratedDistribution<BasicDBObject>(freqs);
	}
	
	private int getNumChanges() {
		return (int)this.changesPerCommit.sample();
	}
	
	public List<BasicDBObject> generateCommit() {
		int numChanges = getNumChanges();
		ArrayList<BasicDBObject> changes = new ArrayList<BasicDBObject>();
		for (int i=0; i<numChanges; i++) {
			changes.add(changeTypes.sample());
		}
		return changes;
	}
	
	
	
	public static void main(String[] args) {
		Sampler sampler = new Sampler(1.503785, 1.215351);
		for (int i = 0; i < 10; i++) {
			System.out.println(sampler.getNumChanges());
		}
	}
}
