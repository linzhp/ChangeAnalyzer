package edu.ucsc.cs.analysis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
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
	private int repoId;

	public ChangeExtractor(int repoId) {
		collection = DatabaseManager.getMongoDB().getCollection("changes");
		this.repoId = repoId;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		Integer repoId = Integer.valueOf(args[0]);
		ChangeExtractor reducer = new ChangeExtractor(repoId);
		Repository repo = new Repository(repoId, reducer);
		repo.extractChanges(null);
		System.out.println("Time spent: " + 
				(System.currentTimeMillis() - start)/1000 +
				" seconds");
	}

	@Override
	public void add(List<SourceCodeChange> changes, FileRevision fv) throws IOException, SQLException {
		Connection conn = DatabaseManager.getSQLConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM scmlog WHERE id = " + fv.commitId);
		Timestamp date = null;
		if (rs.next()) {
			date = rs.getTimestamp("date");
		}
		stmt.close();
		Logger logger = LogManager.getLogger();
		for (SourceCodeChange c : changes) {
			SourceCodeEntity changedEntity = c.getChangedEntity();
			BasicDBObject dbObj = new BasicDBObject("repoId", repoId)
			.append("fileId", fv.fileId)
			.append("commitId", fv.commitId)
			.append("date", date.toString().split("\\.")[0]) // yyyy-mm-dd hh:mm:ss
			.append("changeType", c.getLabel())
			.append("entity", entityToString(changedEntity));
			if (c instanceof Update) {
				dbObj.append("newEntity", entityToString(((Update) c).getNewEntity()));
			} else if (c instanceof Insert || c instanceof Delete) {
				SourceCodeEntity parentEntity = c.getParentEntity();
				if (parentEntity != null && parentEntity.getType() != null) {
					EntityType parentType = parentEntity.getType();
					if (parentType != null) {
						dbObj.append("parentEntity", parentType.toString());
					}
				}
				// extract parent class information
				if (changedEntity.getType() == JavaEntityType.CLASS) {
					Collection<String> names = getParentClassNames(
							changedEntity.getStartPosition(), 
							changedEntity.getEndPosition(), fv);
					dbObj.append("parentClasses", names);
				}
			} else if (c instanceof Move) {
				Move m = (Move)c;
				dbObj.append("newParentEntity", m.getNewParentEntity().getLabel());
				if (m.getNewEntity().getType() != m.getChangedEntity().getType()) {
					logger.warning("entity changed when moving: " + 
							m.getChangedEntity() + "->" + m.getNewEntity());
				}
			}
			collection.insert(dbObj);
		}
	}
	
	static String entityToString(SourceCodeEntity entity) {
		if (entity.getType().isType()) {
			return entity.getUniqueName();
		} else {
			return entity.getLabel();
		}
	}

	static Collection<String> getParentClassNames(int start, int end, FileRevision code) {
		CompilationUnitDeclaration tree = JavaParser.parse(code);
		ParentClassFinder visitor = new ParentClassFinder(start, end);
		tree.traverse(visitor, tree.scope);
		return visitor.getParentNames();
	}
}
