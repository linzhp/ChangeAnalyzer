package edu.ucsc.cs.analysis;

import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public class InsertCollector extends SubChangeCollector {
	
	public InsertCollector(int start, int end) {
		super(start, end);
	}

	@Override
	protected SourceCodeChange newChange(SourceCodeEntity thisEntity,
			SourceCodeEntity parentEntity) {
		return new Insert(null, thisEntity, parentEntity);
	}

}
