package edu.ucsc.cs.analysis;

import java.util.logging.Logger;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import ch.uzh.ifi.seal.changedistiller.ast.InvalidSyntaxException;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilationUtils;
import edu.ucsc.cs.utils.LogManager;

public class JavaParser {
	private static final String[] sourceLevels = { "1.7", "1.6", "1.5", "1.4",
		"1.3", "1.2", "1.1" };
	private static final long[] versionConstants = { ClassFileConstants.JDK1_7,
		ClassFileConstants.JDK1_6, ClassFileConstants.JDK1_5,
		ClassFileConstants.JDK1_4, ClassFileConstants.JDK1_3,
		ClassFileConstants.JDK1_2, ClassFileConstants.JDK1_1 };

	public static CompilationUnitDeclaration parse(FileRevision code) {
		CompilationUnitDeclaration tree = null;
		Logger logger = LogManager.getLogger();
		int i = 0;
		while (tree == null) {
			try {
				tree = JavaCompilationUtils.compile(code.content,
						code.toString(), versionConstants[i])
						.getCompilationUnit();
			} catch (InvalidSyntaxException e) {
				if (i < sourceLevels.length - 1) {
					logger.info("Failed to parse " + code
							+ " with source level " + sourceLevels[i++]
							+ ", trying with " + sourceLevels[i]);
				} else {
					logger.warning("Failed to parse " + code);
					return null;
				}
			}
		}
		return tree;
	}
}
