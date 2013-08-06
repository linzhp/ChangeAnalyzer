package edu.ucsc.cs.analysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public abstract class SubChangeCollector extends ASTVisitor {
	protected List<SourceCodeChange> changes = new LinkedList<SourceCodeChange>();
	public List<SourceCodeChange> getChanges() {
		return changes;
	}

	private Stack<ASTNode> ancestors = new Stack<>();
	private int start, end;
	protected JavaASTNodeTypeConverter converter = new JavaASTNodeTypeConverter();

	
	public SubChangeCollector(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	protected boolean isSubNode(ASTNode node) {
		return node.sourceStart >= start && node.sourceEnd <= end;
	}
	
	private boolean shouldVisitChildren(ASTNode node) {
		if (node instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration)node;
			return type.declarationSourceEnd >= start && type.declarationSourceStart <= end;
		} else if (node instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration)node;
			return method.declarationSourceEnd >= start && method.declarationSourceStart <= end;
		} else {
			return true;			
		}
	}

	@Override
	public boolean visit(TypeDeclaration td, ClassScope scope) {
		return visit(td);
	}
	
	@Override
	public void endVisit(TypeDeclaration td, ClassScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(TypeDeclaration td, BlockScope scope) {
		// FIXME a class definition can be under any block, not only in method
		return visit(td);
	}
	
	@Override
	public void endVisit(TypeDeclaration td, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(TypeDeclaration td,
			CompilationUnitScope scope) {
		return visit(td);
	}
	
	@Override
	public void endVisit(TypeDeclaration td,
			CompilationUnitScope scope) {
		ancestors.pop();
	}
	
	@Override
	public boolean visit(MethodDeclaration md, ClassScope scope) {
		return visit(md);
	}
	
	@Override
	public void endVisit(MethodDeclaration md, ClassScope scope) {
		ancestors.pop();
	}
	
	@Override
	public boolean visit(FieldDeclaration field, MethodScope scope) {
		return visit(field);
	}
	
	@Override
	public void endVisit(FieldDeclaration field, MethodScope scope) {
		ancestors.pop();
	}
	
	private boolean visit(ASTNode node) {
		if (isSubNode(node)) {
			EntityType eType = converter.convertNode(node);
			EntityType pType;
			if (ancestors.empty()) {
				pType = null;
			} else {
				pType = converter.convertNode(ancestors.peek());
			}
			SourceCodeEntity parent = pType == null ? 
					null : new SourceCodeEntity(null, pType, null);
			SourceCodeChange change = newChange(new SourceCodeEntity(null, eType, null), 
					parent);
			changes.add(change);
		}
		ancestors.push(node);
		return shouldVisitChildren(node);
	}
	
	protected abstract SourceCodeChange newChange(SourceCodeEntity thisEntity,
			SourceCodeEntity parentEntity);
}
