package edu.ucsc.cs.analysis;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public abstract class RangeVisitor extends ASTVisitor {

	protected int start;
	protected int end;

	public RangeVisitor(int start, int end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Optimization to speedup the traversal, shouldn't change the behavior
	 */
	protected boolean shouldVisitChildren(ASTNode node) {
		if (node instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) node;
			return type.declarationSourceEnd >= start
					&& type.declarationSourceStart <= end;
		} else if (node instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) node;
			return method.declarationSourceEnd >= start
					&& method.declarationSourceStart <= end;
		} else {
			// whether to go into the children of statements
			return false;
		}
	}

	/**
	 * Decide if the node is a descendant of the current changed node.
	 * @param node
	 * @return
	 */
	protected boolean isInRange(ASTNode node) {
		return node.sourceStart >= start && node.sourceEnd <= end;
	}


}