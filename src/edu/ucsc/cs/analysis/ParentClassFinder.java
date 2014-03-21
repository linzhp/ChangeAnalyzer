package edu.ucsc.cs.analysis;

import java.util.ArrayList;

import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class ParentClassFinder extends BaseVisitor {
	public ParentClassFinder(int start, int end) {
		super(start, end);
	}

	ArrayList<TypeReference> parents = new ArrayList<>();

	@Override
	public boolean visit(TypeDeclaration td, ClassScope scope) {
		return visit(td);
	}

	@Override
	public boolean visit(TypeDeclaration td, BlockScope scope) {
		return visit(td);
	}

	@Override
	public boolean visit(TypeDeclaration td, CompilationUnitScope scope) {
		return visit(td);
	}
	
	@Override
	public boolean visit(MethodDeclaration md, ClassScope scope) {
		return shouldVisitChildren(md);
	}

	protected boolean visit(TypeDeclaration td) {
		if (isInRange(td)) {
			if (td.superclass != null) {
				parents.add(td.superclass);
			}
			if (td.superInterfaces != null) {
				for (TypeReference itf : td.superInterfaces) {
					parents.add(itf);
				}
			}
			if (td.allocation != null) {
				// anonymous class
				parents.add(td.allocation.type);
			}
			return false; // done!
		} else {
			return shouldVisitChildren(td);
		}
	}
	
	public String[] getParentNames() {
		return Collections2.transform(parents, new Function<TypeReference, String>() {
			@Override
			public String apply(TypeReference parent) {
				return parent.toString();
			}
			
		}).toArray(new String[parents.size()]);
	}
}
