package edu.ucsc.cs.analysis;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public abstract class SubChangeCollector extends RangeVisitor {

	protected List<SourceCodeChange> changes = new LinkedList<SourceCodeChange>();
	protected Deque<ASTNode> ancestors = new ArrayDeque<>();
	protected Deque<String> qualifiers = new ArrayDeque<>();
	protected JavaASTNodeTypeConverter converter = new JavaASTNodeTypeConverter();

	public SubChangeCollector(int start, int end) {
		super(start, end);
	}
	
	@Override
	public boolean visit(CompilationUnitDeclaration cu, CompilationUnitScope scope) {
        if (cu.currentPackage != null) {
            for (char[] qualifier : cu.currentPackage.tokens) {
            	qualifiers.push(new String(qualifier));
            }
        }
        ancestors.push(cu);
		return true;
	}

	@Override
	public void endVisit(CompilationUnitDeclaration cu, CompilationUnitScope scope) {
		ancestors.pop();
	}
	
	@Override
	public boolean visit(TypeDeclaration td, ClassScope scope) {
		qualifiers.add(new String(td.name));
		return visit(td);
	}

	@Override
	public void endVisit(TypeDeclaration td, ClassScope scope) {
		ancestors.pop();
		qualifiers.pop();
	}

	@Override
	public boolean visit(TypeDeclaration td, BlockScope scope) {
		qualifiers.add(new String(td.name));
		return visit(td);
	}

	@Override
	public void endVisit(TypeDeclaration td, BlockScope scope) {
		ancestors.pop();
		qualifiers.pop();
	}

	@Override
	public boolean visit(TypeDeclaration td, CompilationUnitScope scope) {
		qualifiers.add(new String(td.name));
		return visit(td);
	}

	@Override
	public void endVisit(TypeDeclaration td, CompilationUnitScope scope) {
		ancestors.pop();
		qualifiers.pop();
	}

	public List<SourceCodeChange> getChanges() {
		return changes;
	}

	protected boolean visit(ASTNode node) {
		if (isInRange(node)) {
			EntityType pType;
			if (ancestors.isEmpty()) {
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
