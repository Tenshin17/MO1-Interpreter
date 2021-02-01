package semantic.representation;

import antlr.Java8Parser.ExpressionContext;
import error.checkers.TypeChecker;
import Execution.ExecutionManager;
import Execution.ExecutionMonitor;
import Execution.FunctionTracker;
import Execution.command.ICommand;
import Execution.command.ICtrlCommand;
import semantic.representation.JavaValue.PrimitiveType;
import semantic.symboltable.scope.ClassScope;
import semantic.symboltable.scope.LocalScope;
import semantic.utils.RecognizedKeywords;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Represents the intermediate representation of a function
 *
 */
public class JavaMethod implements ICtrlCommand{
	
	public enum FunctionType {
		INT_TYPE,
		BOOLEAN_TYPE,
		BYTE_TYPE,
		CHAR_TYPE,
		DOUBLE_TYPE,
		FLOAT_TYPE,
		LONG_TYPE,
		SHORT_TYPE,
		STRING_TYPE,
		VOID_TYPE,
	}
	
	private String functionName;
	private List<ICommand> commandSequences; //the list of commands execution by the function
	
	private LocalScope parentLocalScope; //refers to the parent local scope of this function.
	
	private LinkedHashMap<String, ClassScope> parameterReferences; //the list of parameters accepted that follows the 'call-by-reference' standard.
	private LinkedHashMap<String, JavaValue> parameterValues;	//the list of parameters accepted that follows the 'call-by-value' standard.
	private JavaValue returnValue; //the return value of the function. null if it's a void type
	private FunctionType returnType = FunctionType.VOID_TYPE; //the return type of the function
	
	public JavaMethod() {
		commandSequences = new ArrayList<>();
		parameterValues = new LinkedHashMap<>();
		parameterReferences = new LinkedHashMap<>();
	}

	// for the recursion stuff
	public JavaMethod(JavaMethod javaMethod) {
	    this.functionName = javaMethod.functionName;
	    this.commandSequences = new ArrayList<>(javaMethod.commandSequences);
	    this.parentLocalScope = javaMethod.parentLocalScope;
	    this.parameterReferences = new LinkedHashMap<>(javaMethod.parameterReferences);
	    this.parameterValues = new LinkedHashMap<>(javaMethod.parameterValues);
	    this.returnValue = javaMethod.returnValue;
	    this.returnType = javaMethod.returnType;
    }
	
	public void setParentLocalScope(LocalScope localScope) {
		parentLocalScope = localScope;
	}
	
	public LocalScope getParentLocalScope() {
		return parentLocalScope;
	}
	
	public void setReturnType(FunctionType functionType) {
		returnType = functionType;
		
		//create an empty java value as a return value
		switch(returnType) {
			case BOOLEAN_TYPE: returnValue = new JavaValue(true, PrimitiveType.BOOLEAN); break;
			case BYTE_TYPE: returnValue = new JavaValue(0, PrimitiveType.BYTE); break;
			case CHAR_TYPE: returnValue = new JavaValue(' ', PrimitiveType.CHAR); break;
			case INT_TYPE: returnValue = new JavaValue(0, PrimitiveType.INT); break;
			case DOUBLE_TYPE: returnValue = new JavaValue(0, PrimitiveType.DOUBLE); break;
			case FLOAT_TYPE: returnValue = new JavaValue(0, PrimitiveType.FLOAT); break;
			case LONG_TYPE: returnValue = new JavaValue(0, PrimitiveType.LONG); break;
			case SHORT_TYPE: returnValue = new JavaValue(0, PrimitiveType.SHORT); break;
			case STRING_TYPE: returnValue = new JavaValue("", PrimitiveType.STRING); break;
			default: break;
		}
	}
	
	public FunctionType getReturnType() {
		return returnType;
	}
	
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
	public String getFunctionName() {
		return functionName;
	}
	
	/*
	 * Maps parameters by values, which means that the value is copied to its parameter listing
	 */
	public void mapParameterByValue(String... values) {
		for(int i = 0; i < values.length; i++) {
			JavaValue javaValue = getParameterAt(i);
			javaValue.setValue(values[i]);
		}
	}
	
	public void mapParameterByValueAt(String value, int index) {
		if(index >= parameterValues.size()) {
			return;
		}
		
		JavaValue javaValue = getParameterAt(index);
		javaValue.setValue(value);
	}
	
	public void mapArrayAt(JavaValue javaValue, int index, String identifier) {
		if(index >= parameterValues.size()) {
			return;
		}
		
		JavaArray javaArray = (JavaArray) javaValue.getValue();
		
		JavaArray newArray = new JavaArray(javaArray.getPrimitiveType(), identifier);
		JavaValue newValue = new JavaValue(newArray, PrimitiveType.ARRAY);
		
		newArray.initializeSize(javaArray.getSize());
		
		for(int i = 0; i < newArray.getSize(); i++) {
			newArray.updateValueAt(javaArray.getValueAt(i), i);
		}
		
		parameterValues.put(getParameterKeyAt(index), newValue);
		
	}
	
	public int getParameterValueSize() {
		return parameterValues.size();
	}
	
	public void verifyParameterByValueAt(ExpressionContext exprCtx, int index) {
		if(index >= parameterValues.size()) {
			return;
		}
		
		JavaValue javaValue = getParameterAt(index);
		TypeChecker typeChecker = new TypeChecker(javaValue, exprCtx);
		typeChecker.verify();
	}
	
	/*
	 * Maps parameters by reference, in this case, accept a class scope.
	 */
	public void mapParameterByReference(ClassScope... classScopes) {
		System.err.println("Mapping of parameter by reference not yet supported.");
	}
	
	public void addParameter(String identifierString, JavaValue javaValue) {
		parameterValues.put(identifierString, javaValue);
		//txtWriter.writeMessage(StringUtils.formatDebug(functionName + " added an empty parameter " +
				//identifierString+ " type " +javaValue.getPrimitiveType()));
	}
	
	public boolean hasParameter(String identifierString) {
		return parameterValues.containsKey(identifierString);
	}
	public JavaValue getParameter(String identifierString) {
		if(this.hasParameter(identifierString)) {
			return parameterValues.get(identifierString);
		}
		else {
			System.err.println("JavaMethod: " + identifierString + " not found in parameter list");
			return null;
		}
	}
	
	public JavaValue getParameterAt(int index) {
		int i = 0;

		for(JavaValue javaValue : parameterValues.values()) {
			if(i == index) {
				return javaValue;
			}
			
			i++;
		}

		System.err.println("JavaMethod: " + index + " has exceeded parameter list.");
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

		System.err.println("JavaMethod: " + index + " has exceeded parameter list.");
		return null;
	}
	
	public JavaValue getReturnValue() {
		if(this.returnType == FunctionType.VOID_TYPE) {
			//txtWriter.writeMessage(StringUtils.formatDebug(functionName + " is a void function. Null java value is returned"));
			return null;
		}
		else {
			return this.returnValue;
		}
	}
	
	@Override
	public void addCommand(ICommand command) {
		this.commandSequences.add(command);
		//Console.log("Execution.command added to " +this.functionName);
	}
	
	@Override
	public void execute() {
		ExecutionMonitor executionMonitor = ExecutionManager.getExecutionManager().getExecutionMonitor();
		FunctionTracker.getInstance().reportEnterFunction(this);
		try {
			for(ICommand command : this.commandSequences) {
				executionMonitor.tryExecution();
				command.execute();
			}

		} catch(InterruptedException e) {
			System.err.println("JavaMethod: " + "Monitor block interrupted! " +e.getMessage());
		}
		
		FunctionTracker.getInstance().reportExitFunction();
	}

	@Override
	public ControlTypeEnum getControlType() {
		return ControlTypeEnum.FUNCTION_TYPE;
	}

	public static FunctionType identifyFunctionType(String primitiveTypeString) {
		
		if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_BOOLEAN, primitiveTypeString)) {
			return FunctionType.BOOLEAN_TYPE;
		}
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_BYTE, primitiveTypeString)) {
			return FunctionType.BYTE_TYPE;
		}
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_CHAR, primitiveTypeString)) {
			return FunctionType.CHAR_TYPE;
		}
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_DOUBLE, primitiveTypeString)) {
			return FunctionType.DOUBLE_TYPE;
		}
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_FLOAT, primitiveTypeString)) {
			return FunctionType.FLOAT_TYPE;
		}
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_INT, primitiveTypeString)) {
			return FunctionType.INT_TYPE;
		}
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_LONG, primitiveTypeString)) {
			return FunctionType.LONG_TYPE;
		}
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_SHORT, primitiveTypeString)) {
			return FunctionType.SHORT_TYPE;
		}
		else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.PRIMITIVE_TYPE_STRING, primitiveTypeString)) {
			return FunctionType.STRING_TYPE;
		}
		
		return FunctionType.VOID_TYPE;
	}
}
