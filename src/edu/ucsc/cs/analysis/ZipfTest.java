package edu.ucsc.cs.analysis;

import java.io.File;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

import ch.uzh.ifi.seal.changedistiller.ast.FileUtils;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilationUtils;


public class ZipfTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String content = FileUtils.getContent(new File("SourceMapper.java"));
		FileRevision fileRevision = new FileRevision(6569, 4822, content);
		JavaParser.parse(fileRevision);
	}
}
