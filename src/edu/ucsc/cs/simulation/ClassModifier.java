package edu.ucsc.cs.simulation;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public class ClassModifier {
	private TypeDeclaration node;
	
	public ClassModifier(ASTNode node) {
		this.node = (TypeDeclaration)node;
	}
	
	public void rename(String newName) {
		node.name = newName.toCharArray();
	}
}
