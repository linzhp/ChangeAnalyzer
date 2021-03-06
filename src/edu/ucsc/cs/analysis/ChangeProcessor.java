package edu.ucsc.cs.analysis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public abstract class ChangeProcessor {
	public abstract void add(List<SourceCodeChange> changes, FileRevision fv) throws IOException, SQLException;
}
