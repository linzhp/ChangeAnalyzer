package edu.ucsc.cs.analysis;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public abstract class SubChangeCollector extends ASTVisitor {
	protected List<SourceCodeChange> changes = new LinkedList<SourceCodeChange>();
	public List<SourceCodeChange> getChanges() {
		return changes;
	}


	private int start, end;
	protected JavaASTNodeTypeConverter converter = new JavaASTNodeTypeConverter();

	
	public SubChangeCollector(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	protected boolean isSubNode(ASTNode node) {
		return node.sourceStart >= start && node.sourceEnd <= end;
	}
	
	private boolean isOutOfRange(ASTNode node) {
		return node.sourceEnd < start || node.sourceStart > end;
	}
	

	@Override
	public boolean visit(TypeDeclaration td, ClassScope scope) {
		return visit(td, JavaEntityType.CLASS);
	}

	@Override
	public boolean visit(TypeDeclaration td, BlockScope scope) {
		// FIXME a class definition can be under any block, not only in method
		return visit(td, JavaEntityType.METHOD);
	}

	@Override
	public boolean visit(TypeDeclaration td,
			CompilationUnitScope scope) {
		return visit(td, JavaEntityType.NULL_LITERAL);
	}
	
	@Override
	public boolean visit(MethodDeclaration md, ClassScope scope) {
		return visit(md, JavaEntityType.CLASS);
	}
	
	@Override
	public boolean visit(FieldDeclaration field, MethodScope scope) {
		return visit(field, JavaEntityType.CLASS);
	}
	
	private boolean visit(ASTNode node, JavaEntityType parentType) {
		if (isSubNode(node)) {
			EntityType eType = converter.convertNode(node);
			SourceCodeEntity parent = parentType == JavaEntityType.NULL_LITERAL ? 
					null : new SourceCodeEntity(null, parentType, null);
			SourceCodeChange change = newChange(new SourceCodeEntity(null, eType, null), parent);
			changes.add(change);
		}
		return true;
	}
	
	protected abstract SourceCodeChange newChange(SourceCodeEntity thisEntity,
			SourceCodeEntity parentEntity);
}
