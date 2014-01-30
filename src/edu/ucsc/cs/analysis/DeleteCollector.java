package edu.ucsc.cs.analysis;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public class DeleteCollector extends SubChangeCollector {

	public DeleteCollector(FineChange change) {
		super(change);
	}

	@Override
	protected SourceCodeChange newChange(ASTNode node,
			SourceCodeEntity parentEntity) {
		ChangeType cType = null;
		if (node instanceof TypeDeclaration) {
			cType = ChangeType.REMOVED_CLASS;
		} else if (node instanceof AbstractMethodDeclaration) {
			cType = ChangeType.REMOVED_FUNCTIONALITY;
		} else if (node instanceof FieldDeclaration) {
			cType = ChangeType.REMOVED_OBJECT_STATE;
		} else if (node instanceof Statement) {
			cType = ChangeType.STATEMENT_DELETE;
		} else if (node instanceof Javadoc) {
			cType = ChangeType.DOC_DELETE;
		}
		SourceCodeEntity thisEntity = new SourceCodeEntity(null, 
				converter.convertNode(node), null);
		return new Delete(cType, null, thisEntity, parentEntity);
	}

}
