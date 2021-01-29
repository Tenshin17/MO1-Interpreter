//package baraco.semantics.symboltable.scopes;
package symboltable.scope;

//import baraco.execution.commands.MethodList; <??? don;t know where this is
//import baraco.representations.BaracoMethod;
//import baraco.representations.BaracoValue;
//import baraco.representations.RecognizedKeywords;

import VarAndConstDec.javaValue;
import VarAndConstDec.javaMethod;
import VarAndConstDec.javaKeywords;
import java.util.HashMap;

public class ClassScope implements IScope{

    private final static String TAG = "ClassScope";

    private String className;

//    private HashMap<String, BaracoValue> publicVariables;
//    private HashMap<String, BaracoValue> privateVariables;
//
//    private HashMap<String, BaracoMethod> publicMethods;
//    private HashMap<String, BaracoMethod> privateMethods;
    private HashMap<String, javaValue> publicVariables;
    private HashMap<String, javaValue> privateVariables;

    private HashMap<String, javaMethod> publicMethods;
    private HashMap<String, javaMethod> privateMethods;

    private LocalScope parentLocalScope; //represents the parent local scope which is the local scope covered by the main() function. Other classes may not contain this.

    public ClassScope(String className) {
        this.className = className;

//        this.publicVariables = new HashMap<String, BaracoValue>();
//        this.privateVariables = new HashMap<String, BaracoValue>();
//
//        this.publicMethods = new HashMap<String, BaracoMethod>();
//        this.privateMethods = new HashMap<String, BaracoMethod>();
        this.publicVariables = new HashMap<String, javaValue>();
        this.privateVariables = new HashMap<String, javaValue>();

        this.publicMethods = new HashMap<String, javaMethod>();
        this.privateMethods = new HashMap<String, javaMethod>();
    }

    public String getClassName() {
        return this.className;
    }


    /*
     * Sets the parent local scope which is instantiated if this class contains a main function.
     */
    public void setParentLocalScope(LocalScope localScope) {
        this.parentLocalScope = localScope;
    }

    @Override
    /*
     * A class scope is automatically the parent of local scopes.
     * (non-Javadoc)
     * @see com.neildg.mobiprog.semantics.symboltable.scopes.IScope#isParent()
     */
    public boolean isParent(){
        return true;
    }

    /*
     * Attempts to add an empty variable based from keywords
     */
    public void addEmptyVariableFromKeywords(String classModifierString, String primitiveTypeString, String identifierString) {
        boolean isPublic = true;

//        if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.CLASS_MODIFIER_PRIVATE, classModifierString)) {
        if(javaKeywords.matchesKeyword(javaKeywords.CLASS_MODIFIER_PRIVATE, classModifierString)) {
            isPublic = false;
        }

        //create empty mobi value
//        BaracoValue mobiValue = BaracoValue.createEmptyVariableFromKeywords(primitiveTypeString);
        javaValue javaVal = javaValue.createEmptyVariableFromKeywords(primitiveTypeString);

        if(isPublic) {
            this.publicVariables.put(identifierString, javaVal);
            System.out.println("Created public variable " +identifierString+ " type: " +javaVal.getPrimitiveType());
        }
        else {
            this.privateVariables.put(identifierString, javaVal);
            System.out.println("Created private variable " +identifierString+ " type: " +javaVal.getPrimitiveType());
        }
    }

    /*
     * Attempts to add an initialized variable mobi value
     */
    public void addInitializedVariableFromKeywords(String classModifierString, String primitiveTypeString, String identifierString, String valueString) {
        boolean isPublic = true;

//        if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.CLASS_MODIFIER_PRIVATE, classModifierString)) {
        if(javaKeywords.matchesKeyword(javaKeywords.CLASS_MODIFIER_PRIVATE, classModifierString)) {
            isPublic = false;
        }

        this.addEmptyVariableFromKeywords(classModifierString, primitiveTypeString, identifierString);

        if(isPublic) {
//            BaracoValue baracoValue = this.publicVariables.get(identifierString);
//            baracoValue.setValue(valueString);
//            System.out.println("Updated public variable " +identifierString+ " of type " +baracoValue.getPrimitiveType()+ " with value " +valueString);

            javaValue javaVal = this.publicVariables.get(identifierString);
            javaVal.setValue(valueString);
            System.out.println("Updated public variable " +identifierString+ " of type " +javaVal.getPrimitiveType()+ " with value " +valueString);
        }
        else {
//            BaracoValue baracoValue = this.privateVariables.get(identifierString);
//            baracoValue.setValue(valueString);
//            System.out.println("Updated private variable " +identifierString+ " of type " +baracoValue.getPrimitiveType()+ " with value " +valueString);
            javaValue javaVal = this.privateVariables.get(identifierString);
            javaVal.setValue(valueString);
            System.out.println("Updated private variable " +identifierString+ " of type " +javaVal.getPrimitiveType()+ " with value " +valueString);

        }
    }


//    public BaracoValue getPublicVariable(String identifier) {
    public javaValue getPublicVariable(String identifier) {

        if(this.containsPublicVariable(identifier)) {
            return this.publicVariables.get(identifier);
        }
        else {
            System.out.println(TAG + ": " + "Public " +identifier + " is not found.");
            return null;
        }
    }

//    public BaracoValue getPrivateVariable(String identifier) {
    public javaValue getPrivateVariable(String identifier) {

        if(this.containsPrivateVariable(identifier)) {
            return this.privateVariables.get(identifier);
        }
        else {
            System.out.println(TAG + ": " + "Private " +identifier + " is not found.");
            return null;
        }
    }

//    public void addPrivateBaracoMethod(String identifier, BaracoMethod baracoMethod) {
    public void addPrivateBaracoMethod(String identifier, javaMethod javaMet) {
//        this.privateMethods.put(identifier, baracoMethod);
//        System.out.println("Created private function " +identifier+ " with return type " + baracoMethod.getReturnType());
//        MethodList.getInstance().addMethodName(identifier);
        this.privateMethods.put(identifier, javaMet);
        System.out.println("Created private function " +identifier+ " with return type " + javaMet.getReturnType());
        MethodList.getInstance().addMethodName(identifier); //???methodlist
    }

//    public void addPublicBaracoMethod(String identifier, BaracoMethod baracoMethod) {
//        this.publicMethods.put(identifier, baracoMethod);
//        System.out.println("Created public function " +identifier+ " with return type " + baracoMethod.getReturnType());
//        MethodList.getInstance().addMethodName(identifier);
//    }
    public void addPublicBaracoMethod(String identifier, javaMethod javMet) {
        this.publicMethods.put(identifier, javMet);
        System.out.println("Created public function " +identifier+ " with return type " + javMet.getReturnType());
        MethodList.getInstance().addMethodName(identifier); //??method lsit
    }


//    public void addBaracoValue(String accessControlModifier, String identifier, BaracoValue baracoValue) {
//        boolean isPublic = true;
//
//        if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.CLASS_MODIFIER_PRIVATE, accessControlModifier)) {
//            isPublic = false;
//        }
//
//        if(isPublic){
//            this.publicVariables.put(identifier, baracoValue);
//        }
//        else {
//            this.privateVariables.put(identifier, baracoValue);
//        }
//    }
    public void addBaracoValue(String accessControlModifier, String identifier, javaValue javaVal) {
        boolean isPublic = true;

        if(javaKeywords.matchesKeyword(javaKeywords.CLASS_MODIFIER_PRIVATE, accessControlModifier)) {
            isPublic = false;
        }

        if(isPublic){
            this.publicVariables.put(identifier, javaVal);
        }
        else {
            this.privateVariables.put(identifier, javaVal);
        }
    }

//    public BaracoMethod getPublicMethod(String identifier) {
//        if(this.containsPublicFunction(identifier)) {
//            return this.publicMethods.get(identifier);
//        }
//        else {
//            System.out.println(TAG + ": " + "Private " +identifier+ " function is not found.");
//            return null;
//        }
//    }

    public javaMethod getPublicMethod(String identifier) {
        if(this.containsPublicFunction(identifier)) {
            return this.publicMethods.get(identifier);
        }
        else {
            System.out.println(TAG + ": " + "Private " +identifier+ " function is not found.");
            return null;
        }
    }

    public javaMethod getPrivateMethod(String identifier) {
        if(this.containsPublicFunction(identifier)) {
            return this.privateMethods.get(identifier);
        }
        else {
            System.out.println(TAG + ": " + "Public " +identifier+ " function is not found");
            return null;
        }
    }

    public javaMethod searchMethod(String identifier) {
        if(this.containsPublicFunction(identifier)) {
            return this.publicMethods.get(identifier);
        }
        else if(this.containsPrivateFunction(identifier)) {
            return this.privateMethods.get(identifier);
        }
        else {
            System.out.println(TAG + ": " + identifier + " is not found in " +this.className);
            return null;
        }
    }

    public boolean containsPublicFunction(String identifier) {
        return this.publicMethods.containsKey(identifier);
    }

    public boolean containsPrivateFunction(String identifier) {
        return this.privateMethods.containsKey(identifier);
    }

    @Override
    /* Attempts to find a variable first in the private and public variable storage, then on the local scopes.
     * (non-Javadoc)
     * @see com.neildg.mobiprog.semantics.symboltable.scopes.IScope#getVariable(java.lang.String)
     */
    public javaValue searchVariableIncludingLocal(String identifier) {

        javaValue value;

        if(this.containsPrivateVariable(identifier)) {
            value = this.getPrivateVariable(identifier);
        }
        else if(this.containsPublicVariable(identifier)) {
            value = this.getPublicVariable(identifier);
        }
        else {
//            value = baraco.semantics.symboltable.scopes.LocalScopeCreator.searchVariableInLocalIterative(identifier, this.parentLocalScope);
            value = symboltable.scope.LocalScopeCreator.searchVariableInLocalIterative(identifier, this.parentLocalScope);

        }

        return value;
    }

    public javaValue searchVariable(String identifier) {

        javaValue value = null;

        if(this.containsPrivateVariable(identifier)) {
            value = this.getPrivateVariable(identifier);
        }
        else if(this.containsPublicVariable(identifier)) {
            value = this.getPublicVariable(identifier);
        }

        return value;
    }

    public boolean containsPublicVariable(String identifier) {
        return this.publicVariables.containsKey(identifier);
    }

    public boolean containsPrivateVariable(String identifier) {
        return this.privateVariables.containsKey(identifier);
    }

    /*
     * Resets all stored variables. This is called after the execution manager finishes
     */
    public void resetValues() {
        javaValue[] publicBaracoValues = this.publicVariables.values().toArray(new javaValue[this.publicVariables.size()]);
        javaValue[] privateBaracoValues = this.privateVariables.values().toArray(new javaValue[this.privateVariables.size()]);

        for(int i = 0; i < publicBaracoValues.length; i++) {
            publicBaracoValues[i].reset();
        }

        for(int i = 0; i < privateBaracoValues.length; i++) {
            privateBaracoValues[i].reset();
        }
    }





//    public BaracoMethod getPrivateMethod(String identifier) {
//        if(this.containsPublicFunction(identifier)) {
//            return this.privateMethods.get(identifier);
//        }
//        else {
//            System.out.println(TAG + ": " + "Public " +identifier+ " function is not found");
//            return null;
//        }
//    }

//    public BaracoMethod searchMethod(String identifier) {
//        if(this.containsPublicFunction(identifier)) {
//            return this.publicMethods.get(identifier);
//        }
//        else if(this.containsPrivateFunction(identifier)) {
//            return this.privateMethods.get(identifier);
//        }
//        else {
//            System.out.println(TAG + ": " + identifier + " is not found in " +this.className);
//            return null;
//        }
//    }
//
//    public boolean containsPublicFunction(String identifier) {
//        return this.publicMethods.containsKey(identifier);
//    }
//
//    public boolean containsPrivateFunction(String identifier) {
//        return this.privateMethods.containsKey(identifier);
//    }
//
//    @Override
//    /* Attempts to find a variable first in the private and public variable storage, then on the local scopes.
//     * (non-Javadoc)
//     * @see com.neildg.mobiprog.semantics.symboltable.scopes.IScope#getVariable(java.lang.String)
//     */
//    public BaracoValue searchVariableIncludingLocal(String identifier) {
//
//        BaracoValue value;
//
//        if(this.containsPrivateVariable(identifier)) {
//            value = this.getPrivateVariable(identifier);
//        }
//        else if(this.containsPublicVariable(identifier)) {
//            value = this.getPublicVariable(identifier);
//        }
//        else {
//            value = LocalScopeCreator.searchVariableInLocalIterative(identifier, this.parentLocalScope);
//        }
//
//        return value;
//    }
//
//    public BaracoValue searchVariable(String identifier) {
//
//        BaracoValue value = null;
//
//        if(this.containsPrivateVariable(identifier)) {
//            value = this.getPrivateVariable(identifier);
//        }
//        else if(this.containsPublicVariable(identifier)) {
//            value = this.getPublicVariable(identifier);
//        }
//
//        return value;
//    }
//
//    public boolean containsPublicVariable(String identifier) {
//        return this.publicVariables.containsKey(identifier);
//    }
//
//    public boolean containsPrivateVariable(String identifier) {
//        return this.privateVariables.containsKey(identifier);
//    }
//
//    /*
//     * Resets all stored variables. This is called after the execution manager finishes
//     */
//    public void resetValues() {
//        BaracoValue[] publicBaracoValues = this.publicVariables.values().toArray(new BaracoValue[this.publicVariables.size()]);
//        BaracoValue[] privateBaracoValues = this.privateVariables.values().toArray(new BaracoValue[this.privateVariables.size()]);
//
//        for(int i = 0; i < publicBaracoValues.length; i++) {
//            publicBaracoValues[i].reset();
//        }
//
//        for(int i = 0; i < privateBaracoValues.length; i++) {
//            privateBaracoValues[i].reset();
//        }
//    }
}
