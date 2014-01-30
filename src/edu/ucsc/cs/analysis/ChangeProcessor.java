package edu.ucsc.cs.analysis;

import java.io.IOException;
import java.sql.SQLException;

public abstract class ChangeProcessor {
	public abstract void add(FineChange change) throws IOException, SQLException;
}
