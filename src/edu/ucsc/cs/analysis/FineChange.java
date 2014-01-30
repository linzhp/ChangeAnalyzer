package edu.ucsc.cs.analysis;

import java.sql.ResultSet;
import java.util.LinkedList;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class FineChange {
	SourceCodeChange change;
	ResultSet action;
	LinkedList<SourceCodeChange> subChanges;

	public FineChange(SourceCodeChange change) {
		this.change = change;
		subChanges = new LinkedList<>();
	}
	
	public void setAction(ResultSet action) {
		this.action = action;
	}
	
	public void addSubChange(SourceCodeChange moreChange) {
		subChanges.add(moreChange);
	}
}
