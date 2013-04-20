package edu.ucsc.cs;

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
}
