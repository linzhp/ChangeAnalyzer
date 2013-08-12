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

public class ClassModifier extends Modifier {
	private TypeDeclaration node;
	
	public ClassModifier(ASTNode node, Indexer indexer) {
		super(indexer);
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
		// index the new class
		indexer.index(memberClass);
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
		// index the new method
		indexer.index(method);
	}
	
	public void removeMethod() {
		if (node.methods != null && node.methods.length > 0) {
			int methodIndex = (int)(Math.random() * node.methods.length);
			indexer.nodeIndex.get("METHOD").remove(node.methods[methodIndex]);
			node.methods = ArrayUtils.remove(node.methods, methodIndex);
		}
		
	}
	
	public void removeClass() {
		if (node.memberTypes != null && node.memberTypes.length > 0) {
			int i = (int)(Math.random() * node.memberTypes.length);
			indexer.nodeIndex.get("CLASS").remove(node.memberTypes[i]);
			node.memberTypes = ArrayUtils.remove(node.memberTypes, i);
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
		// index the new field
		indexer.index(field);
	}
	
	
	public void removeField() {
		if (node.fields != null && node.fields.length > 0) {
			int i = (int)(Math.random() * node.fields.length);
			indexer.nodeIndex.get("CLASS").remove(node.fields[i]);
			node.fields = ArrayUtils.remove(node.fields, i);			
		}
	}
	/* (non-Javadoc)
	 * @see edu.ucsc.cs.simulation.Modifier#modify(com.mongodb.BasicDBObject)
	 */
	@Override
	public void modify(BasicDBObject object) {
		String changeType = object.getString("changeType");
		switch (changeType) {
		case "ADDITIONAL_CLASS":
			this.addClass();
			break;
		case "ADDITIONAL_FUNCTIONALITY":
			this.addMethod();
			break;
		case "ADDITIONAL_OBJECT_STATE":
			this.addField();
			break;
		case "REMOVED_FUNCTIONALITY":
			this.removeMethod();
			break;
		case "REMOVED_OBJECT_STATE":
			this.removeField();
		case "REMOVED_CLASS":
			this.removeClass();
			break;
		default:
			LogManager.getLogger().warning(changeType + " is not supported");
		}
	}
	
	
}
