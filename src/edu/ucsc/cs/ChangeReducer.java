package edu.ucsc.cs;

import java.io.IOException;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public abstract class ChangeReducer {
	public abstract void add(List<SourceCodeChange> changes, int fileID, int commitID) throws IOException;
}
