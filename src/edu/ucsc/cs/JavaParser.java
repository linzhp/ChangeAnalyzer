package edu.ucsc.cs;

import java.io.File;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.ASTHelperFactory;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureNode;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class JavaParser {
	@Inject private ASTHelperFactory factory;
	private ASTHelper<StructureNode> astHelper;
	
	public JavaParser() {
    	Injector injector = Guice.createInjector(new ASTHelperModule());
    	factory = injector.getInstance(ASTHelperFactory.class);		
	}
 	
	public StructureNode parse(String source, String fileName) {
		File file = FileUtils.javaFileFromString(source, fileName);
		return this.parse(file);
	}
    
    @SuppressWarnings("unchecked")
	public StructureNode parse(File file) {
		astHelper = factory.create(file);
		return astHelper.createStructureTree();
	}
    
    public static void main(String[] args) {
    	JavaParser parser = new JavaParser();
    	StructureNode tree = parser.parse(new File("src/edu/ucsc/cs/JavaParser.java"));
    	System.out.println(tree);
    }
}
