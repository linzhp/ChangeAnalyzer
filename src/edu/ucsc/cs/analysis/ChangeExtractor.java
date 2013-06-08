package edu.ucsc.cs.analysis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.LogManager;

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
	public void add(List<SourceCodeChange> changes, int fileID, int commitID) throws IOException, SQLException {
		Connection conn = DatabaseManager.getMySQLConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM scmlog WHERE id = " + commitID);
		Timestamp date = null;
		if (rs.next()) {
			date = rs.getTimestamp("commit_date");
		}
		stmt.close();
		for (SourceCodeChange c : changes) {
			BasicDBObject dbObj = new BasicDBObject("fileId", fileID)
			.append("commitId", commitID)
			.append("commitDate", date)
			.append("changeType", c.getLabel())
			.append("entity", c.getChangedEntity().getLabel())
			.append("changeClass", c.getClass().getSimpleName());
			if (c instanceof Update) {
				dbObj.append("newEntity", ((Update) c).getNewEntity().getLabel());
			} else if (c instanceof Insert) {
				dbObj.append("parentEntity", c.getParentEntity().getLabel());
			} else if (c instanceof Move) {
				Move m = (Move)c;
				dbObj.append("newParentEntity", m.getNewParentEntity().getLabel());
				if (m.getNewEntity().getType() != m.getChangedEntity().getType()) {
					LogManager.getLogger().warning("entity changed when moving: " + m.getChangedEntity() + "->" + m.getNewEntity());
				}
			}
			collection.insert(dbObj);
		}
	}

}
