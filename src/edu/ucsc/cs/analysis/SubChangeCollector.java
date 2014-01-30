package edu.ucsc.cs.analysis;

import java.util.Stack;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

public abstract class SubChangeCollector extends ASTVisitor {
	private Stack<ASTNode> ancestors = new Stack<>();
	protected JavaASTNodeTypeConverter converter = new JavaASTNodeTypeConverter();
	protected FineChange fineChange;

	public SubChangeCollector(FineChange change) {
		this.fineChange = change;
	}

	protected boolean isSubNode(ASTNode node) {
		SourceCodeEntity changedEntity = fineChange.change.getChangedEntity();
		return node.sourceStart >= changedEntity.getStartPosition() && 
				node.sourceEnd <= changedEntity.getEndPosition();
	}

	/**
	 * don't go into any statement
	 */
	private boolean shouldVisitChildren(ASTNode node) {
		SourceCodeEntity changedEntity = fineChange.change.getChangedEntity();
		int start = changedEntity.getStartPosition(), 
				end = changedEntity.getEndPosition();
		if (node instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) node;
			return type.declarationSourceEnd >= start
					&& type.declarationSourceStart <= end;
		} else if (node instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) node;
			return method.declarationSourceEnd >= start
					&& method.declarationSourceStart <= end;
		} else {
			return false;
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
		return false;
	}

	// @Override
	// public void endVisit(TypeDeclaration td, BlockScope scope) {
	// ancestors.pop();
	// }

	@Override
	public boolean visit(TypeDeclaration td, CompilationUnitScope scope) {
		return visit(td);
	}

	@Override
	public void endVisit(TypeDeclaration td, CompilationUnitScope scope) {
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

	@Override
	public boolean visit(AssertStatement assertStatement, BlockScope scope) {
		visit(assertStatement);
		return false;
	}

	@Override
	public void endVisit(AssertStatement assertStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(Assignment assignment, BlockScope scope) {
		return visit(assignment);
	}

	@Override
	public void endVisit(Assignment assignment, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(BreakStatement breakStatement, BlockScope scope) {
		return visit(breakStatement);
	}

	@Override
	public void endVisit(BreakStatement breakStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(CaseStatement caseStatement, BlockScope scope) {
		return visit(caseStatement);
	}

	@Override
	public void endVisit(CaseStatement caseStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(Clinit clinit, ClassScope scope) {
		return visit(clinit);
	}

	@Override
	public void endVisit(Clinit clinit, ClassScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(CompoundAssignment compoundAssignment, BlockScope scope) {
		return visit(compoundAssignment);
	}

	@Override
	public void endVisit(CompoundAssignment compoundAssignment, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(ConstructorDeclaration constructorDeclaration,
			ClassScope scope) {
		return visit(constructorDeclaration);
	}

	@Override
	public void endVisit(ConstructorDeclaration constructorDeclaration,
			ClassScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(ContinueStatement continueStatement, BlockScope scope) {
		return visit(continueStatement);
	}

	@Override
	public void endVisit(ContinueStatement continueStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(DoStatement doStatement, BlockScope scope) {
		return visit(doStatement);
	}

	@Override
	public void endVisit(DoStatement doStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(EmptyStatement emptyStatement, BlockScope scope) {
		return visit(emptyStatement);
	}

	@Override
	public void endVisit(EmptyStatement emptyStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(ExplicitConstructorCall explicitConstructor,
			BlockScope scope) {
		return visit(explicitConstructor);
	}

	@Override
	public void endVisit(ExplicitConstructorCall explicitConstructor,
			BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(ForeachStatement forStatement, BlockScope scope) {
		return visit(forStatement);
	}

	@Override
	public void endVisit(ForeachStatement forStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(ForStatement forStatement, BlockScope scope) {
		return visit(forStatement);
	}

	@Override
	public void endVisit(ForStatement forStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(IfStatement ifStatement, BlockScope scope) {
		return visit(ifStatement);
	}

	@Override
	public void endVisit(IfStatement ifStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(Initializer initializer, MethodScope scope) {
		return visit(initializer);
	}

	@Override
	public void endVisit(Initializer initializer, MethodScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(Javadoc javadoc, BlockScope scope) {
		return visit(javadoc);
	}

	@Override
	public void endVisit(Javadoc javadoc, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(Javadoc javadoc, ClassScope scope) {
		return visit(javadoc);
	}

	@Override
	public void endVisit(Javadoc javadoc, ClassScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(LabeledStatement labeledStatement, BlockScope scope) {
		return visit(labeledStatement);
	}

	@Override
	public void endVisit(LabeledStatement labeledStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
		return visit(localDeclaration);
	}

	@Override
	public void endVisit(LocalDeclaration localDeclaration, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(MemberValuePair pair, BlockScope scope) {
		System.out.println("Here is a MemberValuePair: " + pair);
		return true;
	}

	@Override
	public boolean visit(MessageSend messageSend, BlockScope scope) {
		return visit(messageSend);
	}

	@Override
	public void endVisit(MessageSend messageSend, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(ReturnStatement returnStatement, BlockScope scope) {
		return visit(returnStatement);
	}

	@Override
	public void endVisit(ReturnStatement returnStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(SwitchStatement switchStatement, BlockScope scope) {
		return visit(switchStatement);
	}

	@Override
	public void endVisit(SwitchStatement switchStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(SynchronizedStatement synchronizedStatement,
			BlockScope scope) {
		return visit(synchronizedStatement);
	}

	@Override
	public void endVisit(SynchronizedStatement synchronizedStatement,
			BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(ThrowStatement throwStatement, BlockScope scope) {
		return visit(throwStatement);
	}

	@Override
	public void endVisit(ThrowStatement throwStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(TryStatement tryStatement, BlockScope scope) {
		return visit(tryStatement);
	}

	@Override
	public void endVisit(TryStatement tryStatement, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(WhileStatement whileStatement, BlockScope scope) {
		return visit(whileStatement);
	}

	@Override
	public void endVisit(WhileStatement whileStatement, BlockScope scope) {
		ancestors.pop();
	}

	private boolean visit(ASTNode node) {
		if (isSubNode(node)) {
			EntityType pType;
			if (ancestors.empty()) {
				pType = null;
			} else {
				pType = converter.convertNode(ancestors.peek());
			}
			SourceCodeEntity parent = pType == null ? null
					: new SourceCodeEntity(null, pType, null);
			SourceCodeChange change = newChange(node, parent);
			fineChange.addSubChange(change);
		}
		ancestors.push(node);
		return shouldVisitChildren(node);
	}

	protected abstract SourceCodeChange newChange(ASTNode node,
			SourceCodeEntity parentEntity);
}
