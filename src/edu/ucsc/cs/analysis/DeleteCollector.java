package edu.ucsc.cs.analysis;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public class DeleteCollector extends SubChangeCollector {

	public DeleteCollector(int start, int end) {
		super(start, end);
	}

	@Override
	protected SourceCodeChange newChange(SourceCodeEntity thisEntity,
			SourceCodeEntity parentEntity) {
		return new Delete(null, thisEntity, parentEntity);
	}

}
