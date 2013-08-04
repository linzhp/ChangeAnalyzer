package edu.ucsc.cs.analysis;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
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
		ChangeType cType;
		switch ((JavaEntityType)thisEntity.getType()) {
		case CLASS: cType = ChangeType.REMOVED_CLASS; break;
		case METHOD: cType = ChangeType.REMOVED_FUNCTIONALITY; break;
		case FIELD: cType = ChangeType.REMOVED_OBJECT_STATE; break;
		default: cType = null;
		}
		return new Delete(cType, null, thisEntity, parentEntity);
	}

}
