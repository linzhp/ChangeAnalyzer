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
	private static HashMap<Integer, FileContent> fileContentCache = new HashMap<Integer, FileContent>();
	private CommitGraph commitGraph;

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
			List<SourceCodeChange> changes = extractChangesFromContent(content, ChangeType.REMOVED_CLASS);
			reducer.add(changes, fileId, commitId);
			fileContentCache.remove(fileId);						
		}
	}

	private void processAdd(int fileId, int commitId) throws SQLException, IOException {
		String newContent = FileUtils.getContent(fileId, commitId);
		if (newContent == null)
			logger.warning("Content for file " + fileId + " at commit_id "
					+ commitId + " not found");
		else {
	    	List<SourceCodeChange> changes = extractChangesFromContent(newContent, ChangeType.ADDITIONAL_CLASS);
	    	reducer.add(changes, fileId, commitId);
			fileContentCache.put(fileId, new FileContent(commitId, newContent));
		}
	}

	private List<SourceCodeChange> extractChangesFromContent(String content, ChangeType changeType) {
		JavaParser parser = new JavaParser();
		ASTHelper<JavaStructureNode> astHelper;
		try {
			astHelper = 
					parser.getASTHelper(content, "file", "1.7");
		} catch (CompilationError e) {
			logger.info("Failed to parse with source level 1.7, trying with 1.4");
			astHelper = 
					parser.getASTHelper(content, "file", "1.4");
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
		processAdd(fileID, commitID);
	}

	private void processModify(int fileId, int commitId) throws SQLException,
			IOException {
		String newContent = FileUtils.getContent(fileId, commitId);
		int previousCommitId = commitGraph.findPreviousCommitId(fileId, commitId);
		String oldContent = getPreviousContent(fileId, previousCommitId);
		List<SourceCodeChange> changes = null;
		try {
			changes = extractDiff(oldContent, "1.6", newContent, "1.6");
		} catch (CompilationError error) {
			changes = extractDiff(oldContent, "1.4", newContent, "1.4");
		}
		if (changes == null || changes.size() == 0) {
			logger.warning("No changes distilled for file " + fileId
					+ " at commit_id " + commitId + " from previous commit id " + previousCommitId);
		} else {
			this.reducer.add(changes, fileId, commitId);			
		}
		if (newContent != null)
			fileContentCache.put(fileId, new FileContent(commitId, newContent));
	}

	private String getPreviousContent(int fileId, int previousCommitId)
			throws SQLException {
		FileContent fileContent = fileContentCache.get(fileId);
		String oldContent = null;
		if (fileContent != null && fileContent.commitID == previousCommitId) {
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

	public static List<SourceCodeChange> extractDiff(String oldContent, String oldSourceLevel,
			String newContent, String newSourceLevel) throws IOException {
		if (newContent == null || oldContent == null) {
			return null;
		}

		File newFile = FileUtils.javaFileFromString(newContent, "New");
		File oldFile = FileUtils.javaFileFromString(oldContent, "Old");
		return extractDiff(oldFile, oldSourceLevel, newFile, newSourceLevel);
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
