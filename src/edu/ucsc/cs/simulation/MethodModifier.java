package edu.ucsc.cs.simulation;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;

import com.mongodb.BasicDBObject;

import edu.ucsc.cs.utils.LogManager;

public class MethodModifier extends Modifier{
	private MethodDeclaration node;
	
	public MethodModifier(ASTNode node, Indexer indexer) {
		super(indexer);
		this.node = (MethodDeclaration)node;
	}
	
	public void rename() {
		String name = new String(node.selector);
		String renamePrefix = "methodRename";
		if (name.startsWith(renamePrefix)) {
			// the method has previously been renamed
			Integer n = Integer.valueOf(name.substring(renamePrefix.length()));
			n++;
			node.selector = (renamePrefix + n).toCharArray();
		} else {
			node.selector = (renamePrefix + 1).toCharArray();
		}
			
	}

	@Override
	public void modify(BasicDBObject object) {
		String changeType = object.getString("changeType");
		switch (changeType) {
		case "STATEMENT_INSERT":
			break;
		case "STATEMENT_ORDERING_CHANGE":
			break;
		case "STATEMENT_PARENT_CHANGE":
			break;
		case "STATEMENT_DELETE":
			break;
		case "METHOD_RENAMING":
			this.rename();
			break;
		default:
			LogManager.getLogger().warning(changeType + " is not supported");
		}
		
	}

}
