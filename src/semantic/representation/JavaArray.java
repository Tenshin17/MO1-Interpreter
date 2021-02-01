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
		arrayPrimitiveType = primitiveType;
		arrayIdentifier = identifier;
	}
	
	public void setPrimitiveType(PrimitiveType primitiveType) {
		arrayPrimitiveType = primitiveType;
	}
	
	public PrimitiveType getPrimitiveType() {
		return arrayPrimitiveType;
	}
	
	public void markFinal() {
		finalFlag = true;
	}
	
	public boolean isFinal() {
		return finalFlag;
	}
	
	public void initializeSize(int size) {
		javaValueArray = new JavaValue[size];
		System.out.println("Java array initialized to size " + javaValueArray.length);
	}
	
	public int getSize() {
		return javaValueArray.length;
	}
	
	public void updateValueAt(JavaValue javaValue, int index) {
		if(index >= javaValueArray.length) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError(
					"JavaArray: Out of bounds exception for array " + arrayIdentifier + " Index: " + index));
			return;
		}
		javaValueArray[index] = javaValue;
	}
	
	public JavaValue getValueAt(int index) {
		if(index >= javaValueArray.length) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError(
					"JavaArray: Out of bounds exception for array " + arrayIdentifier + " Index: " + index));
			return javaValueArray[javaValueArray.length - 1];
		}
		else {
			return javaValueArray[index];
		}
	}
	
	/*
	 * Utility function that returns an arary of specified primitive type.
	 */
	public static JavaArray createArray(String primitiveTypeString, String arrayIdentifier) {
		//identify primitive type
		PrimitiveType primitiveType = AssignmentUtils.assignPrimitiveType(primitiveTypeString);
		return new JavaArray(primitiveType, arrayIdentifier);
	}
}
