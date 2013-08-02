package edu.ucsc.cs.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

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
		RepoFileDistiller.previousCommits.put(1,
				new TreeSet<Integer>(Arrays.asList(1, 4)));
		RepoFileDistiller distiller = new RepoFileDistiller(null);
		assertEquals(4, distiller.findPreviousCommitId(1, 8));

		RepoFileDistiller.previousCommits.put(2,
				new TreeSet<Integer>(Arrays.asList(1, 6)));
		assertEquals(6, distiller.findPreviousCommitId(2, 8));

		RepoFileDistiller.previousCommits.put(3,
				new TreeSet<Integer>(Arrays.asList(1)));
		assertEquals(1, distiller.findPreviousCommitId(3, 8));
		
		RepoFileDistiller.previousCommits.put(4,
				new TreeSet<Integer>());
		assertEquals(-1, distiller.findPreviousCommitId(4, 8));
		
	}

	@Test
	public void testExtractDiff() throws IOException {
		String leftContent = FileUtils.getContent(new File("fixtures/TestLeft.java"));
		String rightContent = FileUtils.getContent(new File("fixtures/TestRight.java"));
		List<SourceCodeChange> changes = RepoFileDistiller.extractDiff(leftContent, rightContent);
		assertEquals(2, changes.size());
		
		changes = RepoFileDistiller.extractDiff(rightContent, leftContent);
		assertEquals(2, changes.size());
	}
}
