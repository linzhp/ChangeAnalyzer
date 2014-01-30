package edu.ucsc.cs.analysis;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Logger;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.LogManager;

public class ChangeExtractor extends ChangeProcessor {

	private DBCollection collection;

	public ChangeExtractor() {
		collection = DatabaseManager.getMongoDB().getCollection("changes");
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		ChangeExtractor reducer = new ChangeExtractor();
		Repository repo = new Repository(1, reducer);
		repo.extractChanges(null);
		System.out.println("Time spent: "
				+ (System.currentTimeMillis() - start) / 1000 + " seconds");
	}

	@Override
	public void add(FineChange change) throws IOException, SQLException {
		ResultSet action = change.action;
		Timestamp date = action.getTimestamp("date");
		Logger logger = LogManager.getLogger();
		SourceCodeChange c = change.change;
		BasicDBObject dbObj = new BasicDBObject("fileId",
				action.getInt("file_id"))
				.append("commitId", action.getInt("commit_id"))
				.append("date", date.toString().split("\\.")[0]) // yyyy-mm-dd hh:mm:ss
				.append("changeType", c.getLabel())
				.append("entity", c.getChangedEntity().getLabel())
				.append("changeClass", c.getClass().getSimpleName());
		if (c instanceof Update) {
			dbObj.append("newEntity", ((Update) c).getNewEntity().getLabel());
		} else if (c instanceof Insert || c instanceof Delete) {
			SourceCodeEntity parentEntity = c.getParentEntity();
			if (parentEntity != null && parentEntity.getType() != null) {
				EntityType parentType = parentEntity.getType();
				if (parentType != null) {
					dbObj.append("parentEntity", parentType.toString());
				}
			}
		} else if (c instanceof Move) {
			Move m = (Move) c;
			dbObj.append("newParentEntity", m.getNewParentEntity().getLabel());
			if (m.getNewEntity().getType() != m.getChangedEntity().getType()) {
				logger.warning("entity changed when moving: "
						+ m.getChangedEntity() + "->" + m.getNewEntity());
			}
		}
		collection.insert(dbObj);
	}

}
