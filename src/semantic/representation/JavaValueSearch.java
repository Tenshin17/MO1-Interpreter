package semantic.representation;

import error.ParserHandler;
import Execution.FunctionTracker;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import semantic.symboltable.scope.LocalScopeCreator;

/**
 * A component that searches for the corresponding Java value.
 * If it is in a function, it looks in the function parameters and local scope first before the global scope.
 * TODO: Can be expanded to properly search for a value if OOP is implemented.

 *
 */
public class JavaValueSearch {

	public static JavaValue searchJavaValue(String identifier) {
		
		JavaValue javaValue = null;
		
		if(FunctionTracker.getInstance().isInsideFunction()) {
			JavaMethod javaMethod = FunctionTracker.getInstance().getLatestFunction();
			
			if(javaMethod.hasParameter(identifier)) {
				javaValue =  javaMethod.getParameter(identifier);
			}
			else {
				javaValue = LocalScopeCreator.searchVariableInLocalIterative(identifier, javaMethod.getParentLocalScope());
			}
		}
		
		if(javaValue == null) {
			ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
			javaValue = classScope.searchVariableIncludingLocal(identifier);
		}
		
		return javaValue;
		
	}
}
