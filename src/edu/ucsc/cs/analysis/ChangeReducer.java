package edu.ucsc.cs.analysis;

import com.mongodb.DBObject;

public interface ChangeReducer {
	public void add(DBObject change);
}
