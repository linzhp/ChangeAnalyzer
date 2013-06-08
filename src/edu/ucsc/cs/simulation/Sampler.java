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
	
	public Sampler(double mean, double std) {
		changesPerCommit = new LogNormalDistribution(mean, std);
		ArrayList<Pair<BasicDBObject, Double>> freqs = new ArrayList<Pair<BasicDBObject, Double>>();
		DBCollection collection = DatabaseManager.getMongoDB().getCollection("contingency");
		DBCursor cursor = collection.find();
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			freqs.add(new Pair<BasicDBObject, Double>((BasicDBObject)obj.get("_id"), (Double)obj.get("value")));
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
		Sampler sampler = new Sampler(2.379116, 1.190405);
		for (int i = 0; i < 10; i++) {
			System.out.println(sampler.getNumChanges());
		}
	}
}
