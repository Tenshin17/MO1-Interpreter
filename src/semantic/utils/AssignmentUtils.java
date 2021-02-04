package semantic.utils;

import semantic.representation.JavaValue;
import semantic.representation.JavaValue.PrimitiveType;

import java.math.BigDecimal;

/**
 * Assignment utilities are put here.

 *
 */
public class AssignmentUtils {

	/*
	 * Assigns an appropriate value depending on the primitive type. Since expression class returns a double value, we attempt
	 * to properly cast it. All expression commands accept INT, LONG, BYTE, SHORT, FLOAT and DOUBLE.
	 */
	public static void assignAppropriateValue(JavaValue javaValue, BigDecimal evaluationValue) {

		// No type specified
		if(javaValue == null){
			System.out.println("JavaValue: Primitive type not specified ");
		}
		else {
			if (javaValue.getPrimitiveType() == PrimitiveType.INT) {
				javaValue.setValue(Integer.toString(evaluationValue.intValue()));
			} else if (javaValue.getPrimitiveType() == PrimitiveType.LONG) {
				javaValue.setValue(Long.toString(evaluationValue.longValue()));
			} else if (javaValue.getPrimitiveType() == PrimitiveType.BYTE) {
				javaValue.setValue(Byte.toString(evaluationValue.byteValue()));
			} else if (javaValue.getPrimitiveType() == PrimitiveType.SHORT) {
				javaValue.setValue(Short.toString(evaluationValue.shortValue()));
			} else if (javaValue.getPrimitiveType() == PrimitiveType.FLOAT) {
				javaValue.setValue(Float.toString(evaluationValue.floatValue()));
			} else if (javaValue.getPrimitiveType() == PrimitiveType.DOUBLE) {
				javaValue.setValue(Double.toString(evaluationValue.doubleValue()));
			} else if (javaValue.getPrimitiveType() == PrimitiveType.BOOLEAN) {
				int result = evaluationValue.intValue();

				if (result == 1) {
					javaValue.setValue(RecognizedKeywords.BOOLEAN_TRUE);
				} else {
					javaValue.setValue(RecognizedKeywords.BOOLEAN_FALSE);
				}
			} else {
				System.out.println("JavaValue: No appropriate type :(");
			}
		}
	}

	public static PrimitiveType assignPrimitiveType(String primitiveTypeString){
		if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_BOOLEAN, primitiveTypeString))
			return PrimitiveType.BOOLEAN;
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_BYTE, primitiveTypeString))
			return PrimitiveType.BYTE;
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_CHAR, primitiveTypeString))
			return PrimitiveType.CHAR;
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_DOUBLE, primitiveTypeString))
			return PrimitiveType.DOUBLE;
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_FLOAT, primitiveTypeString))
			return PrimitiveType.FLOAT;
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_INT, primitiveTypeString))
			return PrimitiveType.INT;
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_LONG, primitiveTypeString))
			return PrimitiveType.LONG;
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_SHORT, primitiveTypeString))
			return PrimitiveType.SHORT;
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_STRING, primitiveTypeString))
			return PrimitiveType.STRING;
		return PrimitiveType.NOT_YET_IDENTIFIED;
	}
	
}
