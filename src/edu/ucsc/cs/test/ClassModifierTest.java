package edu.ucsc.cs.test;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;
import edu.ucsc.cs.simulation.ClassModifier;
import edu.ucsc.cs.simulation.JavaParser;

public class ClassModifierTest {

	@Test
	public void testAddClass() {
		JavaParser parser = new JavaParser();
		ASTHelper<JavaStructureNode> astHelper = parser.getASTHelper(new File("src/edu/ucsc/cs/test/ClassModifierTest.java"));
		JavaStructureNode tree = astHelper.createStructureTree();
		TypeDeclaration classNode = (TypeDeclaration)tree.getChildren().get(0).getASTNode();
		ClassModifier modifier = new ClassModifier(classNode);
		classNode.toString();
		int oldCount = classNode.memberTypes == null ? 0: classNode.memberTypes.length;
		modifier.addClass();
		assertEquals(oldCount + 1, classNode.memberTypes.length);
		classNode.toString();
	}

}
