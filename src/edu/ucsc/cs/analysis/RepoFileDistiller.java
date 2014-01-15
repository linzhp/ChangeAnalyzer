package edu.ucsc.cs.analysis;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.CompilationError;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;
import edu.ucsc.cs.utils.FileUtils;
import edu.ucsc.cs.utils.LogManager;

public class RepoFileDistiller {
	private ChangeProcessor reducer;
	private static Logger logger = LogManager.getLogger();
	private static HashMap<Integer, FileRevision> fileContentCache = new HashMap<Integer, FileRevision>();
	private CommitGraph commitGraph;
	private static final String[] sourceLevels = {"1.7", "1.6", "1.5", "1.4", "1.3", "1.2", "1.1"};

	public RepoFileDistiller(ChangeProcessor reducer) {
		this.reducer = reducer;
		commitGraph = new CommitGraph();
	}

	public void extractASTDelta(int fileId, int commitId, char actionType)
			throws SQLException, IOException {
		logger.info("Extracting AST difference for file " + fileId + "@commit "
				+ commitId + " with action type " + actionType);
		switch (actionType) {
		case 'C':
			// the file is created by copying from another file
			processCopy(fileId, commitId);
			break;
		case 'M':
			processModify(fileId, commitId);
			break;
		case 'D':
			// a file is deleted
			processDelete(fileId, commitId);
			break;
		case 'A':
			// a file is added
			processAdd(fileId, commitId);
			break;
		case 'V':
			processRename(fileId, commitId);
			break;
		}
		commitGraph.addCommit(fileId, commitId);
	}

	private void processDelete(int fileId, int commitId) throws IOException, SQLException {
		int previousCommitId = commitGraph.findPreviousCommitId(fileId, commitId);
		String content = getPreviousContent(fileId, previousCommitId);
		if (content == null) {
			logger.warning("Previous content for file " + fileId + " at commit "
					+ commitId + " not found. Previous commit is " + previousCommitId);
		} else {
			List<SourceCodeChange> changes = extractChangesFromContent(
					new FileRevision(previousCommitId, fileId, content), ChangeType.REMOVED_CLASS);
			if (changes != null) {
				reducer.add(changes, fileId, commitId);
			}
			fileContentCache.remove(fileId);						
		}
	}

	private void processAdd(int fileId, int commitId) throws SQLException, IOException {
		String newContent = FileUtils.getContent(fileId, commitId);
		if (newContent == null)
			logger.warning("Content for file " + fileId + " at commit_id "
					+ commitId + " not found");
		else {
	    	FileRevision fileRevision = new FileRevision(commitId, fileId, newContent);
			List<SourceCodeChange> changes = extractChangesFromContent(
	    			fileRevision, ChangeType.ADDITIONAL_CLASS);
			if (changes != null) {
		    	reducer.add(changes, fileId, commitId);				
				fileContentCache.put(fileId, fileRevision);
			}
		}
	}

	private static List<SourceCodeChange> extractChangesFromContent(FileRevision code, ChangeType changeType) {
		JavaParser parser = new JavaParser();
		ASTHelper<JavaStructureNode> astHelper = null;
		int i = 0;
		while (astHelper == null) {
			try {
				astHelper = 
						parser.getASTHelper(code.content, code.toString(), sourceLevels[i]);
			} catch (CompilationError e) {
				if (i < sourceLevels.length - 1) {
					logger.info("Failed to parse " + code + " with source level " + 
							sourceLevels[i++] + ", trying with " + sourceLevels[i]);
				} else {
					logger.warning("Failed to parse " + code);
					return null;
				}
			}
		}
		JavaStructureNode tree = astHelper.createStructureTree();
		JavaASTNodeTypeConverter converter = new JavaASTNodeTypeConverter();
		SourceCodeEntity parentEntity = new SourceCodeEntity(null, 
				converter.convertNode(tree.getASTNode()), null);
		List<SourceCodeChange> changes = new LinkedList<>();
		for (JavaStructureNode node : tree.getChildren()) {
			if (node.isClassOrInterface()) {
				SourceCodeEntity thisEntity = new SourceCodeEntity(null, 
						converter.convertNode(node.getASTNode()), null);
				if (changeType == ChangeType.ADDITIONAL_CLASS) {
					changes.add(
							new Insert(ChangeType.ADDITIONAL_CLASS, null, thisEntity, parentEntity));					
				} else {
					changes.add(new Delete(ChangeType.REMOVED_CLASS, null, thisEntity, parentEntity));
				}
			} else {
				logger.severe("Unexpected node: " + node);
			}
		}
		return changes;
	}

	private void processCopy(int fileID, int commitID) throws SQLException, IOException {
		// TODO many A status are classified by CVSAnalY as C
		processAdd(fileID, commitID);
	}

	/**
	 * When old revision doesn't exist or is invalid, treat it as ADD. When new revision doesn't exist or
	 * is invalid, keep the old revision in cache
	 * @param fileId
	 * @param commitId
	 * @throws SQLException
	 * @throws IOException
	 */
	private void processModify(int fileId, int commitId) throws SQLException,
			IOException {
		String newContent = FileUtils.getContent(fileId, commitId);
		int previousCommitId = commitGraph.findPreviousCommitId(fileId, commitId);
		String oldContent = getPreviousContent(fileId, previousCommitId);
		List<SourceCodeChange> changes = extractDiff(
				new FileRevision(previousCommitId, fileId, oldContent), 
				new FileRevision(commitId, fileId, newContent));
		if (changes == null || changes.size() == 0) {
			logger.warning("No changes distilled for file " + fileId
					+ " at commit_id " + commitId + " from previous commit id " + previousCommitId);
		} else {
			this.reducer.add(changes, fileId, commitId);			
		}
		if (changes != null) { // can't check newcontent alone, as it can have invalid syntax
			assert(newContent != null);
			fileContentCache.put(fileId, new FileRevision(commitId, fileId, newContent));
		}
	}

	private String getPreviousContent(int fileId, int previousCommitId)
			throws SQLException {
		FileRevision fileContent = fileContentCache.get(fileId);
		String oldContent = null;
		if (fileContent != null && fileContent.commitId == previousCommitId) {
			oldContent = fileContent.content;
		} else if (previousCommitId != -1){
			 oldContent = FileUtils.getContent(fileId, previousCommitId);			
		}
		return oldContent;
	}

	private void processRename(int fileID, int commitID) throws SQLException,
			IOException {
		processModify(fileID, commitID);
	}

	public static List<SourceCodeChange> extractDiff(FileRevision oldSource, FileRevision newSource) throws IOException {
		if (oldSource.content == null && newSource.content != null) {
			return extractChangesFromContent(newSource, ChangeType.ADDITIONAL_CLASS);
		}
		if (newSource.content == null)
			return null;
		assert(oldSource.content != null && newSource.content != null);
		
		int newLevel = 0, oldLevel = 0;
		File newFile = FileUtils.javaFileFromString(newSource.content, "New " + newSource.toString());
		File oldFile = FileUtils.javaFileFromString(oldSource.content, "Old " + oldSource.toString());
		
		List<SourceCodeChange> changes = null;
		while (changes == null) {
			try {
				changes = extractDiff(oldFile, sourceLevels[oldLevel], newFile, sourceLevels[newLevel]);
			} catch (CompilationError e) {
				if (e.fileName.startsWith("New ")) {
					if (newLevel < sourceLevels.length -1) {
						logger.info("Failed to parse " + newSource +
								" with source level " + sourceLevels[newLevel++] + 
								", trying source level " + sourceLevels[newLevel]);
					} else {
						logger.warning("Failed to parse " + newSource);
						return null;
					}
				} else if (e.fileName.startsWith("Old ")) {
					if (oldLevel < sourceLevels.length -1) {
						logger.info("Failed to parse " + oldSource +
								" with JDK " + sourceLevels[oldLevel++] + 
								", trying JDK " + sourceLevels[oldLevel]);
					} else {
						logger.warning("Failed to parse " + oldSource);
						return extractChangesFromContent(newSource, ChangeType.ADDITIONAL_CLASS);
					}
				}
			}
		} 
		newFile.delete();
		oldFile.delete();
		return changes;
	}
	
	public static List<SourceCodeChange> extractDiff(File oldFile, String oldSourceLevel, 
			File newFile, String newSourceLevel) {
		FileDistiller distiller = ChangeDistiller
				.createFileDistiller(Language.JAVA);
		distiller.extractClassifiedSourceCodeChanges(
				oldFile, oldSourceLevel, newFile, newSourceLevel);

		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		if (changes == null) {
			logger.info("No AST difference found");
		}
		return changes;
		
	}
}
