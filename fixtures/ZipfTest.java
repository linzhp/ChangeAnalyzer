package edu.ucsc.cs.analysis;

import java.io.File;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilationUtils;
import edu.ucsc.cs.analysis.BaseVisitor;


public class ZipfTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CompilationUnitDeclaration tree = JavaCompilationUtils.compile(
				new File("src/edu/ucsc/cs/analysis/ZipfTest.java"), 
				ClassFileConstants.JDK1_7).getCompilationUnit();
		tree.traverse(new ASTVisitor() {
			@Override
			public boolean visit(TypeDeclaration td, CompilationUnitScope scope) {
				td.getClass();
				return true;
			}
		}, tree.scope);
	}

	class ParentClassFinder extends BaseVisitor {

		public ParentClassFinder(int start, int end) {
			super(start, end);
		}
		
	}
}
