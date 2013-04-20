package edu.ucsc.cs;

import java.io.*;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.*;

public class APIChangeExtractor extends ChangeReducer {
	private Writer writer;
	
	public APIChangeExtractor(Writer writer) throws IOException {
		this.writer = writer;
		writer.write("ChangeType, SourceCodeEntity, SourceCodeChange, NewSourceCodeEntity\n");
	}

	@Override
	public void add(List<SourceCodeChange> changes) throws IOException {
		
		for(SourceCodeChange c : changes) {
			if (c.getChangeType().isDeclarationChange()) {
				writer.write(c.getLabel());
				writer.write(',');
				writer.write(c.getChangedEntity().getLabel());
				writer.write(',');
				writer.write(c.getClass().getSimpleName());
				writer.write(',');
				if (c instanceof Update) {
					writer.write(((Update)c).getNewEntity().getLabel());
				}
				writer.write('\n');
				
			}
		}
	}

}
