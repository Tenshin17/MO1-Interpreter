//package VarAndConstDec;
package VarAndConstDec;

import java.builder.errorcheckers.TypeChecker;
import java.execution.ExecutionManager;
import java.execution.ExecutionMonitor;
import java.execution.MethodTracker;
import Command.ICommand;
import java.execution.commands.controlled.ForCommand;
import java.execution.commands.controlled.IControlledCommand;
import java.execution.commands.controlled.IfCommand;
import java.execution.commands.evaluation.AssignmentCommand;
import java.execution.commands.evaluation.MappingCommand;
import java.execution.commands.simple.IncDecCommand;
import java.execution.commands.simple.ReturnCommand;
import VarAndConstDec.javaValue.PrimitiveType;
import java.semantics.searching.VariableSearcher;
import java.semantics.symboltable.scopes.ClassScope;
import java.semantics.symboltable.scopes.LocalScope;
import java.antlr.parser.javaParser.ExpressionContext;
import java.semantics.utils.LocalVarTracker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

public class javaMethod implements IControlledCommand{

    private final static String TAG = "javaMethod";

    public enum MethodType {
        BOOL_TYPE,
        INT_TYPE,
        FLOAT_TYPE,
        STRING_TYPE,
        CHAR_TYPE,
        VOID_TYPE
    }

    private String methodName;
    private List<ICommand> commandSequences; //the list of commands execution by the function

    private LocalScope parentLocalScope; //refers to the parent local scope of this function.

    private LinkedHashMap<String, ClassScope> parameterReferences; //the list of parameters accepted that follows the 'call-by-reference' standard.
    private LinkedHashMap<String, javaValue> parameterValues;	//the list of parameters accepted that follows the 'call-by-value' standard.
    private javaValue returnValue; //the return value of the function. null if it's a void type
    private MethodType returnType = MethodType.VOID_TYPE; //the return type of the function

    private boolean hasValidReturns = true;

    public javaMethod() {
        this.commandSequences = new ArrayList<ICommand>();
        this.parameterValues = new LinkedHashMap<String, javaValue>();
        this.parameterReferences = new LinkedHashMap<String, ClassScope>();
    }

    public void setParentLocalScope(LocalScope localScope) {
        this.parentLocalScope = localScope;
    }

    public LocalScope getParentLocalScope() {
        return this.parentLocalScope;
    }

    public void setReturnType(MethodType methodType) {
        this.returnType = methodType;

        //create an empty mobi value as a return value
        switch(this.returnType) {
            case BOOL_TYPE: this.returnValue = new javaValue(true, PrimitiveType.BOOL); setValidReturns(false); break;
            case INT_TYPE: this.returnValue = new javaValue(0, PrimitiveType.INT); setValidReturns(false); break;
            case FLOAT_TYPE: this.returnValue = new javaValue(0.0, PrimitiveType.FLOAT); setValidReturns(false); break;
            case STRING_TYPE: this.returnValue = new javaValue("", PrimitiveType.STRING); setValidReturns(false); break;
            case CHAR_TYPE: this.returnValue = new javaValue(0, PrimitiveType.CHAR); setValidReturns(false); break;
            default:
                break;
        }
    }

    public boolean hasValidReturns(){
        return this.hasValidReturns;
    }

    public void setValidReturns(boolean b) {
        hasValidReturns = b;
    }

    public MethodType getReturnType() {
        return this.returnType;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    /*
     * Maps parameters by values, which means that the value is copied to its parameter listing
     */
    public void mapParameterByValue(String... values) {
        for(int i = 0; i < values.length; i++) {
            javaValue javaValue = this.getParameterAt(i);
            javaValue.setValue(values[i]);
        }
    }

    public void mapParameterByValueAt(String value, int index) {
        if(index >= this.parameterValues.size()) {
            return;
        }

        javaValue javaValue = this.getParameterAt(index);
        javaValue.setValue(value);
    }

    public void mapArrayAt(javaValue javaValue, int index, String identifier) {
        if(index >= this.parameterValues.size()) {
            return;
        }

        /*javaArray javaArray = (javaArray) javaValue.getValue();

        javaArray newArray = new javaArray(javaArray.getPrimitiveType(), identifier);
        javaValue newValue = new javaValue(newArray, PrimitiveType.ARRAY);

        newArray.initializeSize(javaArray.getSize());

        for(int i = 0; i < newArray.getSize(); i++) {
            newArray.updateValueAt(javaArray.getValueAt(i), i);
        }*/

        this.parameterValues.put(this.getParameterKeyAt(index), javaValue);

    }

    public int getParameterValueSize() {
        return this.parameterValues.size();
    }

    public void verifyParameterByValueAt(ExpressionContext exprCtx, int index) {
        if(index >= this.parameterValues.size()) {
            return;
        }

        javaValue javaValue = this.getParameterAt(index);
        TypeChecker typeChecker = new TypeChecker(javaValue, exprCtx);
        typeChecker.verify();
    }

    /*
     * Maps parameters by reference, in this case, accept a class scope.
     */
    public void mapParameterByReference(ClassScope... classScopes) {
        //Log.e(TAG, "Mapping of parameter by reference not yet supported.");
    }

    public void addParameter(String identifierString, javaValue javaValue) {
        this.parameterValues.put(identifierString, javaValue);
        System.out.println(this.methodName + " added an empty parameter " +identifierString+ " type " + javaValue.getPrimitiveType());
    }

    public boolean hasParameter(String identifierString) {
        return this.parameterValues.containsKey(identifierString);
    }
    public javaValue getParameter(String identifierString) {
        if(this.hasParameter(identifierString)) {
            return this.parameterValues.get(identifierString);
        }
        else {
            System.out.println(TAG + ": " + identifierString + " not found in parameter list");
            return null;
        }
    }

    public javaValue getParameterAt(int index) {
        int i = 0;

        for(javaValue mobiValue : this.parameterValues.values()) {
            if(i == index) {
                return mobiValue;
            }

            i++;
        }

        System.out.println(TAG + ": " + index + " has exceeded parameter list.");
        return null;
    }

    private String getParameterKeyAt(int index) {
        int i = 0;

        for(String key : this.parameterValues.keySet()) {
            if(i == index) {
                return key;
            }

            i++;
        }

        System.out.println(TAG + ": " + index + " has exceeded parameter list.");
        return null;
    }

    public javaValue getReturnValue() {
        if(this.returnType == MethodType.VOID_TYPE) {
            System.out.println(this.methodName + " is a void function. Null mobi value is returned");
            return null;
        }
        else {
            return this.returnValue;
        }
    }

    @Override
    public void addCommand(ICommand command) {
        this.commandSequences.add(command);
        //Console.log("Command added to " +this.functionName);
    }

    @Override
    public void execute() {
        ExecutionMonitor executionMonitor = ExecutionManager.getInstance().getExecutionMonitor();
        MethodTracker.getInstance().reportEnterFunction(this);

        LocalVarTracker.getInstance().startNewSession();

        try {
            for(ICommand command : this.commandSequences) {
                executionMonitor.tryExecution();
                command.execute();

                LocalVarTracker.getInstance().populateLocalVars(command);

                if (command instanceof ReturnCommand) {
                    break;
                } else if (command instanceof IfCommand) {
                    if (((IfCommand) command).isReturned()) {
                        ((IfCommand) command).resetReturnFlag();
                        break;
                    }
                }

                if (ExecutionManager.getInstance().isAborted())
                    break;
            }
        } catch(InterruptedException e) {
            System.out.println(TAG + ": " + "Monitor block interrupted! " +e.getMessage());
        }

        MethodTracker.getInstance().reportExitFunction();
        this.popBackParameters();
        this.popBackLocalVars();

        LocalVarTracker.getInstance().endCurrentSession();

        //LocalVarTracker.resetLocalVars(localVars);
    }

    private void popBackParameters() {
        for (javaValue bV : this.parameterValues.values()) {
            if(bV.getPrimitiveType() != PrimitiveType.ARRAY)
                bV.popBack();
        }
    }

    private void popBackLocalVars() {
        for(String s : LocalVarTracker.getInstance().getCurrentSession()) {

            javaValue value = VariableSearcher.searchVariableInFunction(this, s);

            if (value != null) {

                if (value.stackSize() > 1) { // prevent from reaching null
                    if (value.getPrimitiveType() != PrimitiveType.ARRAY)
                        value.popBack();
                }

            }

        }
    }

    @Override
    public ControlTypeEnum getControlType() {
        return ControlTypeEnum.FUNCTION_TYPE;
    }

    public static MethodType identifyFunctionType(String primitiveTypeString) {

        if(javaKeywords.matchesKeyword(javaKeywords.PRIMITIVE_TYPE_BOOLEAN, primitiveTypeString)) {
            return MethodType.BOOL_TYPE;
        }
        else if(javaKeywords.matchesKeyword(javaKeywords.PRIMITIVE_TYPE_CHAR, primitiveTypeString)) {
            return MethodType.CHAR_TYPE;
        }
        else if(javaKeywords.matchesKeyword(javaKeywords.PRIMITIVE_TYPE_FLOAT, primitiveTypeString)) {
            return MethodType.FLOAT_TYPE;
        }
        else if(javaKeywords.matchesKeyword(javaKeywords.PRIMITIVE_TYPE_INT, primitiveTypeString)) {
            return MethodType.INT_TYPE;
        }
        else if(javaKeywords.matchesKeyword(javaKeywords.PRIMITIVE_TYPE_STRING, primitiveTypeString)) {
            return MethodType.STRING_TYPE;
        }

        return MethodType.VOID_TYPE;
    }
}
