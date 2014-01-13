package codeGenerator;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CodeFactory {
	
	/**
	 * Create a method according to 
	 * http://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.4
	 * 
	 * @param cr
	 * @return
	 */
	public MethodDeclaration createMethod(TypeDeclaration parent) {
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		// give it a name
		String name = "newMethod" + DigestUtils.md5Hex(String.valueOf(method.hashCode())).substring(0, 7);
		method.selector = name.toCharArray();
		method.modifiers = createMethodModifiers(parent);
		method.returnType = createTypeRef(parent.scope, true);
		method.arguments = createArguments(parent.scope);
		return method;
	}
	
	/**
	 * Supports public, protected, private, abstract, static, final
	 * @return
	 */
	public int createMethodModifiers(TypeDeclaration parent) {
		int modifiers = 0;
		double ran = Math.random();
		// randomly decide access modifiers
		if (ran < 1/3) {
			modifiers |= ClassFileConstants.AccPrivate;
		} else if (ran < 2/3) {
			modifiers |= ClassFileConstants.AccProtected;
		} else {
			modifiers |= ClassFileConstants.AccPublic;
		}
		
		if ((modifiers & ClassFileConstants.AccPrivate) == 0 &&
				(parent.modifiers & ClassFileConstants.AccAbstract) != 0) {
			// randomly decide abstract
			if (ran < 0.5) {
				modifiers |= ClassFileConstants.AccAbstract;
			}
		}
		
		if (ran < 0.5) {
			modifiers |= ClassFileConstants.AccStatic;
		}
		
		if (ran < 0.5) {
			modifiers |= ClassFileConstants.AccFinal;
		}
		return modifiers;
	}
	

	public TypeReference createTypeRef(Scope scope, boolean allowVoid) {
		TypeBinding[] supportedTypes = {
				TypeBinding.VOID,
				TypeBinding.BOOLEAN,
				TypeBinding.CHAR,
				TypeBinding.FLOAT,
				TypeBinding.DOUBLE,
				TypeBinding.BYTE,
				TypeBinding.SHORT,
				TypeBinding.INT,
				TypeBinding.LONG,
				scope.getJavaLangString(),
				scope.getJavaLangObject()
		};
		double ran = Math.random();
		int index;
		if (allowVoid) {
			index = (int)(ran * supportedTypes.length);
		} else {
			index = (int)(ran * (supportedTypes.length - 1) + 1); 
		}
		TypeBinding selectedType = supportedTypes[index];
		if (ran < 0.5) {
			return new SingleTypeReference(selectedType.sourceName(), 0);
		} else {
			return new ArrayTypeReference(selectedType.sourceName(), 1, 0);
		}
	}
	
	public Argument[] createArguments(Scope scope) {
		int length = (int)(Math.random() * 5);
		Argument[] arguments = new Argument[length];
		for (int i = 0; i < length; i++) {
			arguments[i] = new Argument(("arg" + i).toCharArray(), 0L, createTypeRef(scope, false), 0);
		}
		return arguments;
	}
}
