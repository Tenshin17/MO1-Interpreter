//package VarAndConstDec;
package baraco.representations;

import baraco.builder.ParserHandler;
import baraco.execution.MethodTracker;
import baraco.semantics.symboltable.SymbolTableManager;
import baraco.semantics.symboltable.scopes.ClassScope;
import baraco.semantics.symboltable.scopes.LocalScopeCreator;

public class BaracoValueSearcher {
    private final static String TAG = "BaracoValueSearcher";

    public static BaracoValue searchBaracoValue(String identifier) {

        BaracoValue baracoValue = null;

        if(MethodTracker.getInstance().isInsideFunction()) {
            BaracoMethod mobiFunction = MethodTracker.getInstance().getLatestFunction();

            if(mobiFunction.hasParameter(identifier)) {
                baracoValue =  mobiFunction.getParameter(identifier);
            }
            else {
                baracoValue = LocalScopeCreator.searchVariableInLocalIterative(identifier, mobiFunction.getParentLocalScope());
            }
        }

        if(baracoValue == null) {
            ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
            baracoValue = classScope.searchVariableIncludingLocal(identifier);
        }

        return baracoValue;

    }
}
