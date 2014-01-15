package edu.ucsc.cs.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilationUtils;
import edu.ucsc.cs.simulation.Indexer;
import edu.ucsc.cs.simulation.MethodModifier;

public class MethodModifierTest {

	private AbstractMethodDeclaration methodNode;
	private MethodModifier methodModifier;

	@Before
	public void setUp() throws Exception {
		String fileName = "fixtures/TextArea.java";
		CompilationUnitDeclaration tree = JavaCompilationUtils.compile(
				new File(fileName), 
				ClassFileConstants.JDK1_7).getCompilationUnit();
		MethodFinder mFinder = new MethodFinder();
		tree.traverse(mFinder, tree.scope);
		methodNode = mFinder.methodNode;
		methodModifier = new MethodModifier(methodNode, new Indexer());
	}

	@Test
	public void testRename() {
		methodModifier.rename();
		assertEquals("methodRename1", new String(methodNode.selector));
	}

	private class MethodFinder extends ASTVisitor {
		MethodDeclaration methodNode;
		@Override
		public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
			methodNode = methodDeclaration;
			return false;
		}
	}
}
