package edu.ucsc.cs.analysis;

import java.io.File;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.ASTHelperFactory;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import edu.ucsc.cs.utils.FileUtils;

public class JavaParser {
	@Inject private ASTHelperFactory factory;
	
	public JavaParser() {
    	Injector injector = Guice.createInjector(new ASTHelperModule());
    	factory = injector.getInstance(ASTHelperFactory.class);		
	}
 	
	public JavaStructureNode parse(String source, String fileName, String version) {
		File file = FileUtils.javaFileFromString(source, fileName);
		JavaStructureNode node = this.parse(file, version);
		file.delete();
		return node;
	}
    
	public JavaStructureNode parse(File file, String version) {
    	ASTHelper<JavaStructureNode> astHelper = getASTHelper(file, version);
		return astHelper.createStructureTree();
	}
    
	public ASTHelper<JavaStructureNode> getASTHelper(String source, String fileName, String version) {
    	File file = FileUtils.javaFileFromString(source, fileName);
    	ASTHelper<JavaStructureNode> astHelper = getASTHelper(file, version);
    	file.delete();
		return astHelper;
    }
    
    @SuppressWarnings("unchecked")
	public ASTHelper<JavaStructureNode> getASTHelper(File file, String version) {
    	return factory.create(file, version);
    }
    
    public static void main(String[] args) {
    	JavaParser parser = new JavaParser();
    	ASTHelper<JavaStructureNode> astHelper = 
    			parser.getASTHelper(new File("src/edu/ucsc/cs/analysis/JavaParser.java"), "1.7");
    	JavaStructureNode tree = astHelper.createStructureTree();
    	System.out.println(tree);
    }
}
