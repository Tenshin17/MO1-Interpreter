package Execution.command.evaluation;

import antlr.Java8Lexer;
import antlr.Java8Parser.*;
import antlr.Java8Parser.ExpressionContext;
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
import semantic.searching.VariableSearcher;
import semantic.utils.AssignmentUtils;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import semantic.utils.Expression;

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

		UndeclaredChecker undeclaredChecker = new UndeclaredChecker(this.leftHandExprCtx);
		undeclaredChecker.verify();

		ConstChecker constChecker = new ConstChecker(this.leftHandExprCtx);
		constChecker.verify();

		undeclaredChecker = new UndeclaredChecker(this.rightHandExprCtx);
		undeclaredChecker.verify();

		ParseTreeWalker functionWalker = new ParseTreeWalker();
		functionWalker.walk(new FunctionCallVerifier(), this.rightHandExprCtx);

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
		EvaluationCommand evaluationCommand = new EvaluationCommand(rightHandExprCtx);
		evaluationCommand.execute();

		if(isLeftHandArrayAccessor()) {
			handleArrayAssignment(evaluationCommand.getResult().toEngineeringString());
		}
		else {
			JavaValue javaValue = VariableSearcher.searchVariable(leftHandExprCtx.getText());
			AssignmentUtils.assignAppropriateValue(javaValue, evaluationCommand.getResult());
		}
	}

	private boolean isLeftHandArrayAccessor() {
		List<TerminalNode> lBrackTokens = leftHandExprCtx.getTokens(Java8Lexer.LBRACK);
		List<TerminalNode> rBrackTokens = leftHandExprCtx.getTokens(Java8Lexer.RBRACK);

		return(lBrackTokens.size() > 0 && rBrackTokens.size() > 0);
	}

	private void handleArrayAssignment(String resultString) {
		TerminalNode identifierNode = leftHandExprCtx.expressionName().Identifier();
		ArrayAccessContext arrayIndexExprCtx = leftHandExprCtx.arrayAccess();

		JavaValue javaValue = VariableSearcher.searchVariable(identifierNode.getText());
		JavaArray javaArray = (JavaArray) javaValue.getValue();

		BigDecimal resultValue;
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, arrayIndexExprCtx);

		Expression evalEx = new Expression(arrayIndexExprCtx.getText());
		resultValue = evalEx.eval();
		//EvaluationCommand evaluationCommand = new EvaluationCommand(arrayIndexExprCtx);
		//evaluationCommand.execute();

		//create a new array value to replace value at specified index
		JavaValue newArrayValue = new JavaValue(null, javaArray.getPrimitiveType());
		newArrayValue.setValue(resultString);
		javaArray.updateValueAt(newArrayValue, resultValue.intValue());

		//Console.log("Index to access: " +evaluationCommand.getResult().intValue()+ " Updated with: " +resultString);
	}

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
	}

	@Override
	public void exitEveryRule(ParserRuleContext parserRuleContext) {

	}

	private void evaluateVariable(VariableDeclaratorIdContext exprCtx) {
		JavaValue javaValue = VariableSearcher
				.searchVariable(exprCtx.getText());

		modifiedExp = modifiedExp.replaceFirst(exprCtx.getText(),
				javaValue.getValue().toString());
	}
}