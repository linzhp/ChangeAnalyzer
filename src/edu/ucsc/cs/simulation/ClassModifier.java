package edu.ucsc.cs.simulation;

import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

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
		node.modifiers |= ClassFileConstants.AccPrivate; 
		// give it a name
		String name = "Innerclass" + DigestUtils.md5Hex(String.valueOf(node.hashCode())).substring(0, 7);
		memberClass.name = name.toCharArray();
		// add it to the AST
		if (node.memberTypes != null) {
			TypeDeclaration[] memberTypes = Arrays.copyOf(node.memberTypes, node.memberTypes.length + 1);
			memberTypes[node.memberTypes.length] = memberClass;
			node.memberTypes = memberTypes;
		} else {
			node.memberTypes = new TypeDeclaration[1];
			node.memberTypes[0] = memberClass;
		}		
	}
	
	public void modify(BasicDBObject object) {
		String changeType = object.getString("changeType");
		switch (changeType) {
		case "ADDITIONAL_CLASS":
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
