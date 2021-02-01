package Execution;

import semantic.representation.JavaMethod;

import java.util.Stack;


/**
 * Holds the current function that the control flow is in.
 *
 */
public class FunctionTracker {
	
	private static FunctionTracker functionTracker = null;
	
	private Stack<JavaMethod> callStack;
	
	public static FunctionTracker getInstance() {
		return functionTracker;
	}
	
	private FunctionTracker() {
		this.callStack = new Stack<>();
	}
	
	public static void initialize() {
		functionTracker = new FunctionTracker();
	}
	
	public static void reset() {

	}
	
	public void reportEnterFunction(JavaMethod javaMethod) {
		this.callStack.push(javaMethod);
	}
	
	public void reportExitFunction() {
		this.callStack.pop();
	}
	
	public JavaMethod getLatestFunction() {
		return this.callStack.peek();
	}
	
	/*
	 * Returns true if the control flow is currently inside a function
	 */
	public boolean isInsideFunction() {
		return (this.callStack.size() != 0);
	}
	
}
