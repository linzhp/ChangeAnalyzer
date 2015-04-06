package edu.ucsc.cs.analysis;

import java.io.File;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class FileChangeDistiller {

	public static void main(String[] args) {
		File left = new File(args[0]);
		File right = new File(args[1]);
		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		distiller.extractClassifiedSourceCodeChanges(left, right);
		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		System.out.print("Number of changes: " + changes.size());
	}

}
