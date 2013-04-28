package edu.ucsc.cs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class BasicFreqCounter extends ChangeReducer {
	public HashMap<String, Integer> changeFrequencies = new HashMap<String, Integer>();

	@Override
	public void add(List<SourceCodeChange> changes) {
		for (SourceCodeChange c : changes) {
			String category = c.getLabel();
			Integer count = changeFrequencies.get(category);
			if (count == null) {
				count = 1;
			} else {
				count++;				
			}
			changeFrequencies.put(category, count);
		}
	}
	

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		BasicFreqCounter reducer = new BasicFreqCounter();
//		Distribution dist = new Distribution(1, Arrays.asList(641, 1165)); // voldemort local
		new Repository(9, new ArrayList<Integer>(), reducer);
		String[] changeTypes = reducer.changeFrequencies.keySet().toArray(new String[0]);
		Arrays.sort(changeTypes);
		System.out.println("changeType,freq");
		for(String changeType : changeTypes) {
			System.out.println(changeType + ',' + reducer.changeFrequencies.get(changeType));
		}
	}
}
