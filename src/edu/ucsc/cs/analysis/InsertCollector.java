package edu.ucsc.cs.analysis;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import edu.ucsc.cs.utils.LogManager;

public class InsertCollector extends SubChangeCollector {
	
	public InsertCollector(FineChange change) {
		super(change);
	}

	@Override
	protected SourceCodeChange newChange(ASTNode node,
			SourceCodeEntity parentEntity) {
		ChangeType cType = null;
		if (node instanceof TypeDeclaration) {
			cType = ChangeType.ADDITIONAL_CLASS;
		} else if (node instanceof AbstractMethodDeclaration) {
			cType = ChangeType.ADDITIONAL_FUNCTIONALITY;
		} else if (node instanceof FieldDeclaration) {
			cType = ChangeType.ADDITIONAL_OBJECT_STATE;
		} else if (node instanceof Statement) {
			cType = ChangeType.STATEMENT_INSERT;
		} else if (node instanceof Javadoc) {
			cType = ChangeType.DOC_INSERT;
		}
		EntityType eType = converter.convertNode(node);
		if (eType == null) {
			LogManager.getLogger().severe("Cannot convert " + node);
		}
		SourceCodeEntity thisEntity = new SourceCodeEntity(null, 
				eType, null);
		return new Insert(cType, null, thisEntity, parentEntity);
	}

}
