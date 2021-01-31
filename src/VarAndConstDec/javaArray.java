//package VarAndConstDec;
package VarAndConstDec;

import VarAndConstDec.javaValue.PrimitiveType;

import java.builder.ErrorRepository;
import java.execution.ExecutionManager;
import java.execution.commands.controlled.IAttemptCommand;
import java.representations.javaValue.PrimitiveType;
import java.semantics.statements.StatementControlOverseer;

public class javaArray {
    private final static String TAG = "javaArray";

    private javaValue[] javaValueArray;
    private PrimitiveType arrayPrimitiveType;
    private String arrayIdentifier;
    private boolean finalFlag = false;

    public javaArray(PrimitiveType primitiveType, String identifier) {
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
        try {
            this.javaValueArray = new javaValue[size];
        } catch (NegativeArraySizeException ex) {
            this.javaValueArray = null;

            //StatementControlOverseer.getInstance().setCurrentCatchClause(IAttemptCommand.CatchTypeEnum.NEGATIVE_ARRAY_SIZE);
            ExecutionManager.getInstance().setCurrentCatchType(IAttemptCommand.CatchTypeEnum.NEGATIVE_ARRAY_SIZE);
        }
        //System.out.println(TAG + ": Mobi array initialized to size " +this.javaValueArray.length);
    }

    public int getSize() {
        return this.javaValueArray.length;
    }

    public void updateValueAt(javaValue mobiValue, int index) {
        if(index >= this.javaValueArray.length || index <= -1) {
            //System.out.println("ERROR: " + String.format(ErrorRepository.getErrorMessage(ErrorRepository.RUNTIME_ARRAY_OUT_OF_BOUNDS), this.arrayIdentifier));
            ExecutionManager.getInstance().setCurrentCatchType(IAttemptCommand.CatchTypeEnum.ARRAY_OUT_OF_BOUNDS);

            return;
        }
        this.javaValueArray[index] = mobiValue;
    }

    public javaValue getValueAt(int index) {
        if(index >= this.javaValueArray.length || index <= -1) {
//            System.out.println("ERROR: " + String.format(ErrorRepository.getErrorMessage(ErrorRepository.RUNTIME_ARRAY_OUT_OF_BOUNDS), this.arrayIdentifier));
            ExecutionManager.getInstance().setCurrentCatchType(IAttemptCommand.CatchTypeEnum.ARRAY_OUT_OF_BOUNDS);

            return null;
        }
        else {
            return this.javaValueArray[index];
        }
    }

    /*
     * Utility function that returns an arary of specified primitive type.
     */
    public static javaArray createArray(String primitiveTypeString, String arrayIdentifier) {
        //identify primitive type
        PrimitiveType primitiveType = PrimitiveType.NOT_YET_IDENTIFIED;

        if(javaKeywords.matchesKeyword(javaKeywords.PRIMITIVE_TYPE_BOOLEAN, primitiveTypeString))
            primitiveType = PrimitiveType.BOOL;
        else if(javaKeywords.matchesKeyword(javaKeywords.PRIMITIVE_TYPE_CHAR, primitiveTypeString)) {
            primitiveType = PrimitiveType.CHAR;
        }
        else if(javaKeywords.matchesKeyword(javaKeywords.PRIMITIVE_TYPE_FLOAT, primitiveTypeString)) {
            primitiveType = PrimitiveType.FLOAT;
        }
        else if(javaKeywords.matchesKeyword(javaKeywords.PRIMITIVE_TYPE_INT, primitiveTypeString)) {
            primitiveType = PrimitiveType.INT;
        }
        else if(javaKeywords.matchesKeyword(javaKeywords.PRIMITIVE_TYPE_STRING, primitiveTypeString)) {
            primitiveType = PrimitiveType.STRING;
        }

        javaArray mobiArray = new javaArray(primitiveType, arrayIdentifier);

        return mobiArray;
    }
}
