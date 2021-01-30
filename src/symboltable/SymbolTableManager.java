//package baraco.semantics.symboltable;
package symboltable;
//import baraco.semantics.symboltable.scopes.ClassScope;

import symboltable.scope.ClassScope;

import java.util.HashMap;

public class SymbolTableManager {

    private final static String TAG = "SymbolTableManager";

    private static SymbolTableManager sharedInstance = null;

    public static SymbolTableManager getInstance() {
        return sharedInstance;
    }

    private HashMap<String, ClassScope> classTable;
    private ClassScope latestClassScope;

    private SymbolTableManager() {
        this.classTable = new HashMap<String, ClassScope>();
    }

    public static void initialize() {
        sharedInstance = new SymbolTableManager();
    }

    public static void reset() {
        sharedInstance.classTable.clear();
    }

    public void addClassScope(String className, ClassScope classScope) {
        this.classTable.put(className, classScope);
        this.latestClassScope = classScope;
    }

    public ClassScope getClassScope(String className) {
        if(this.containsClassScope(className)) {
            return this.classTable.get(className);
        }
        else {
            System.out.println(TAG + ": " + className + " is not found!");
            return null;
        }
    }

    public ClassScope getLatestScope() {
        return this.latestClassScope;
    }

    public boolean containsClassScope(String className) {
        return this.classTable.containsKey(className);
    }

    public void resetClassTables() {
        ClassScope[] classScopes = this.classTable.values().toArray(new ClassScope[this.classTable.size()]);

        for(int i = 0; i < classScopes.length; i++) {
            classScopes[i].resetValues();
        }
    }
}
