package edu.ucsc.cs.test;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;
import edu.ucsc.cs.simulation.Indexer;
import edu.ucsc.cs.simulation.JavaParser;
import edu.ucsc.cs.simulation.MethodModifier;

public class MethodModifierTest {

	private AbstractMethodDeclaration methodNode;
	private MethodModifier methodModifier;

	@Before
	public void setUp() throws Exception {
		JavaParser parser = new JavaParser();
		ASTHelper<JavaStructureNode> astHelper = parser.getASTHelper(new File("fixtures/TextArea.java"));
		JavaStructureNode tree = astHelper.createStructureTree();
		methodNode = ((TypeDeclaration)tree.getChildren().get(0).getASTNode()).methods[2];
		methodModifier = new MethodModifier(methodNode, new Indexer());
	}

	@Test
	public void testRename() {
		methodModifier.rename();
		assertEquals("methodRename1", new String(methodNode.selector));
	}

}
