package edu.ucsc.cs.analysis;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

public abstract class ClassChangeCollector extends SubChangeCollector {

	public ClassChangeCollector(int start, int end) {
		super(start, end);
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
		return visit(td);
	}

	@Override
	public void endVisit(TypeDeclaration td, BlockScope scope) {
		ancestors.pop();
	}

	@Override
	public boolean visit(TypeDeclaration td, CompilationUnitScope scope) {
		return visit(td);
	}

	@Override
	public void endVisit(TypeDeclaration td, CompilationUnitScope scope) {
		ancestors.pop();
	}

	
}
