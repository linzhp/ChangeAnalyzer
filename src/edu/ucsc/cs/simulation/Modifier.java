package edu.ucsc.cs.simulation;

import com.mongodb.BasicDBObject;

public abstract class Modifier {
	protected Indexer indexer;
	
	public Modifier(Indexer indexer) {
		this.indexer = indexer;
	}

	public abstract void modify(BasicDBObject object);

}