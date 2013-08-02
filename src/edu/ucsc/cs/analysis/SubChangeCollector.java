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
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public abstract class SubChangeCollector extends ASTVisitor {
	private List<SourceCodeChange> changes = new LinkedList<SourceCodeChange>();
	public List<SourceCodeChange> getChanges() {
		return changes;
	}


	private int start, end;
	private JavaASTNodeTypeConverter converter = new JavaASTNodeTypeConverter();

	
	public SubChangeCollector(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	private boolean isSubNode(ASTNode node) {
		return node.sourceStart >= start && node.sourceEnd <= end;
	}
	

	@Override
	public boolean visit(TypeDeclaration td, ClassScope scope) {
		if (isSubNode(td)) {
			EntityType eType = converter.convertNode(td);
			SourceCodeChange change = newChange(new SourceCodeEntity(null, eType, null), 
					new SourceCodeEntity(null, JavaEntityType.TYPE_DECLARATION, null));
			change.setChangeType(ChangeType.ADDITIONAL_CLASS);
			changes.add(change);
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration td, BlockScope scope) {
		if (isSubNode(td)) {
			EntityType eType = converter.convertNode(td);
			// FIXME a class definition can be under any block, not only in method
			SourceCodeChange change = newChange(new SourceCodeEntity(null, eType, null), 
					new SourceCodeEntity(null, JavaEntityType.METHOD, null));
			change.setChangeType(ChangeType.ADDITIONAL_CLASS);
			changes.add(change);
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration td,
			CompilationUnitScope scope) {
		if (isSubNode(td)) {
			EntityType eType = converter.convertNode(td);
			SourceCodeChange change = newChange(new SourceCodeEntity(null, eType, null), 
					null);
			change.setChangeType(ChangeType.ADDITIONAL_CLASS);
			changes.add(change);
		}
		return true;
	}
	
	@Override
	public boolean visit(MethodDeclaration md, ClassScope scope) {
		if (isSubNode(md)) {
			EntityType eType = converter.convertNode(md);
			SourceCodeChange change = newChange(new SourceCodeEntity(null, eType, null), 
					new SourceCodeEntity(null, JavaEntityType.TYPE_DECLARATION, null));
			change.setChangeType(ChangeType.ADDITIONAL_FUNCTIONALITY);
			changes.add(change);
		}
		return true;
	}
	
	@Override
	public boolean visit(FieldDeclaration field, MethodScope scope) {
		if (isSubNode(field)) {
			EntityType eType = converter.convertNode(field);
			SourceCodeChange change = newChange(new SourceCodeEntity(null, eType, null), 
					new SourceCodeEntity(null, JavaEntityType.TYPE_DECLARATION, null));
			change.setChangeType(ChangeType.ADDITIONAL_OBJECT_STATE);
			changes.add(change);
		}
		return true;
	}
	
	
	protected abstract SourceCodeChange newChange(SourceCodeEntity thisEntity,
			SourceCodeEntity parentEntity);
}
