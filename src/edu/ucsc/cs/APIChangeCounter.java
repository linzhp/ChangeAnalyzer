package edu.ucsc.cs;

import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class APIChangeCounter extends ChangeReducer {

	@Override
	public void add(List<SourceCodeChange> changes) {
		for(SourceCodeChange c : changes) {
			ChangeType changeType = c.getChangeType();
			switch(c.getLabel()) {
			case "ADDING_ATTRIBUTE_MODIFIABILITY":
			case "ADDING_METHOD_OVERRIDABILITY":
			case "ADDITIONAL_FUNCTIONALITY":
			case "ADDITIONAL_OBJECT_STATE":
			case "ATTRIBUTE_RENAMING":
			case "ATTRIBUTE_TYPE_CHANGE":
			case "CLASS_RENAMING":
			case "DECREASING_ACCESSIBILITY_CHANGE":
			case "INCREASING_ACCESSIBILITY_CHANGE":
			case "METHOD_RENAMING":
			case "PARAMETER_DELETE":
			case "PARAMETER_INSERT":
			case "PARAMETER_ORDERING_CHANGE":
			case "PARAMETER_RENAMING":
			case "PARAMETER_TYPE_CHANGE":
			case "PARENT_CLASS_CHANGE":
			case "PARENT_CLASS_DELETE":
			case "PARENT_CLASS_INSERT":
			case "PARENT_INTERFACE_CHANGE":
			case "PARENT_INTERFACE_DELETE":
			case "PARENT_INTERFACE_INSERT":
			case "REMOVED_FUNCTIONALITY":
			case "REMOVED_OBJECT_STATE":
			case "REMOVING_ATTRIBUTE_MODIFIABILITY":
			case "REMOVING_CLASS_DERIVABILITY":
			case "REMOVING_METHOD_OVERRIDABILITY":
			case "RETURN_TYPE_CHANGE":
			case "RETURN_TYPE_DELETE":
			case "RETURN_TYPE_INSERT":
			}
		}
	}

}
