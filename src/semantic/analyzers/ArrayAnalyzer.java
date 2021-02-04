package semantic.analyzers;

import Execution.command.ICondCommand;
import Execution.command.ICtrlCommand;
import antlr.Java8Parser;
import antlr.Java8Parser.DimExprContext;
import antlr.Java8Parser.PrimitiveTypeContext;
import antlr.Java8Parser.VariableDeclaratorIdContext;
import error.checkers.MultipleVarDecChecker;
import Execution.ExecutionManager;
import Execution.command.evaluation.ArrayInitCom;
import semantic.representation.JavaArray;
import semantic.representation.JavaValue;
import semantic.representation.JavaValue.PrimitiveType;
import semantic.statements.StatementControlOverseer;
import semantic.symboltable.scope.ClassScope;
import semantic.symboltable.scope.LocalScope;
import semantic.utils.IdentifiedTokens;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Analyzes a given array declaration. Only accepts primitive declaration
 *
 */
public class ArrayAnalyzer implements ParseTreeListener {
	
	private final static String ARRAY_PRIMITIVE_KEY = "ARRAY_PRIMITIVE_KEY";
	private final static String ARRAY_IDENTIFIER_KEY = "ARRAY_IDENTIFIER_KEY";
	
	private IdentifiedTokens identifiedTokens;
	private ClassScope declaredClassScope;
	private LocalScope localScope;
	private JavaArray declaredArray;
	
	public ArrayAnalyzer( IdentifiedTokens identifiedTokens, ClassScope declaredClassScope) {
		this.identifiedTokens = identifiedTokens;
		this.declaredClassScope = declaredClassScope;
	}
	
	public ArrayAnalyzer( IdentifiedTokens identifiedTokens, LocalScope localScope) {
		this.identifiedTokens = identifiedTokens;
		this.localScope = localScope;
	}
	
	public void analyze(ParserRuleContext ctx) {
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, ctx);
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		if(ctx instanceof PrimitiveTypeContext) {
			PrimitiveTypeContext primitiveCtx = (PrimitiveTypeContext) ctx;
			this.identifiedTokens.addToken(ARRAY_PRIMITIVE_KEY, primitiveCtx.getText());
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Array created name: " +primitiveCtx.getText()));
		}
		else if(ctx instanceof VariableDeclaratorIdContext) {
			VariableDeclaratorIdContext varDecIdCtx = (VariableDeclaratorIdContext) ctx;
			MultipleVarDecChecker multipleVarDecChecker = new MultipleVarDecChecker(varDecIdCtx);
			multipleVarDecChecker.verify();
			this.identifiedTokens.addToken(ARRAY_IDENTIFIER_KEY, varDecIdCtx.getText());
			this.analyzeArray();
		}
		else if(ctx instanceof DimExprContext) {
			DimExprContext arrayCreatorCtx = (DimExprContext) ctx;
			this.createInitializeCommand(arrayCreatorCtx);
		}
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
	
	}
	
	private void analyzeArray() {
		
		if(this.declaredClassScope != null) {
			if(this.identifiedTokens.containsTokens(ClassAnalyzer.ACCESS_CONTROL_KEY, ARRAY_PRIMITIVE_KEY, ARRAY_IDENTIFIER_KEY)) {
				String accessControlString = this.identifiedTokens.getToken(ClassAnalyzer.ACCESS_CONTROL_KEY);
				String arrayTypeString = this.identifiedTokens.getToken(ARRAY_PRIMITIVE_KEY);
				String arrayIdentifierString = this.identifiedTokens.getToken(ARRAY_IDENTIFIER_KEY);
				
				//initialize an array javaValue
				this.declaredArray = JavaArray.createArray(arrayTypeString, arrayIdentifierString);
				JavaValue javaValue = new JavaValue(this.declaredArray, PrimitiveType.ARRAY);
				
				this.declaredClassScope.addJavaValue(accessControlString, arrayIdentifierString, javaValue);
				ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Creating array with type " +
						arrayTypeString+ " variable " +arrayIdentifierString));
				
				this.identifiedTokens.clearTokens();
			}
		}
		else if(this.localScope != null) {
			if(this.identifiedTokens.containsTokens(ARRAY_PRIMITIVE_KEY, ARRAY_IDENTIFIER_KEY)) {
				String arrayTypeString = this.identifiedTokens.getToken(ARRAY_PRIMITIVE_KEY);
				String arrayIdentifierString = this.identifiedTokens.getToken(ARRAY_IDENTIFIER_KEY);
				
				//initialize an array javaValue
				this.declaredArray = JavaArray.createArray(arrayTypeString, arrayIdentifierString);
				JavaValue javaValue = new JavaValue(this.declaredArray, PrimitiveType.ARRAY);
				
				localScope.addJavaValue(arrayIdentifierString, javaValue);
				ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Creating array with type " +
						arrayTypeString+ " variable " +arrayIdentifierString));
				
				this.identifiedTokens.clearTokens();
			}
		}
		
	}
	
	private void createInitializeCommand(DimExprContext arrayCreatorCtx) {
		ArrayInitCom arrayInitializeCommand = new ArrayInitCom(this.declaredArray, arrayCreatorCtx);
		StatementControlOverseer statementControl = StatementControlOverseer.getInstance();

		//add to conditional controlled command
		if(statementControl.isInConditionalCommand()) {
			ICondCommand conditionalCommand = (ICondCommand) statementControl.getActiveControlledCommand();

			if(statementControl.isInPositiveRule()) {
				conditionalCommand.addPositiveCommand(arrayInitializeCommand);
			}
			else {
				conditionalCommand.addNegativeCommand(arrayInitializeCommand);
			}
		}

		else if(statementControl.isInControlledCommand()) {
			ICtrlCommand controlledCommand = (ICtrlCommand) statementControl.getActiveControlledCommand();
			controlledCommand.addCommand(arrayInitializeCommand);
		}
		else {
			ExecutionManager.getExecutionManager().addCommand(arrayInitializeCommand);
		}
	}
}
