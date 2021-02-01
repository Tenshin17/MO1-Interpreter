package semantic.representation;

import semantic.utils.AssignmentUtils;

/**
 * Represents a value for type checking mechanisms.
 * This class can check if the value assigned is appropriate for the given primitive type.
 *
 */
public class JavaValue {
	
	//these are the accepted primitive types
	public enum PrimitiveType {
		NOT_YET_IDENTIFIED,
		BOOLEAN,
		CHAR,
		INT,
		BYTE,
		SHORT,
		LONG,
		FLOAT,
		DOUBLE,
		STRING,
		ARRAY
	}
		
	private Object defaultValue; //this value will no longer change.
	private Object value;
	private PrimitiveType primitiveType = PrimitiveType.NOT_YET_IDENTIFIED;
	private boolean finalFlag = false;
	
	
	public JavaValue(Object value, PrimitiveType primitiveType) {
		if(value == null || checkValueType(value, primitiveType)) {
			this.value = value;
			this.primitiveType = primitiveType;
		}
		else {
			System.err.println("JavaValue: " + "Value " + value + " is not appropriate for  " + primitiveType+ "!");
		}
	}
	
	public void setPrimitiveType(PrimitiveType primitiveType) {
		this.primitiveType = primitiveType;
	}
	
	public void reset() { value = defaultValue;
	}
	
	/*
	 * Marks this value as final if there is a final keyword
	 */
	public void markFinal() {
		finalFlag = true;
	}
	
	public boolean isFinal() {
		return finalFlag;
	}
	
	public void setValue(String value) {
		
		if(primitiveType == PrimitiveType.NOT_YET_IDENTIFIED) {
			System.err.println("JavaValue: " +  "Primitive type not yet identified!");
		}
		else if(primitiveType == PrimitiveType.ARRAY) {
			System.err.println("JavaValue: " +  primitiveType + " is an array. Cannot directly change value.");
		}
		else {
			//attempts to type cast the value
			this.value = attemptTypeCast(value);
		}
	}
	
	
	private Object attemptTypeCast(String value) {
		switch(this.primitiveType) {
			case BOOLEAN: return Boolean.valueOf(value);
			case BYTE: return Byte.valueOf(value);
			case CHAR: return value.charAt(0); //only get first char at value
			case INT: return Integer.valueOf(value);
			case LONG: return Long.valueOf(value);
			case SHORT: return Short.valueOf(value);
			case FLOAT: return Float.valueOf(value);
			case DOUBLE: return Double.valueOf(value);
			case STRING: return value;
			default: return null;
		}
	}
	
	public Object getValue() {
		return value;
	}
	
	public PrimitiveType getPrimitiveType() {
		return primitiveType;
	}

	
	public static boolean checkValueType(Object value, PrimitiveType primitiveType) {
		switch(primitiveType) {
			case BOOLEAN:
				return value instanceof Boolean;
			case CHAR:
				return value instanceof Character;
			case INT:
				return value instanceof Integer;
			case BYTE:
				return value instanceof Byte;
			case SHORT:
				return value instanceof Short;
			case LONG:
				return value instanceof Long;
			case FLOAT:
				return value instanceof Float;
			case DOUBLE:
				return value instanceof Double;
			case STRING:
				return value instanceof String;
			case ARRAY:
				return value != null;
			default:
				return false;
		}
	}
	
	/*
	 * Utility function.
	 * Attempts to add an empty variable based from keywords
	 */
	public static JavaValue createEmptyVariableFromKeywords(String primitiveTypeString) {
		//identify primitive type
		PrimitiveType primitiveType = AssignmentUtils.assignPrimitiveType(primitiveTypeString);
		//create empty java value
		return new JavaValue(null, primitiveType);
	}
	
}
