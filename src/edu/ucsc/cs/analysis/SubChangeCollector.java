package edu.ucsc.cs.analysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public abstract class SubChangeCollector extends RangeVisitor {

	protected List<SourceCodeChange> changes = new LinkedList<SourceCodeChange>();
	protected Stack<ASTNode> ancestors = new Stack<>();

	public List<SourceCodeChange> getChanges() {
		return changes;
	}

	protected JavaASTNodeTypeConverter converter = new JavaASTNodeTypeConverter();

	public SubChangeCollector(int start, int end) {
		super(start, end);
	}

	protected boolean visit(ASTNode node) {
		if (isInRange(node)) {
			EntityType pType;
			if (ancestors.empty()) {
				pType = null;
			} else {
				pType = converter.convertNode(ancestors.peek());
			}
			SourceCodeEntity parent = pType == null ? null
					: new SourceCodeEntity(null, pType, null);
			SourceCodeChange change = newChange(node, parent);
			changes.add(change);
		}
		ancestors.push(node);
		return shouldVisitChildren(node);
	}

	protected abstract SourceCodeChange newChange(ASTNode node, SourceCodeEntity parentEntity);

}