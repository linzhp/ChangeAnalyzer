package edu.ucsc.cs.analysis;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public class DeleteClassCollector extends SubChangeCollector {

	public DeleteClassCollector(int start, int end) {
		super(start, end);
	}

	@Override
	protected SourceCodeChange newChange(ASTNode node,
			SourceCodeEntity parentEntity) {
		if (node instanceof TypeDeclaration) {
			ChangeType cType= ChangeType.REMOVED_CLASS;
			EntityType eType = converter.convertNode(node);
			SourceCodeEntity thisEntity = new SourceCodeEntity(
					StringUtils.join(qualifiers, "."), 
					eType, new SourceRange(node.sourceStart(), node.sourceEnd()));
			return new Delete(cType, null, thisEntity, parentEntity);
		} else {
			return null;			
		}
	}

}
