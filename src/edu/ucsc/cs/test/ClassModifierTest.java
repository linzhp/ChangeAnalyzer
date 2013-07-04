package edu.ucsc.cs.test;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.junit.*;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;
import edu.ucsc.cs.simulation.ClassModifier;
import edu.ucsc.cs.simulation.JavaParser;

public class ClassModifierTest {
	private TypeDeclaration classNode;
	private ClassModifier modifier;

	@Before
	public void setUp() {
		JavaParser parser = new JavaParser();
		ASTHelper<JavaStructureNode> astHelper = parser.getASTHelper(new File("src/edu/ucsc/cs/test/ClassModifierTest.java"));
		JavaStructureNode tree = astHelper.createStructureTree();
		classNode = (TypeDeclaration)tree.getChildren().get(0).getASTNode();
		modifier = new ClassModifier(classNode);		
	}

	@Test
	public void testAddClass() {
		int oldCount = classNode.memberTypes == null ? 0: classNode.memberTypes.length;
		modifier.addClass();
		assertEquals(oldCount + 1, classNode.memberTypes.length);
	}

	@Test
	public void testAddMethod() {
		int oldCount = classNode.methods == null ? 0:classNode.methods.length;
		modifier.addMethod();
		assertEquals(oldCount + 1, classNode.methods.length);
		System.out.println(classNode.toString());
	}
}
