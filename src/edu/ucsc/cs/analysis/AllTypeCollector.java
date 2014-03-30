package edu.ucsc.cs.analysis;

import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ContinueStatement;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

public abstract class AllTypeCollector extends ClassChangeCollector {
	public AllTypeCollector(int start, int end) {
		super(start, end);
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
		return visit(assertStatement);
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
}
