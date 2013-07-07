package edu.ucsc.cs.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.junit.*;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;
import edu.ucsc.cs.simulation.ClassModifier;
import edu.ucsc.cs.simulation.Indexer;
import edu.ucsc.cs.simulation.JavaParser;

public class ClassModifierTest {
	private TypeDeclaration classNode;
	private ClassModifier modifier;
	private HashMap<String, ArrayList<ASTNode>> nodeIndex;

	@Before
	public void setUp() {
		JavaParser parser = new JavaParser();
		ASTHelper<JavaStructureNode> astHelper = parser.getASTHelper(new File("fixtures/TextArea.java"));
		JavaStructureNode tree = astHelper.createStructureTree();
		classNode = (TypeDeclaration)tree.getChildren().get(0).getASTNode();
		Indexer indexer = new Indexer();
		CompilationUnitDeclaration astNode = (CompilationUnitDeclaration)tree.getASTNode();
		astNode.traverse(indexer, astNode.scope);
		nodeIndex = indexer.nodeIndex;
		modifier = new ClassModifier(classNode, indexer);		
	}

	@Test
	public void testAddClass() {
		int oldCount1 = classNode.memberTypes == null ? 0: classNode.memberTypes.length;
		int oldCount2 = nodeIndex.containsKey("CLASS") ? nodeIndex.get("CLASS").size():0;
		modifier.addClass();
		assertEquals(oldCount1 + 1, classNode.memberTypes.length);
		assertEquals(oldCount2 + 1, nodeIndex.get("CLASS").size());
	}

	@Test
	public void testAddMethod() {
		int oldCount1 = classNode.methods == null ? 0:classNode.methods.length;
		int oldCount2 = nodeIndex.containsKey("METHOD") ? nodeIndex.get("METHOD").size():0;
		modifier.addMethod();
		assertEquals(oldCount1 + 1, classNode.methods.length);
		assertEquals(oldCount2 + 1, nodeIndex.get("METHOD").size());
	}
	
	@Test
	public void testRemoveClass() {
		int oldCount1 = classNode.memberTypes == null ? 0:classNode.memberTypes.length;
		int oldCount2 = nodeIndex.containsKey("CLASS") ? nodeIndex.get("CLASS").size():0;
		modifier.removeClass();
		assertEquals(oldCount1 - 1, classNode.memberTypes.length);
		assertEquals(oldCount2 - 1, nodeIndex.get("CLASS").size());
	}
}
