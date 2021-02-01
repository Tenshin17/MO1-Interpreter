package semantic.symboltable.scope;

import semantic.representation.JavaValue;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a local scope, which is either a function, a code block or inside a certain loop/conditional statement.
 * This local scope starts as an empty representation of a class.
 *
 * LocalScope can be viewed as a tree structure wherein it has a parent and children.

 *
 */
public class LocalScope implements IScope {

	private IScope parentScope;
	private ArrayList<LocalScope> childScopeList = null;

	private HashMap<String, JavaValue> localVariables = null;

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
			this.localVariables = new HashMap<String, JavaValue>();
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
	public JavaValue searchVariableIncludingLocal(String identifier) {
		if(this.containsVariable(identifier)) {
			return this.localVariables.get(identifier);
		}
		else {
			System.out.println("LocalScope: " + identifier + " not found!");
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

		JavaValue baracoValue = JavaValue.createEmptyVariableFromKeywords(primitiveTypeString);
		this.localVariables.put(identifierString, baracoValue);
	}

	/*
	 * Adds an initialized variable based from keywords
	 */
	public void addInitializedVariableFromKeywords(String primitiveTypeString, String identifierString, String valueString) {
		this.initializeLocalVariableMap();

		this.addEmptyVariableFromKeywords(primitiveTypeString, identifierString);
		JavaValue baracoValue = this.localVariables.get(identifierString);
		baracoValue.setValue(valueString);
	}

	public void addFinalEmptyVariableFromKeywords(String primitiveTypeString, String identifierString) {
		this.initializeLocalVariableMap();

		JavaValue baracoValue = JavaValue.createEmptyVariableFromKeywords(primitiveTypeString);
		baracoValue.markFinal();
		this.localVariables.put(identifierString, baracoValue);
	}

	public void addFinalInitVariableFromKeyWords(String primitiveTypeString, String identifierString, String valueString) {
		this.initializeLocalVariableMap();

		this.addEmptyVariableFromKeywords(primitiveTypeString, identifierString);
		JavaValue baracoValue = this.localVariables.get(identifierString);
		baracoValue.setValue(valueString);
		baracoValue.markFinal();
	}

	public void addJavaValue(String identifier, JavaValue baracoValue) {
		this.initializeLocalVariableMap();
		this.localVariables.put(identifier, baracoValue);
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