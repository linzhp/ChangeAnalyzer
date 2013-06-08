package edu.ucsc.cs.analysis;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.*;

public class ZipfTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<Pair<String, Double>> probs = new ArrayList<Pair<String, Double>>();
		probs.add(new Pair<String, Double>("insert statement", 700.0));
		probs.add(new Pair<String, Double>("delete statement", 400.0));
		EnumeratedDistribution<String> dist = new EnumeratedDistribution<String>(probs);
		System.out.println(dist.sample());
	}

}
