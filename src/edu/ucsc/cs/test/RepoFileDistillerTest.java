package edu.ucsc.cs.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.ast.FileUtils;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import edu.ucsc.cs.analysis.RepoFileDistiller;
import edu.ucsc.cs.utils.DatabaseManager;

public class RepoFileDistillerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DatabaseManager.test = true;
	}

	@Test
	public void testFindPreviousCommitId() throws SQLException {
		
	}

	@Test
	public void testExtractDiff() throws IOException {
		String leftContent = FileUtils.getContent(new File("fixtures/TestLeft.java"));
		String rightContent = FileUtils.getContent(new File("fixtures/TestRight.java"));
		List<SourceCodeChange> changes = RepoFileDistiller.extractDiff(leftContent, rightContent);
		assertEquals(7, changes.size());
		
		changes = RepoFileDistiller.extractDiff(rightContent, leftContent);
		assertEquals(7, changes.size());
	}
}
