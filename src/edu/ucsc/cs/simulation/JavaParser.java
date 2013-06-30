package edu.ucsc.cs.simulation;

import java.io.File;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.ASTHelperFactory;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import edu.ucsc.cs.analysis.ASTHelperModule;
import edu.ucsc.cs.utils.FileUtils;

public class JavaParser {
	@Inject private ASTHelperFactory factory;
	
	public JavaParser() {
    	Injector injector = Guice.createInjector(new ASTHelperModule());
    	factory = injector.getInstance(ASTHelperFactory.class);		
	}
 	
	public JavaStructureNode parse(String source, String fileName) {
		File file = FileUtils.javaFileFromString(source, fileName);
		return this.parse(file);
	}
    
    @SuppressWarnings("unchecked")
	public JavaStructureNode parse(File file) {
    	ASTHelper<JavaStructureNode> astHelper = factory.create(file);
		return astHelper.createStructureTree();
	}
    
	public ASTHelper<JavaStructureNode> getASTHelper(String source, String fileName) {
    	File file = FileUtils.javaFileFromString(source, fileName);
    	return getASTHelper(file);
    }
    
    @SuppressWarnings("unchecked")
	public ASTHelper<JavaStructureNode> getASTHelper(File file) {
    	return factory.create(file);
    }
    
    public static void main(String[] args) {
    	JavaParser parser = new JavaParser();
    	ASTHelper<JavaStructureNode> astHelper = 
    			parser.getASTHelper(new File("src/edu/ucsc/cs/simulation/JavaParser.java"));
    	JavaStructureNode tree = astHelper.createStructureTree();
    	System.out.println(tree);
    }
}
