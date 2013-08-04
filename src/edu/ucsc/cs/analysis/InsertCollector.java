package edu.ucsc.cs.analysis;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
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
		ChangeType cType;
		switch ((JavaEntityType)thisEntity.getType()) {
		case CLASS: cType = ChangeType.ADDITIONAL_CLASS; break;
		case METHOD: cType = ChangeType.ADDITIONAL_FUNCTIONALITY; break;
		case FIELD: cType = ChangeType.ADDITIONAL_OBJECT_STATE; break;
		default: cType = null;
		}
		return new Insert(cType, null, thisEntity, parentEntity);
	}

}
