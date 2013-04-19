package edu.ucsc.cs;

import java.util.HashMap;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public abstract class ChangeReducer {
	public HashMap<String, Integer> changeFrequencies = new HashMap<String, Integer>();

	public abstract void add(List<SourceCodeChange> changes);
}
