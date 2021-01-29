//package VarAndConstDec;
package VarAndConstDec;

import java.builder.ParserHandler;
import java.execution.MethodTracker;
import java.semantics.symboltable.SymbolTableManager;
import java.semantics.symboltable.scopes.ClassScope;
import java.semantics.symboltable.scopes.LocalScopeCreator;

public class javaValueSearch {
    private final static String TAG = "javaValueSearcher";

    public static javaValue searchjavaValue(String identifier) {

        javaValue javaValue = null;

        if(MethodTracker.getInstance().isInsideFunction()) {
            javaMethod mobiFunction = MethodTracker.getInstance().getLatestFunction();

            if(mobiFunction.hasParameter(identifier)) {
                javaValue =  mobiFunction.getParameter(identifier);
            }
            else {
                javaValue = LocalScopeCreator.searchVariableInLocalIterative(identifier, mobiFunction.getParentLocalScope());
            }
        }

        if(javaValue == null) {
            ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
            javaValue = classScope.searchVariableIncludingLocal(identifier);
        }

        return javaValue;

    }
}
