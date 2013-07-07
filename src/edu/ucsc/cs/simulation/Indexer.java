package edu.ucsc.cs.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import edu.ucsc.cs.utils.LogManager;

public class Indexer extends ASTVisitor {
	public HashMap<String, ArrayList<ASTNode>> nodeIndex = new HashMap<String, ArrayList<ASTNode>>();
	private JavaASTNodeTypeConverter converter = new JavaASTNodeTypeConverter();

	
	public void index(ASTNode node) {
		EntityType type = converter.convertNode(node);
		if (type != null) {
			String typeString = type.toString();
			if (nodeIndex.containsKey(typeString)) {
				nodeIndex.get(typeString).add(node);
			} else {
				nodeIndex.put(typeString, new ArrayList<ASTNode>(Arrays.asList(node)));
			}
		} else {
			LogManager.getLogger().info("Invalid ASTNode class: " + node.getClass());
		}
	}

	@Override
	public boolean visit(TypeDeclaration td, ClassScope scope) {
		index(td);
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		index(localTypeDeclaration);
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration,
			CompilationUnitScope scope) {
		index(typeDeclaration);
		return true;
	}
	
	@Override
	public boolean visit(MethodDeclaration method, ClassScope scope) {
		index(method);
		return true;
	}
	
	@Override
	public boolean visit(FieldDeclaration field, MethodScope scope) {
		index(field);
		return true;
	}
}
