package edu.ucsc.cs.analysis;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.ast.InvalidSyntaxException;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilationUtils;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import edu.ucsc.cs.utils.DatabaseManager;
import edu.ucsc.cs.utils.FileUtils;
import edu.ucsc.cs.utils.LogManager;

public class RepoFileDistiller {
	private ChangeProcessor reducer;
	private static Logger logger = LogManager.getLogger();
	private static HashMap<Integer, FileRevision> fileContentCache = new HashMap<Integer, FileRevision>();
	private CommitGraph commitGraph;
	private static final String[] sourceLevels = { "1.7", "1.6", "1.5", "1.4",
			"1.3", "1.2", "1.1" };
	private static final long[] versionConstants = { ClassFileConstants.JDK1_7,
			ClassFileConstants.JDK1_6, ClassFileConstants.JDK1_5,
			ClassFileConstants.JDK1_4, ClassFileConstants.JDK1_3,
			ClassFileConstants.JDK1_2, ClassFileConstants.JDK1_1 };
	private PreparedStatement fileCopiesStmt;
	private PreparedStatement fileInfoStmt;

	public RepoFileDistiller(ChangeProcessor reducer, CommitGraph commitGraph) throws SQLException {
		this.reducer = reducer;
		this.commitGraph = commitGraph;
		Connection conn = DatabaseManager.getSQLConnection();
		fileCopiesStmt = conn.prepareStatement(
				"SELECT * FROM file_copies WHERE action_id= ?");
		fileInfoStmt = conn.prepareStatement(
				"SELECT * FROM files WHERE id = ?");
	}
	
	@Override
	protected void finalize() throws SQLException {
		fileInfoStmt.close();
		fileCopiesStmt.close();
	}

	/**
	 * 
	 * @return false when the new file content is note parsable
	 */
	public boolean extractASTDelta(ResultSet action)
			throws SQLException, IOException {
		String actionType = action.getString("type");
		int fileId = action.getInt("file_id");
		int commitId = action.getInt("commit_id");
		logger.info("Extracting AST difference for file " + fileId + "@commit "
				+ commitId + " with action type " + actionType);
		switch (actionType) {
		case "C":
			// the file is created by copying from another file
			return processCopy(fileId, commitId, action.getInt("id"));
		case "M":
			return processModify(fileId, commitId);
		case "D":
			// a file is deleted
			processDelete(fileId, commitId);
			return false;
		case "A":
			// a file is added
			return processAdd(fileId, commitId);
		case "V":
			return processRename(fileId, commitId);
		default:
			// Not able to handle merge types, e.g., MM
			logger.warning("Unknown action type: " + actionType);
			return true;
		}
	}

	private void processDelete(int fileId, int commitId) throws IOException,
			SQLException {
		int previousCommitId = commitGraph.findPreviousCommitId(fileId,
				commitId);
		String content = getPreviousContent(fileId, previousCommitId);
		if (content == null) {
			logger.severe("Previous content for file " + fileId
					+ " at commit " + commitId
					+ " not found. Previous commit is " + previousCommitId);
		} else {
			List<SourceCodeChange> changes = extractChangesFromContent(
					new FileRevision(previousCommitId, fileId, content),
					ChangeType.REMOVED_CLASS);
			if (changes != null) {
				// using commitId instead of previousCommitId
				reducer.add(changes, new FileRevision(commitId, fileId, content));
			}
			fileContentCache.remove(fileId);
		}
	}

	private boolean processAdd(int fileId, int commitId) throws SQLException,
			IOException {
		String newContent = FileUtils.getContent(fileId, commitId);
		if (newContent == null) {
			logger.warning("Content for file " + fileId + " at commit_id "
					+ commitId + " not found");
			return false;
		}
		else {
			FileRevision fileRevision = new FileRevision(commitId, fileId,
					newContent);
			List<SourceCodeChange> changes = extractChangesFromContent(
					fileRevision, ChangeType.ADDITIONAL_CLASS);
			if (changes != null) {
				reducer.add(changes, fileRevision);
				fileContentCache.put(fileId, fileRevision);
				return true;
			} else {
				return false;
			}
		}
	}

	private static List<SourceCodeChange> extractChangesFromContent(
			FileRevision code, ChangeType changeType) {
		CompilationUnitDeclaration tree = JavaParser.parse(code);
		SubChangeCollector collector;
		if (changeType == ChangeType.ADDITIONAL_CLASS) {
			collector = new InsertCollector(0, Integer.MAX_VALUE);
		} else {
			collector = new DeleteCollector(0, Integer.MAX_VALUE);
		}
		if (tree != null) {
			tree.traverse(collector, tree.scope);
			return collector.getChanges();			
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @return false when the new source is not parsable
	 */
	private boolean processCopy(int fileId, int commitId, int actionId) throws SQLException,
			IOException {
		fileCopiesStmt.setInt(1, actionId);
		ResultSet copy = fileCopiesStmt.executeQuery();
		if (copy.next()) {
			int sourceFileId = copy.getInt("from_id");
			fileInfoStmt.setInt(1, sourceFileId);
			ResultSet fileInfo = fileInfoStmt.executeQuery();
			fileInfo.next();
			if (fileInfo.getString("file_name").endsWith(".java")) {
				int sourceCommitId = copy.getInt("from_commit_id");
				String sourceContent = FileUtils.getContent(
						sourceFileId, sourceCommitId);
				String targetContent = FileUtils.getContent(fileId, commitId);
				FileRevision fv = new FileRevision(commitId, fileId,
						targetContent);
				List<SourceCodeChange> changes = extractDiff(new FileRevision(
						sourceCommitId, sourceFileId, sourceContent), fv);
				if (changes != null) {
					reducer.add(changes, fv);
					fileContentCache.put(fileId, fv);
					return true;
				} else {
					return false;
				}
			}			
		}
		// unclear if the new source is parsable
		return true;
	}

	/**
	 * When old revision doesn't exist or is invalid, treat it as ADD. When new
	 * revision doesn't exist or is invalid, keep the old revision in cache
	 * 
	 * @return false if the new source is not parsable
	 */
	private boolean processModify(int fileId, int commitId) throws SQLException,
			IOException {
		int previousCommitId = commitGraph.findPreviousCommitId(fileId,
				commitId);
		String newContent = FileUtils.getContent(fileId, commitId);
		String oldContent = getPreviousContent(fileId, previousCommitId);
		FileRevision fileRevision = new FileRevision(commitId, fileId,
				newContent);
		List<SourceCodeChange> changes = extractDiff(new FileRevision(
				previousCommitId, fileId, oldContent), fileRevision);
		if (changes != null) {
			if (changes.size() > 0) {
				this.reducer.add(changes, fileRevision);
				fileContentCache.put(fileId, fileRevision);
			} else {
				logger.info("No changes distilled for file " + fileId
						+ " at commit_id " + commitId + " from previous commit id "
						+ previousCommitId);				
			}
			return true;
		} else {
			return false;
		}
	}

	private String getPreviousContent(int fileId, int previousCommitId)
			throws SQLException {
		FileRevision fileContent = fileContentCache.get(fileId);
		String oldContent = null;
		if (fileContent != null && fileContent.commitId == previousCommitId) {
			oldContent = fileContent.content;
		} else if (previousCommitId != -1) {
			oldContent = FileUtils.getContent(fileId, previousCommitId);
		}
		return oldContent;
	}

	private boolean processRename(int fileID, int commitID) throws SQLException,
			IOException {
		return processModify(fileID, commitID);
	}

	/**
	 * @return null when it failed to parse newSource
	 */
	public static List<SourceCodeChange> extractDiff(FileRevision oldSource,
			FileRevision newSource) throws IOException {
		if (oldSource.content == null && newSource.content != null) {
			return extractChangesFromContent(newSource,
					ChangeType.ADDITIONAL_CLASS);
		}
		if (newSource.content == null)
			return null;
		assert (oldSource.content != null && newSource.content != null);

		int newLevel = 0, oldLevel = 0;
		File newFile = FileUtils.javaFileFromString(newSource.content, "New "
				+ newSource.toString());
		File oldFile = FileUtils.javaFileFromString(oldSource.content, "Old "
				+ oldSource.toString());

		List<SourceCodeChange> changes = null;
		FileDistiller distiller = ChangeDistiller
				.createFileDistiller(Language.JAVA);
		while (changes == null) {
			try {
				distiller.extractClassifiedSourceCodeChanges(oldFile, sourceLevels[oldLevel],
						newFile, sourceLevels[newLevel]);
				
				changes = distiller.getSourceCodeChanges();
			} catch (InvalidSyntaxException e) {
				if (e.getFileName().startsWith("New ")) {
					if (newLevel < sourceLevels.length - 1) {
						logger.info("Failed to parse " + newSource
								+ " with source level "
								+ sourceLevels[newLevel++]
								+ ", trying source level "
								+ sourceLevels[newLevel]);
					} else {
						logger.warning("Failed to parse " + newSource);
						return null;
					}
				} else if (e.getFileName().startsWith("Old ")) {
					if (oldLevel < sourceLevels.length - 1) {
						logger.info("Failed to parse " + oldSource
								+ " with JDK " + sourceLevels[oldLevel++]
								+ ", trying JDK " + sourceLevels[oldLevel]);
					} else {
						logger.warning("Failed to parse " + oldSource);
						return extractChangesFromContent(newSource,
								ChangeType.ADDITIONAL_CLASS);
					}
				}
			}
		}
		newFile.delete();
		oldFile.delete();
		
		// Extracting sub-changes
		CompilationUnitDeclaration oldAST = JavaCompilationUtils.compile(
				oldSource.content, 
				oldSource.toString(), 
				versionConstants[oldLevel]).getCompilationUnit();
		CompilationUnitDeclaration newAST = JavaCompilationUtils.compile(
				newSource.content,
				newSource.toString(),
				versionConstants[newLevel]).getCompilationUnit();
		List<SourceCodeChange> subChanges = new LinkedList<SourceCodeChange>();

		for (SourceCodeChange c : changes) {
			SourceCodeEntity entity = c.getChangedEntity();
			int start = entity.getStartPosition();
			int end = entity.getEndPosition();
			SubChangeCollector collector = null;
			if (c instanceof Insert) {
				collector = new InsertCollector(start, end);
				newAST.traverse(collector, newAST.scope);
			} else if (c instanceof Delete) {
				collector = new DeleteCollector(start, end);
				oldAST.traverse(collector, oldAST.scope);
			}
			if (collector != null && collector.getChanges().size() > 0) {
				List<SourceCodeChange> currentChanges = collector.getChanges();
				// remove the out-most element, assuming pre-order search
				currentChanges.remove(0);
				subChanges.addAll(currentChanges);
			}
		}
		changes.addAll(subChanges);
		return changes;
	}
}
