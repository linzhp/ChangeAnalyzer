package edu.ucsc.cs.analysis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
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
		ChangeExtractor reducer = new ChangeExtractor();
		Repository repo = new Repository(1, reducer);
		repo.extractChanges(Arrays.asList(2996));
	}

	@Override
	public void add(List<SourceCodeChange> changes, int fileID, int commitID) throws IOException, SQLException {
		Connection conn = DatabaseManager.getSQLConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM scmlog WHERE id = " + commitID);
		Timestamp date = null;
		if (rs.next()) {
			date = rs.getTimestamp("date");
		}
		stmt.close();
		Logger logger = LogManager.getLogger();
		for (SourceCodeChange c : changes) {
			BasicDBObject dbObj = new BasicDBObject("fileId", fileID)
			.append("commitId", commitID)
			.append("date", date.toString().split("\\.")[0]) // yyyy-mm-dd hh:mm:ss
			.append("changeType", c.getLabel())
			.append("entity", c.getChangedEntity().getLabel())
			.append("changeClass", c.getClass().getSimpleName());
			if (c instanceof Update) {
				dbObj.append("newEntity", ((Update) c).getNewEntity().getLabel());
			} else if (c instanceof Insert || c instanceof Delete) {
				dbObj.append("parentEntity", c.getParentEntity().getLabel());
			} else if (c instanceof Move) {
				Move m = (Move)c;
				dbObj.append("newParentEntity", m.getNewParentEntity().getLabel());
				if (m.getNewEntity().getType() != m.getChangedEntity().getType()) {
					logger.warning("entity changed when moving: " + m.getChangedEntity() + "->" + m.getNewEntity());
				}
			}
			collection.insert(dbObj);
		}
	}

}
