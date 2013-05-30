package edu.ucsc.cs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import edu.ucsc.cs.utils.DatabaseManager;

public class ChangeExtractor extends ChangeReducer {
	
	private DBCollection collection;

	public ChangeExtractor() {
		collection = DatabaseManager.getMongoDB().getCollection("changes");
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ChangeExtractor reducer = new ChangeExtractor();
		Repository repo = new Repository(1, reducer);
		repo.extractChanges(Arrays.asList(2996));
	}

	@Override
	public void add(List<SourceCodeChange> changes, int fileID, int commitID) throws IOException {
		for (SourceCodeChange c : changes) {
			BasicDBObject dbObj = new BasicDBObject("fileId", fileID)
			.append("commitId", commitID)
			.append("changeType", c.getLabel())
			.append("entity", c.getChangedEntity().getLabel())
			.append("changeClass", c.getClass().getSimpleName());
			if (c instanceof Update) {
				dbObj.append("newEntity", ((Update) c).getNewEntity().getLabel());
			} else if (c instanceof Insert) {
				dbObj.append("parentEntity", c.getParentEntity().getLabel());
			}
			collection.insert(dbObj);
		}
	}

}
