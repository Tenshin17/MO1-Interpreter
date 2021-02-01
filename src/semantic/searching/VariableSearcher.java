package semantic.searching;

import error.ParserHandler;
import Execution.FunctionTracker;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import semantic.symboltable.scope.LocalScopeCreator;

/**
 * A utility class to search for a certain variable depending on where the control flow is.
 *
 */
public class VariableSearcher {
	
	public static JavaValue searchVariable(String identifierString) {
		JavaValue javaValue = null;
		
		if(FunctionTracker.getInstance().isInsideFunction()) {
			javaValue = searchVariableInFunction(FunctionTracker.getInstance().getLatestFunction(), identifierString);
		}
		
		if(javaValue == null) {
			ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
			javaValue = searchVariableInClassIncludingLocal(classScope, identifierString);
		}
		
		return javaValue;
	}
	
	public static JavaValue searchVariableInFunction(JavaMethod javaMethod, String identifierString) {
		JavaValue javaValue = null;
		
		if(javaMethod.hasParameter(identifierString)) {
			javaValue = javaMethod.getParameter(identifierString);
		}
		else {
			javaValue = LocalScopeCreator.searchVariableInLocalIterative(identifierString, javaMethod.getParentLocalScope());
		}
		
		return javaValue;
	}
	
	public static JavaValue searchVariableInClassIncludingLocal(ClassScope classScope, String identifierString) {
		return classScope.searchVariableIncludingLocal(identifierString);
	}
	
	public static JavaValue searchVariableInClass(ClassScope classScope, String identifierString) {
		return classScope.searchVariable(identifierString);
	}
	
}
