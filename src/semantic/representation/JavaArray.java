package semantic.representation;

import Execution.ExecutionManager;
import semantic.representation.JavaValue.PrimitiveType;
import semantic.utils.AssignmentUtils;
import semantic.utils.StringUtils;

/**
 * A representation of an array. For now, we only accept 1D array.
 */
public class JavaArray {
	
	private JavaValue[] javaValueArray;
	private PrimitiveType arrayPrimitiveType;
	private String arrayIdentifier;
	private boolean finalFlag = false;
	
	public JavaArray(PrimitiveType primitiveType, String identifier) {
		this.arrayPrimitiveType = primitiveType;
		this.arrayIdentifier = identifier;
	}
	
	public void setPrimitiveType(PrimitiveType primitiveType) {
		this.arrayPrimitiveType = primitiveType;
	}
	
	public PrimitiveType getPrimitiveType() {
		return this.arrayPrimitiveType;
	}
	
	public void markFinal() {
		this.finalFlag = true;
	}
	
	public boolean isFinal() {
		return this.finalFlag;
	}
	
	public void initializeSize(int size) {
		this.javaValueArray = new JavaValue[size];
		System.out.println("Java array initialized to size " + javaValueArray.length);
	}
	
	public int getSize() {
		return this.javaValueArray.length;
	}
	
	public void updateValueAt(JavaValue javaValue, int index) {
		if(index >= this.javaValueArray.length) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError(
					"JavaArray: Out of bounds exception for array " + this.arrayIdentifier + " Index: " + index));
			return;
		}
		this.javaValueArray[index] = javaValue;
	}
	
	public JavaValue getValueAt(int index) {
		if(index >= this.javaValueArray.length) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError(
					"JavaArray: Out of bounds exception for array " + this.arrayIdentifier + " Index: " + index));
			return this.javaValueArray[javaValueArray.length - 1];
		}
		else {
			return this.javaValueArray[index];
		}
	}
	
	/*
	 * Utility function that returns an array of specified primitive type.
	 */
	public static JavaArray createArray(String primitiveTypeString, String arrayIdentifier) {
		//identify primitive type
		PrimitiveType primitiveType = AssignmentUtils.assignPrimitiveType(primitiveTypeString);
		return new JavaArray(primitiveType, arrayIdentifier);
	}
}
