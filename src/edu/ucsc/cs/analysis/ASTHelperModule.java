package edu.ucsc.cs.analysis;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.ASTHelperFactory;
import ch.uzh.ifi.seal.changedistiller.ast.ASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaSourceCodeChangeClassifier;
import ch.uzh.ifi.seal.changedistiller.distilling.SourceCodeChangeClassifier;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ASTHelperModule extends AbstractModule {

	@Override
	protected void configure() {
        bind(ASTNodeTypeConverter.class).to(JavaASTNodeTypeConverter.class);
        bind(SourceCodeChangeClassifier.class).to(JavaSourceCodeChangeClassifier.class);
        install(new FactoryModuleBuilder().implement(ASTHelper.class, JavaASTHelper.class)
                .build(ASTHelperFactory.class));

	}

}
