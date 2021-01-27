//package VarAndConstDec;
package baraco.representations;

import baraco.builder.ErrorRepository;
import baraco.execution.ExecutionManager;
import baraco.execution.commands.controlled.IAttemptCommand;
import baraco.representations.BaracoValue.PrimitiveType;
import baraco.semantics.statements.StatementControlOverseer;

public class javaArray {
    private final static String TAG = "BaracoArray";

    private BaracoValue[] baracoValueArray;
    private PrimitiveType arrayPrimitiveType;
    private String arrayIdentifier;
    private boolean finalFlag = false;

    public BaracoArray(PrimitiveType primitiveType, String identifier) {
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
            this.baracoValueArray = new BaracoValue[size];
        } catch (NegativeArraySizeException ex) {
            this.baracoValueArray = null;

            //StatementControlOverseer.getInstance().setCurrentCatchClause(IAttemptCommand.CatchTypeEnum.NEGATIVE_ARRAY_SIZE);
            ExecutionManager.getInstance().setCurrentCatchType(IAttemptCommand.CatchTypeEnum.NEGATIVE_ARRAY_SIZE);
        }
        //System.out.println(TAG + ": Mobi array initialized to size " +this.baracoValueArray.length);
    }

    public int getSize() {
        return this.baracoValueArray.length;
    }

    public void updateValueAt(BaracoValue mobiValue, int index) {
        if(index >= this.baracoValueArray.length || index <= -1) {
            //System.out.println("ERROR: " + String.format(ErrorRepository.getErrorMessage(ErrorRepository.RUNTIME_ARRAY_OUT_OF_BOUNDS), this.arrayIdentifier));
            ExecutionManager.getInstance().setCurrentCatchType(IAttemptCommand.CatchTypeEnum.ARRAY_OUT_OF_BOUNDS);

            return;
        }
        this.baracoValueArray[index] = mobiValue;
    }

    public BaracoValue getValueAt(int index) {
        if(index >= this.baracoValueArray.length || index <= -1) {
//            System.out.println("ERROR: " + String.format(ErrorRepository.getErrorMessage(ErrorRepository.RUNTIME_ARRAY_OUT_OF_BOUNDS), this.arrayIdentifier));
            ExecutionManager.getInstance().setCurrentCatchType(IAttemptCommand.CatchTypeEnum.ARRAY_OUT_OF_BOUNDS);

            return null;
        }
        else {
            return this.baracoValueArray[index];
        }
    }

    /*
     * Utility function that returns an arary of specified primitive type.
     */
    public static BaracoArray createArray(String primitiveTypeString, String arrayIdentifier) {
        //identify primitive type
        PrimitiveType primitiveType = PrimitiveType.NOT_YET_IDENTIFIED;

        if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_BOOLEAN, primitiveTypeString))
            primitiveType = PrimitiveType.BOOL;
        else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_CHAR, primitiveTypeString)) {
            primitiveType = PrimitiveType.CHAR;
        }
        else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_DECIMAL, primitiveTypeString)) {
            primitiveType = PrimitiveType.DECIMAL;
        }
        else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_INT, primitiveTypeString)) {
            primitiveType = PrimitiveType.INT;
        }
        else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_STRING, primitiveTypeString)) {
            primitiveType = PrimitiveType.STRING;
        }

        BaracoArray mobiArray = new BaracoArray(primitiveType, arrayIdentifier);

        return mobiArray;
    }
}
