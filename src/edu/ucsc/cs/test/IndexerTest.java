package edu.ucsc.cs.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.ast.FileUtils;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilationUtils;
import edu.ucsc.cs.simulation.Indexer;

public class IndexerTest {

	@Test
	public void testIndex() {
		String fileName1 = "fixtures/TextAreaLeft.java";
		String oldContent = FileUtils.getContent(new File(fileName1));
		String fileName2 = "fixtures/TextAreaRight.java";
		String newContent = FileUtils.getContent(new File(fileName2));

		CompilationUnitDeclaration astNode1 = JavaCompilationUtils.compile(
				oldContent, fileName1, ClassFileConstants.JDK1_7).getCompilationUnit();
		Indexer indexer1 = new Indexer();
		astNode1.traverse(indexer1, astNode1.scope);

		CompilationUnitDeclaration astNode2 = JavaCompilationUtils.compile(
				newContent, fileName2, ClassFileConstants.JDK1_7).getCompilationUnit();
		Indexer indexer2 = new Indexer();
		astNode2.traverse(indexer2, astNode2.scope);

		assertEquals(indexer1.nodeIndex.get("METHOD").size(), indexer2.nodeIndex.get("METHOD").size());
		assertEquals(indexer1.nodeIndex.get("CLASS").size(), indexer2.nodeIndex.get("CLASS").size());
	}

}
