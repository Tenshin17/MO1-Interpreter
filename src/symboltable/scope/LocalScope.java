//package baraco.semantics.symboltable.scopes;
package symboltable.scope;

//import baraco.representations.BaracoValue;
import VarAndConstDec.javaValue;

import java.util.ArrayList;
import java.util.HashMap;

public class LocalScope implements IScope {

    private final static String TAG = "MobiProg_LocalScope";

    private IScope parentScope;
    private ArrayList<LocalScope> childScopeList = null;

//    private HashMap<String, BaracoValue> localVariables = null;
    private HashMap<String, javaValue> localVariables = null;


    public LocalScope() {
        this.parentScope = null;
    }

    public LocalScope(IScope parentScope) {
        this.parentScope = parentScope;
    }

    /*
     * Initialize the moment a variable is about to be placed.
     */
    public void initializeLocalVariableMap() {
        if(this.localVariables == null) {
//            this.localVariables = new HashMap<String, BaracoValue>();
            this.localVariables = new HashMap<String, javaValue>();

        }
    }

    /*
     * Initialize the child list the moment a child scope is about to be placed
     */
    public void initializeChildList() {
        if(this.childScopeList == null) {
            this.childScopeList = new ArrayList<LocalScope>();
        }
    }

    public void setParent(IScope parentScope) {
        this.parentScope = parentScope;
    }

    public void addChild(LocalScope localScope) {
        this.initializeChildList();

        this.childScopeList.add(localScope);
    }

    public boolean isParent() {
        return (this.parentScope == null);
    }

    public IScope getParent() {
        return this.parentScope;
    }

    public int getChildCount() {
        if(this.childScopeList != null)
            return this.childScopeList.size();
        else
            return 0;
    }

    public LocalScope getChildAt(int index) {
        if(this.childScopeList != null)
            return this.childScopeList.get(index);
        else
            return null;
    }

    @Override
//    public BaracoValue searchVariableIncludingLocal(String identifier) {
    public javaValue searchVariableIncludingLocal(String identifier) {
        if(this.containsVariable(identifier)) {
            return this.localVariables.get(identifier);
        }
        else {
            System.out.println(TAG + ": " + identifier + " not found!");
            return null;
        }
    }

    public boolean containsVariable(String identifier) {
        if(this.localVariables!= null && this.localVariables.containsKey(identifier)) {
            return true;
        }
        else {
            return false;
        }
    }

    /*
     * Adds an empty variable based from keywords
     */
    public void addEmptyVariableFromKeywords(String primitiveTypeString, String identifierString) {
        this.initializeLocalVariableMap();

//        BaracoValue baracoValue = BaracoValue.createEmptyVariableFromKeywords(primitiveTypeString);
        javaValue javaval = javaValue.createEmptyVariableFromKeywords(primitiveTypeString);

        this.localVariables.put(identifierString, javaval);

    }

    /*
     * Adds an initialized variable based from keywords
     */
    public void addInitializedVariableFromKeywords(String primitiveTypeString, String identifierString, String valueString) {
        this.initializeLocalVariableMap();

        this.addEmptyVariableFromKeywords(primitiveTypeString, identifierString);
//        BaracoValue baracoValue = this.localVariables.get(identifierString);
//        baracoValue.setValue(valueString);
        javaValue javaval = this.localVariables.get(identifierString);
        javaval.setValue(valueString);
    }

    public void addFinalEmptyVariableFromKeywords(String primitiveTypeString, String identifierString) {
        this.initializeLocalVariableMap();

//        BaracoValue baracoValue = BaracoValue.createEmptyVariableFromKeywords(primitiveTypeString);
//        baracoValue.markFinal();
        javaValue javaval = javaValue.createEmptyVariableFromKeywords(primitiveTypeString);
        javaval.markFinal();
        this.localVariables.put(identifierString, javaval);
    }

    public void addFinalInitVariableFromKeyWords(String primitiveTypeString, String identifierString, String valueString) {
        this.initializeLocalVariableMap();

        this.addEmptyVariableFromKeywords(primitiveTypeString, identifierString);
//        BaracoValue baracoValue = this.localVariables.get(identifierString);
//        baracoValue.setValue(valueString);
//        baracoValue.markFinal();
        javaValue javaval = this.localVariables.get(identifierString);
        javaval.setValue(valueString);
        javaval.markFinal();
    }

//    public void addMobiValue(String identifier, BaracoValue baracoValue) {
    public void addMobiValue(String identifier, javaValue javaval) {
        this.initializeLocalVariableMap();
        this.localVariables.put(identifier, javaval);
    }

    /*
     * Returns the depth of this local scope.
     */
    public int getDepth() {
        int depthCount = -1;

        LocalScope scope = (LocalScope) this;

        while(scope != null) {
            depthCount++;

            IScope abstractScope = scope.getParent();

            if(abstractScope instanceof ClassScope)
                break;

            scope = (LocalScope) abstractScope;
        }

        return depthCount;
    }
}
