package semantic.symboltable.scope;

import Execution.ExecutionManager;
import Execution.command.MethodList;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.utils.RecognizedKeywords;
import semantic.utils.StringUtils;

import java.util.HashMap;

/**
 * Represents a class scope with mappings of variables and functions
 */
public class ClassScope implements IScope {

	private String className;

	private HashMap<String, JavaValue> publicVariables;
	private HashMap<String, JavaValue> privateVariables;

	private HashMap<String, JavaMethod> publicMethods;
	private HashMap<String, JavaMethod> privateMethods;

	private LocalScope parentLocalScope; //represents the parent local scope which is the local scope covered by the main() function. Other classes may not contain this.

	public ClassScope(String className) {
		this.className = className;

		this.publicVariables = new HashMap<String, JavaValue>();
		this.privateVariables = new HashMap<String, JavaValue>();

		this.publicMethods = new HashMap<String, JavaMethod>();
		this.privateMethods = new HashMap<String, JavaMethod>();
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
	public boolean isParent(){
		return true;
	}

	/*
	 * Attempts to add an empty variable based from keywords
	 */
	public void addEmptyVariableFromKeywords(String classModifierString, String primitiveTypeString, String identifierString) {
		boolean isPublic = true;

		if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.CLASS_MODIFIER_PRIVATE, classModifierString)) {
			isPublic = false;
		}

		//create empty java value
		JavaValue javaValue = JavaValue.createEmptyVariableFromKeywords(primitiveTypeString);

		if(isPublic) {
			this.publicVariables.put(identifierString, javaValue);
			System.out.println("Created public variable " +identifierString+ " type: " +javaValue.getPrimitiveType());
		}
		else {
			this.privateVariables.put(identifierString, javaValue);
			System.out.println("Created private variable " +identifierString+ " type: " +javaValue.getPrimitiveType());
		}
	}

	/*
	 * Attempts to add an initialized variable java value
	 */
	public void addInitializedVariableFromKeywords(String classModifierString, String primitiveTypeString, String identifierString, String valueString) {
		boolean isPublic = true;

		if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.CLASS_MODIFIER_PRIVATE, classModifierString)) {
			isPublic = false;
		}

		this.addEmptyVariableFromKeywords(classModifierString, primitiveTypeString, identifierString);

		if(isPublic) {
			JavaValue baracoValue = this.publicVariables.get(identifierString);
			baracoValue.setValue(valueString);
			System.out.println("Updated public variable " +identifierString+ " of type " +baracoValue.getPrimitiveType()+ " with value " +valueString);
		}
		else {
			JavaValue baracoValue = this.privateVariables.get(identifierString);
			baracoValue.setValue(valueString);
			System.out.println("Updated private variable " +identifierString+ " of type " +baracoValue.getPrimitiveType()+ " with value " +valueString);
		}
	}

	public JavaValue getPublicVariable(String identifier) {
		if(this.containsPublicVariable(identifier)) {
			return this.publicVariables.get(identifier);
		}
		else {
			System.out.println("ClassScope: " + "Public " +identifier + " is not found.");
			return null;
		}
	}

	public JavaValue getPrivateVariable(String identifier) {
		if(this.containsPrivateVariable(identifier)) {
			return this.privateVariables.get(identifier);
		}
		else {
			System.out.println("ClassScope: " + "Private " +identifier + " is not found.");
			return null;
		}
	}

	public void addPrivateJavaMethod(String identifier, JavaMethod baracoMethod) {
		this.privateMethods.put(identifier, baracoMethod);
		System.out.println("Created private function " +identifier+ " with return type " + baracoMethod.getReturnType());
		MethodList.getInstance().addMethodName(identifier);
	}

	public void addPublicJavaMethod(String identifier, JavaMethod baracoMethod) {
		this.publicMethods.put(identifier, baracoMethod);
		System.out.println("Created public function " +identifier+ " with return type " + baracoMethod.getReturnType());
		MethodList.getInstance().addMethodName(identifier);
	}

	public void addJavaValue(String accessControlModifier, String identifier, JavaValue baracoValue) {
		boolean isPublic = true;

		if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.CLASS_MODIFIER_PRIVATE, accessControlModifier)) {
			isPublic = false;
		}

		if(isPublic){
			this.publicVariables.put(identifier, baracoValue);
		}
		else {
			this.privateVariables.put(identifier, baracoValue);
		}
	}

	public JavaMethod getPublicMethod(String identifier) {
		if(this.containsPublicFunction(identifier)) {
			return this.publicMethods.get(identifier);
		}
		else {
			System.out.println("ClassScope: " + "Private " +identifier+ " function is not found.");
			return null;
		}
	}

	public JavaMethod getPrivateMethod(String identifier) {
		if(this.containsPublicFunction(identifier)) {
			return this.privateMethods.get(identifier);
		}
		else {
			System.out.println("ClassScope: " + "Public " +identifier+ " function is not found");
			return null;
		}
	}

	public JavaMethod searchMethod(String identifier) {
		if(this.containsPublicFunction(identifier)) {
			return this.publicMethods.get(identifier);
		}
		else if(this.containsPrivateFunction(identifier)) {
			return this.privateMethods.get(identifier);
		}
		else {
			System.out.println("ClassScope: " + identifier + " is not found in " +this.className);
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
	public JavaValue searchVariableIncludingLocal(String identifier) {

		JavaValue value;

		if(this.containsPrivateVariable(identifier)) {
			value = this.getPrivateVariable(identifier);
		}
		else if(this.containsPublicVariable(identifier)) {
			value = this.getPublicVariable(identifier);
		}
		else {
			value = LocalScopeCreator.searchVariableInLocalIterative(identifier, this.parentLocalScope);
		}

		return value;
	}

	public JavaValue searchVariable(String identifier) {

		JavaValue value = null;

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
		JavaValue[] publicJavaValues = this.publicVariables.values().toArray(new JavaValue[this.publicVariables.size()]);
		JavaValue[] privateJavaValues = this.privateVariables.values().toArray(new JavaValue[this.privateVariables.size()]);

		for(int i = 0; i < publicJavaValues.length; i++) {
			publicJavaValues[i].reset();
		}

		for(int i = 0; i < privateJavaValues.length; i++) {
			privateJavaValues[i].reset();
		}
	}
}
