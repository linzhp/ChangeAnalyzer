package edu.ucsc.cs.analysis;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.ucsc.cs.analysis.*;
import edu.ucsc.cs.utils.DatabaseManager;

public class CommitGraphTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DatabaseManager.test = true;
	}
	
	@Test
	public void testFindPreviousCommitId() throws SQLException {
		CommitGraph commitGraph = new CommitGraph();
		commitGraph.addCommit(1, 1);
		commitGraph.addCommit(1, 4);
		assertEquals(4, commitGraph.findPreviousCommitId(1, 8));
		
		commitGraph.addCommit(2, 1);
		commitGraph.addCommit(2, 6);
		assertEquals(6, commitGraph.findPreviousCommitId(2, 8));
		
		commitGraph.addCommit(3, 1);
		assertEquals(1, commitGraph.findPreviousCommitId(3, 8));
		
		
		assertEquals(-1, commitGraph.findPreviousCommitId(4, 8));
	}

}
