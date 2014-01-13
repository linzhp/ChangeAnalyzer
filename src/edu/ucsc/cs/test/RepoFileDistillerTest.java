package edu.ucsc.cs.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

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
		List<SourceCodeChange> changes = RepoFileDistiller.extractDiff(
				new File("fixtures/TestLeft.java"), "default", new File("fixtures/TestRight.java"), "default");
		assertEquals(7, changes.size());		
	}
}
