package edu.ucsc.cs;

import java.io.*;
import java.util.ArrayList;
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
				SourceCodeEntity changedEntity = c.getChangedEntity();
				writer.write(changedEntity.getLabel());
//				if (changedEntity.getType().isType()) {
//					writer.write('(');
//					writer.write(changedEntity.getUniqueName());
//					writer.write(')');					
//				}
				writer.write(',');
				writer.write(c.getClass().getSimpleName());
				writer.write(',');
				if (c instanceof Update) {
					SourceCodeEntity newEntity = ((Update)c).getNewEntity();
					writer.write(newEntity.getLabel());
//					if (newEntity.getType().isType()) {
//						writer.write('(');
//						writer.write(newEntity.getUniqueName());
//						writer.write(')');					
//					}
				}
				writer.write('\n');
				
			}
		}
	}

	public static void main(String[] args) throws Exception {
		FileWriter writer = new FileWriter(new File(args[1]));
		APIChangeExtractor reducer = new APIChangeExtractor(writer);
		new Repository(Integer.valueOf(args[0]), new ArrayList<Integer>(), reducer);
		writer.close();
	}
}
