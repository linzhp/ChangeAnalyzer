package edu.ucsc.cs.simulation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilationUtils;
import edu.ucsc.cs.simulation.ClassModifier;
import edu.ucsc.cs.simulation.Indexer;

public class ClassModifierTest {
	private TypeDeclaration classNode;
	private ClassModifier modifier;
	private HashMap<String, ArrayList<ASTNode>> nodeIndex;

	@Before
	public void setUp() {
		String fileName = "fixtures/TextArea.java";
		CompilationUnitDeclaration tree = JavaCompilationUtils.compile(
				new File(fileName), ClassFileConstants.JDK1_7).getCompilationUnit();
		ClassFinder classFinder = new ClassFinder();
		tree.traverse(classFinder, tree.scope);
		classNode = classFinder.classNode;
		Indexer indexer = new Indexer();
		tree.traverse(indexer, tree.scope);
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
	
	private class ClassFinder extends ASTVisitor {
		TypeDeclaration classNode;
		@Override
		public boolean visit(TypeDeclaration typeDeclaration,
				CompilationUnitScope scope) {
			classNode = typeDeclaration;
			return false;
		}
	}
}
