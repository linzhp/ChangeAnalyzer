package edu.ucsc.cs.analysis;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.uzh.ifi.seal.changedistiller.model.entities.*;
import edu.ucsc.cs.utils.LogManager;

public class APIChangeExtractor extends ChangeProcessor {
	private Writer writer;

	public APIChangeExtractor(Writer writer) throws IOException {
		this.writer = writer;
		writer.write("ChangeType, SourceCodeEntity, SourceCodeChange, NewSourceCodeEntity\n");
	}

	@Override
	public void add(FineChange change) throws IOException {
		SourceCodeChange c = change.change;
		if (c.getChangeType().isDeclarationChange()) {
			writer.write(c.getLabel());
			writer.write(',');
			SourceCodeEntity changedEntity = c.getChangedEntity();
			writer.write(changedEntity.getLabel());
			// if (changedEntity.getType().isType()) {
			// writer.write('(');
			// writer.write(changedEntity.getUniqueName());
			// writer.write(')');
			// }
			writer.write(',');
			writer.write(c.getClass().getSimpleName());
			writer.write(',');
			if (c instanceof Update) {
				SourceCodeEntity newEntity = ((Update) c).getNewEntity();
				writer.write(newEntity.getLabel());
				// if (newEntity.getType().isType()) {
				// writer.write('(');
				// writer.write(newEntity.getUniqueName());
				// writer.write(')');
				// }
			}
			writer.write('\n');

		}
	}

	public static void main(String[] args) {
		try {
			FileWriter writer = new FileWriter(new File(args[1]));
			APIChangeExtractor reducer = new APIChangeExtractor(writer);
			Repository repository = new Repository(Integer.valueOf(args[0]),
					reducer);
			repository.extractChanges(null);
			writer.close();
		} catch (Exception e) {
			Logger logger = LogManager.getLogger();
			logger.log(Level.SEVERE, e.toString());
		}
	}
}
