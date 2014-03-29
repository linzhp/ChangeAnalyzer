package edu.ucsc.cs.analysis;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

public class ParentClassFinder extends BaseVisitor {
	public ParentClassFinder(int start, int end) {
		super(start, end);
	}

	LinkedList<String> parentNames = new LinkedList<>();

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
				parentNames.add(td.superclass.toString());
			}
			if (td.superInterfaces != null) {
				for (TypeReference itf : td.superInterfaces) {
					parentNames.add(itf.toString());
				}
			}
			if (td.allocation != null) {
				if (td.allocation.type != null) {
					// anonymous class
					parentNames.add(td.allocation.type.toString());					
				} else {
					parentNames.add(new String(td.allocation.enumConstant.name));
				}
			}
			return false; // done!
		} else {
			return shouldVisitChildren(td);
		}
	}
	
	public Collection<String> getParentNames() {
		return parentNames;
	}
}
