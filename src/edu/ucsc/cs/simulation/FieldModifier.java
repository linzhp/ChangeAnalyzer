package edu.ucsc.cs.simulation;

import com.mongodb.BasicDBObject;

import edu.ucsc.cs.utils.LogManager;

public class FieldModifier extends Modifier{
	public FieldModifier(Indexer indexer) {
		super(indexer);
	}

	@Override
	public void modify(BasicDBObject object) {
		String changeType = object.getString("changeType");
		LogManager.getLogger().warning(changeType + " is not supported");		
	}
}
