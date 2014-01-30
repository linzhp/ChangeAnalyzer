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

	public RepoFileDistiller(ChangeProcessor reducer, CommitGraph commitGraph) {
		this.reducer = reducer;
		this.commitGraph = commitGraph;
	}

	public void process(ResultSet action)
			throws SQLException, IOException {
		String actionType = action.getString("type");
		int fileId = action.getInt("file_id");
		int commitId = action.getInt("commit_id");
		logger.info("Extracting AST difference for file " + fileId + "@commit "
				+ commitId + " with action type " + actionType);
		switch (actionType) {
		case "C":
			// the file is created by copying from another file
			processCopy(action);
			break;
		case "M":
			processModify(action);
			break;
		case "D":
			// a file is deleted
			processDelete(action);
			break;
		case "A":
			// a file is added
			processAdd(action);
			break;
		case "V":
			processRename(action);
			break;
		}
	}

	private void processDelete(ResultSet action) throws IOException,
			SQLException {
		int fileId = action.getInt("file_id");
		int commitId = action.getInt("commit_id");
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
				reducer.add(changes, fileId, commitId);
			}
			fileContentCache.remove(fileId);
		}
	}

	private void processAdd(ResultSet action) throws SQLException,
			IOException {
		int fileId = action.getInt("file_id");
		int commitId = action.getInt("commit_id");
		String newContent = FileUtils.getContent(fileId, commitId);
		if (newContent == null)
			logger.warning("Content for file " + fileId + " at commit_id "
					+ commitId + " not found");
		else {
			FileRevision fileRevision = new FileRevision(commitId, fileId,
					newContent);
			List<SourceCodeChange> changes = extractChangesFromContent(
					fileRevision, ChangeType.ADDITIONAL_CLASS);
			if (changes != null) {
				reducer.add(changes, fileId, commitId);
				fileContentCache.put(fileId, fileRevision);
			}
		}
	}

	private static List<SourceCodeChange> extractChangesFromContent(
			FileRevision code, ChangeType changeType) {
		CompilationUnitDeclaration tree = null;
		int i = 0;
		while (tree == null) {
			try {
				tree = JavaCompilationUtils.compile(code.content,
						code.toString(), versionConstants[i])
						.getCompilationUnit();
			} catch (InvalidSyntaxException e) {
				if (i < sourceLevels.length - 1) {
					logger.info("Failed to parse " + code
							+ " with source level " + sourceLevels[i++]
							+ ", trying with " + sourceLevels[i]);
				} else {
					logger.warning("Failed to parse " + code);
					return null;
				}
			}
		}
		SubChangeCollector collector;
		if (changeType == ChangeType.ADDITIONAL_CLASS) {
			collector = new InsertCollector(0, Integer.MAX_VALUE);
		} else {
			collector = new DeleteCollector(0, Integer.MAX_VALUE);
		}
		tree.traverse(collector, tree.scope);
		return collector.getChanges();
	}

	private void processCopy(ResultSet action) throws SQLException,
			IOException {
		int fileId = action.getInt("file_id");
		int commitId = action.getInt("commit_id");
		Connection conn = DatabaseManager.getSQLConnection();
		PreparedStatement fileCopiesStmt = conn.prepareStatement(
				"SELECT * FROM file_copies WHERE action_id= ?");
		fileCopiesStmt.setInt(1, action.getInt("id"));
		ResultSet copy = fileCopiesStmt.executeQuery();
		if (copy.next()) {
			int sourceFileId = copy.getInt("from_id");
			
			PreparedStatement fileInfoStmt = conn.prepareStatement(
					"SELECT * FROM files WHERE id = " + sourceFileId);
			ResultSet fileInfo = fileInfoStmt.executeQuery();
			fileInfo.next();
			if (fileInfo.getString("file_name").endsWith(".java")) {
				int sourceCommitId = copy.getInt("from_commit_id");
				String sourceContent = FileUtils.getContent(
						sourceFileId, sourceCommitId);
				String targetContent = FileUtils.getContent(fileId, commitId);
				List<SourceCodeChange> changes = extractDiff(new FileRevision(
						sourceCommitId, sourceFileId, sourceContent), new FileRevision(
						commitId, fileId, targetContent));
				if (changes != null) {
					reducer.add(changes, fileId, commitId);
					fileContentCache.put(fileId, new FileRevision(commitId, fileId,
							targetContent));
				}				
			}
			fileInfoStmt.close();
			
		}
		fileCopiesStmt.close();
	}

	/**
	 * When old revision doesn't exist or is invalid, treat it as ADD. When new
	 * revision doesn't exist or is invalid, keep the old revision in cache
	 * 
	 * @param fileId
	 * @param commitId
	 * @throws SQLException
	 * @throws IOException
	 */
	private void processModify(ResultSet action) throws SQLException,
			IOException {
		int fileId = action.getInt("file_id");
		int commitId = action.getInt("commit_id");
		int previousCommitId = commitGraph.findPreviousCommitId(fileId,
				commitId);
		String newContent = FileUtils.getContent(fileId, commitId);
		String oldContent = getPreviousContent(fileId, previousCommitId);
		List<SourceCodeChange> changes = extractDiff(new FileRevision(
				previousCommitId, fileId, oldContent), new FileRevision(
				commitId, fileId, newContent));
		if (changes == null || changes.size() == 0) {
			logger.warning("No changes distilled for file " + fileId
					+ " at commit_id " + commitId + " from previous commit id "
					+ previousCommitId);
		} else {
			this.reducer.add(changes, fileId, commitId);
		}
		if (changes != null) { // can't check newcontent alone, as it can have
								// invalid syntax
			assert (newContent != null);
			fileContentCache.put(fileId, new FileRevision(commitId, fileId,
					newContent));
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

	private void processRename(ResultSet action) throws SQLException,
			IOException {
		processModify(action);
	}

	public static List<FineChange> extractDiff(FileRevision oldSource,
			FileRevision newSource) throws IOException {
		if (oldSource.content == null && newSource.content != null) {
			return extractChangesFromContent(newSource,
					ChangeType.ADDITIONAL_CLASS);
		}
		/*
		 * Don't treat old content != null and new content == null to be
		 */
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
		LinkedList<FineChange> fineChanges = new LinkedList<>();
		for (SourceCodeChange c : changes) {
			FineChange fChange = new FineChange(c);
			SubChangeCollector collector = null;
			if (c instanceof Insert) {
				collector = new InsertCollector(fChange);
				newAST.traverse(collector, newAST.scope);
			} else if (c instanceof Delete) {
				collector = new DeleteCollector(fChange);
				oldAST.traverse(collector, oldAST.scope);
			}
			if (collector != null && fChange.subChanges.size() > 0) {
				// remove the out-most element, assuming pre-order search
				fChange.subChanges.remove(0);
			}
			fineChanges.add(fChange);
		}
		return fineChanges;
	}
}
