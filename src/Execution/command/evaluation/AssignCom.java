package Execution.command.evaluation;

import antlr.Java8Lexer;
import antlr.Java8Parser.*;
import antlr.Java8Parser.ExpressionContext;
import error.CustomErrorStrategy;
import error.checkers.ConstChecker;
import error.checkers.TypeChecker;
import error.checkers.UndeclaredChecker;
import Execution.ExecutionManager;
import Execution.command.ICommand;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import semantic.analyzers.FunctionCallVerifier;
import semantic.representation.JavaArray;
import semantic.representation.JavaValue;
import semantic.representation.JavaValueSearch;
import semantic.searching.VariableSearcher;
import semantic.utils.AssignmentUtils;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import semantic.utils.Expression;
import semantic.utils.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * A new assignment command that walks a given expression and replaces values to it
 * before being passed to Eval-Ex library.
 *
 */
public class AssignCom implements ICommand, ParseTreeListener {

	private LeftHandSideContext leftHandExprCtx;
	private ExpressionContext rightHandExprCtx;
	private String modifiedExp = "";

	public AssignCom(LeftHandSideContext leftHandExprCtx,
					 ExpressionContext rightHandExprCtx) {
		this.leftHandExprCtx = leftHandExprCtx;
		this.rightHandExprCtx = rightHandExprCtx;

		//System.out.println(this.leftHandExprCtx.getText()+" "+this.rightHandExprCtx.getText());
		UndeclaredChecker undeclaredChecker = new UndeclaredChecker(this.leftHandExprCtx);
		undeclaredChecker.verify();

		ConstChecker constChecker = new ConstChecker(this.leftHandExprCtx);
		constChecker.verify();

		undeclaredChecker = new UndeclaredChecker(this.rightHandExprCtx);
		undeclaredChecker.verify();

		//this.modifiedExp = this.rightHandExprCtx.getText();

		ParseTreeWalker functionWalker = new ParseTreeWalker();
		functionWalker.walk(new FunctionCallVerifier(), this.rightHandExprCtx);

		//System.out.println(this.modifiedExp+" expr");
		
		//type check the javaValue
		JavaValue javaValue;
		if(ExecutionManager.getExecutionManager().isInFunctionExecution()) {
			javaValue = VariableSearcher.searchVariableInFunction(ExecutionManager.getExecutionManager().getCurrentFunction(), this.leftHandExprCtx.getText());
		}
		else {
			javaValue = VariableSearcher.searchVariable(this.leftHandExprCtx.getText());
		}

		TypeChecker typeChecker = new TypeChecker(javaValue, this.rightHandExprCtx);
		typeChecker.verify();
	}

	@Override
	public void execute() {
		//System.out.println(this.rightHandExprCtx);
		EvaluationCommand evaluationCommand = new EvaluationCommand(this.rightHandExprCtx);
		evaluationCommand.execute();

		if(this.isLeftHandArrayAccessor()) {
			System.out.println("Im an Array");
			this.handleArrayAssignment(evaluationCommand.getResult().toEngineeringString());
		}
		else {
			JavaValue javaValue = VariableSearcher.searchVariable(this.leftHandExprCtx.getText());
			AssignmentUtils.assignAppropriateValue(javaValue, evaluationCommand.getResult());
		}
	}

	private boolean isLeftHandArrayAccessor() {
		List<TerminalNode> lBrackTokens = this.leftHandExprCtx.getTokens(Java8Lexer.LBRACK);
		List<TerminalNode> rBrackTokens = this.leftHandExprCtx.getTokens(Java8Lexer.RBRACK);
		if(this.leftHandExprCtx.arrayAccess() != null) {
			lBrackTokens = this.leftHandExprCtx.arrayAccess().getTokens(Java8Lexer.LBRACK);
			rBrackTokens = this.leftHandExprCtx.arrayAccess().getTokens(Java8Lexer.RBRACK);
		}
		else {
			return false;
		}

		return(lBrackTokens.size() > 0 && rBrackTokens.size() > 0);
	}

	private void handleArrayAssignment(String resultString) {
		TerminalNode identifierNode = this.leftHandExprCtx.arrayAccess().expressionName().Identifier();
		ArrayAccessContext arrayIndexExprCtx = this.leftHandExprCtx.arrayAccess();

		System.out.println(identifierNode.getText()+"HEEEEERRREEEEE"+resultString);
		JavaValue javaValue = VariableSearcher.searchVariable(identifierNode.getText());
		JavaArray javaArray = (JavaArray) javaValue.getValue();

		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, arrayIndexExprCtx);

		//Expression evalEx = new Expression(arrayIndexExprCtx.getText());
		//resultValue = evalEx.eval();
		EvaluationCommand evaluationCommand = new EvaluationCommand(arrayIndexExprCtx.expression(0));
		evaluationCommand.execute();

		//create a new array value to replace value at specified index
		JavaValue newArrayValue = new JavaValue(null, javaArray.getPrimitiveType());
		newArrayValue.setValue(resultString);
		System.out.println(resultString+" ffff "+evaluationCommand.getResult().intValue());
		javaArray.updateValueAt(newArrayValue, evaluationCommand.getResult().intValue());

		//Console.log("Index to access: " +evaluationCommand.getResult().intValue()+ " Updated with: " +resultString);
	}
/*
	private void handleArrayRetrieval(String resultString) {
		String arrayIdentifier =  this.rightHandExprCtx.getText();
		String[] arrayParts = arrayIdentifier.split("\\[");
		arrayParts[1] = arrayParts[1].replace("]","");
		JavaValue javaValue = VariableSearcher.searchVariable(arrayParts[0]);
		JavaArray javaArray = (JavaArray) javaValue.getValue();

		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, this.rightHandExprCtx);

		//Expression evalEx = new Expression(arrayIndexExprCtx.getText());
		//resultValue = evalEx.eval();
		//EvaluationCommand evaluationCommand = new EvaluationCommand(this.rightHandExprCtx.expression(0));
		//evaluationCommand.execute();

		//create a new array value to replace value at specified index
		JavaValue newArrayValue = new JavaValue(null, javaArray.getPrimitiveType());
		newArrayValue.setValue(resultString);
		javaArray.updateValueAt(newArrayValue, evaluationCommand.getResult().intValue());
		AssignmentUtils.assignAppropriateValue(javaValue, javaValue);
	}
*/
	@Override
	public void visitTerminal(TerminalNode terminalNode) {

	}

	@Override
	public void visitErrorNode(ErrorNode errorNode) {

	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		if (ctx instanceof VariableDeclaratorIdContext) {
			VariableDeclaratorIdContext exprCtx = (VariableDeclaratorIdContext) ctx;
			if (EvaluationCommand.isVariableOrConst(exprCtx)) {
				evaluateVariable(exprCtx);
			}
		}
		else if (ctx instanceof ExpressionNameContext) {
			ExpressionNameContext exprCtx = (ExpressionNameContext) ctx;
			if(EvaluationCommand.isVariableOrConst(exprCtx)) {
				evaluateVariable(exprCtx);
			}
		}
	}

	@Override
	public void exitEveryRule(ParserRuleContext parserRuleContext) {

	}

	private void evaluateVariable(VariableDeclaratorIdContext exprCtx) {
		JavaValue javaValue = VariableSearcher
				.searchVariable(exprCtx.getText());

		this.modifiedExp = this.modifiedExp.replaceFirst(exprCtx.getText(),
				javaValue.getValue().toString());
	}

	private void evaluateVariable(ExpressionNameContext exprCtx) {
		JavaValue javaValue = VariableSearcher
				.searchVariable(exprCtx.getText());

		this.modifiedExp = this.modifiedExp.replaceFirst(exprCtx.getText(),
				javaValue.getValue().toString());
	}
}