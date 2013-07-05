package edu.ucsc.cs.simulation;

import com.mongodb.BasicDBObject;

public interface Modifier {

	public abstract void modify(BasicDBObject object);

}