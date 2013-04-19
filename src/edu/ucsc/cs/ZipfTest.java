package edu.ucsc.cs;

import org.apache.commons.math3.distribution.ZipfDistribution;

public class ZipfTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ZipfDistribution dist = new ZipfDistribution(1000, 1);
		System.out.println(dist.probability(3));
	}

}
