package edu.ucsc.cs.analysis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import ch.uzh.ifi.seal.changedistiller.ast.FileUtils;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import edu.ucsc.cs.analysis.FileRevision;
import edu.ucsc.cs.analysis.RepoFileDistiller;
import edu.ucsc.cs.utils.DatabaseManager;

public class RepoFileDistillerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DatabaseManager.test = true;
	}

	@Test
	public void testExtractDiff1() throws IOException {
		String oldContent = FileUtils.getContent(new File("fixtures/TextAreaLeft.java"));
		String newContent = FileUtils.getContent(new File("fixtures/TextAreaRight.java"));
		List<SourceCodeChange> changes = RepoFileDistiller.extractDiff(
				new FileRevision(3559, 2996, oldContent), 
				new FileRevision(3565, 2996, newContent));
		Collection<String> changeTypes = Collections2.transform(changes, new Function<SourceCodeChange, String>() {
			@Override
			public String apply(SourceCodeChange c) {
				return c.getLabel();
			}
		});
		assertThat(changeTypes, hasItem(equalTo("ADDITIONAL_FUNCTIONALITY")));
	}
}
