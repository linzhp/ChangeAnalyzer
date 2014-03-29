package edu.ucsc.cs.analysis;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.util.Collection;

import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.ast.FileUtils;

public class ChangeExtractorTest {

	@Test
	public void testGetParentFromNewClass() {
		Collection<String> parentClassNames = ChangeExtractor.getParentClassNames(0, Integer.MAX_VALUE, 
				new FileRevision(0, 0, FileUtils.getContent(new File("fixtures/TextArea.java"))));
		assertThat(parentClassNames.toArray(new String[parentClassNames.size()]), 
				arrayContaining("org.gjt.sp.jedit.textarea.JComponent"));
	}
	
	@Test
	public void testAddAnonymousClass() {
		Collection<String> parentClassNames = ChangeExtractor.getParentClassNames(766, 943, 
				new FileRevision(0, 0, FileUtils.getContent(new File("fixtures/ZipfTest.java"))));
		assertThat(parentClassNames.toArray(new String[parentClassNames.size()]),
				arrayContaining("ASTVisitor"));
	}
	
	@Test
	public void testAddInnerClass() {
		Collection<String> parentClassNames = ChangeExtractor.getParentClassNames(950, 1076, 
				new FileRevision(0, 0, FileUtils.getContent(new File("fixtures/ZipfTest.java"))));
		assertThat(parentClassNames.toArray(new String[parentClassNames.size()]), 
				arrayContaining("BaseVisitor"));		
	}
}
