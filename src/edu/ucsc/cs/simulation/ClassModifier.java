package edu.ucsc.cs.simulation;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

import com.mongodb.BasicDBObject;

import edu.ucsc.cs.utils.LogManager;

public class ClassModifier {
	private TypeDeclaration node;
	
	public ClassModifier(ASTNode node) {
		this.node = (TypeDeclaration)node;
	}
	
	public void rename(String newName) {
		node.name = newName.toCharArray();
	}
	
	public void addClass() {
		TypeDeclaration memberClass = new TypeDeclaration(node.compilationResult);
		// mark it as a member type
		memberClass.bits |= ASTNode.Bit11; 
		// make the inner class private, as it's common
		memberClass.modifiers |= ClassFileConstants.AccPrivate; 
		// give it a name
		String name = "NewClass" + DigestUtils.md5Hex(String.valueOf(memberClass.hashCode())).substring(0, 7);
		memberClass.name = name.toCharArray();
		// add it to the AST
		node.memberTypes = ArrayUtils.add(node.memberTypes, memberClass);
	}
	
	public void addMethod() {
		MethodDeclaration method = new MethodDeclaration(node.compilationResult);
		// make it private, for no reason
		method.modifiers |= ClassFileConstants.AccPrivate;
		// give it a name
		String name = "newMethod" + DigestUtils.md5Hex(String.valueOf(method.hashCode())).substring(0, 7);
		method.selector = name.toCharArray();
		// making return type void
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		// add it to the AST
		node.methods = ArrayUtils.add(node.methods, method);
	}
	
	public void removeClass() {
		if (node.memberTypes != null) {
			node.memberTypes = ArrayUtils.remove(node.memberTypes, (int)(Math.random() * node.memberTypes.length));
		}
	}
	
	public void addField() {
		FieldDeclaration field = new FieldDeclaration();
		field.modifiers |= ClassFileConstants.AccPrivate;
		// give it a name
		String name = "newMethod" + DigestUtils.md5Hex(String.valueOf(field.hashCode())).substring(0, 7);
		field.name = name.toCharArray();
		// making it a String
		field.type = TypeReference.baseTypeReference(TypeIds.T_JavaLangString, 0);
		// add it to the AST
		node.fields = ArrayUtils.add(node.fields, field);
	}
	
	public void modify(BasicDBObject object) {
		String changeType = object.getString("changeType");
		switch (changeType) {
		case "ADDITIONAL_CLASS":
			this.addClass();
			break;
		case "ADDITIONAL_FUNCTIONALITY":
			break;
		case "ADDITIONAL_OBJECT_STATE":
			break;
		case "REMOVED_CLASS":
			break;
		default:
			LogManager.getLogger().warning(changeType + " is not supported");
		}
	}
	
	
}
