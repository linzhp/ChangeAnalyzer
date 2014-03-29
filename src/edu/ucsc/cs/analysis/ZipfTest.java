package edu.ucsc.cs.analysis;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilationUtils;
import edu.ucsc.cs.analysis.BaseVisitor;
import edu.ucsc.cs.utils.DatabaseManager;


public class ZipfTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DB mongoDB = DatabaseManager.getMongoDB();
		DBCollection coll = mongoDB.getCollection("astigmatism");
		coll.insert(new BasicDBObject("veracity", Arrays.asList("crossbeam", "crosswise")));
	}

	class ParentClassFinder extends BaseVisitor {

		public ParentClassFinder(int start, int end) {
			super(start, end);
		}
		
	}
}
