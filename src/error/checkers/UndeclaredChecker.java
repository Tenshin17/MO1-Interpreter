/**
 *
 */
package error.checkers;

import antlr.Java8Parser.*;
import antlr.Java8Parser.ExpressionContext;
import antlr.Java8Parser.StatementContext;
import error.CustomErrorStrategy;
import error.ParserHandler;
import Execution.ExecutionManager;
import Execution.command.evaluation.EvaluationCommand;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.searching.VariableSearcher;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Checker for undeclared variables and function

 *
 */
public class UndeclaredChecker implements IErrorChecker, ParseTreeListener {

	private ExpressionContext exprCtx;
	private LeftHandSideContext lhsCtx;
	private int lineNumber;

	public UndeclaredChecker(ExpressionContext exprCtx) {
		this.exprCtx = exprCtx;
		lineNumber = this.exprCtx.getStart().getLine();
	}

	public UndeclaredChecker(LeftHandSideContext exprCtx) {
		this.lhsCtx = exprCtx;
		lineNumber = this.exprCtx.getStart().getLine();
	}

	@Override
	public void verify() {
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, exprCtx);
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
		if(ctx instanceof MethodInvocationContext) {
			MethodInvocationContext exprCtx = (MethodInvocationContext) ctx;
			if (EvaluationCommand.isFunctionCall(exprCtx)) {
				verifyFunctionCall(exprCtx);
			}
		}
		else if(ctx instanceof VariableDeclaratorIdContext) {
			VariableDeclaratorIdContext exprCtx = (VariableDeclaratorIdContext) ctx;
			if(EvaluationCommand.isVariableOrConst(exprCtx)) {
				verifyVariableOrConst(exprCtx);
			}
		}
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub

	}

	private void verifyFunctionCall(MethodInvocationContext funcExprCtx) {

		if(funcExprCtx.methodName().Identifier() == null)
			return;

		String functionName = funcExprCtx.methodName().Identifier().getText();

		ClassScope classScope = SymbolTableManager.getInstance().getClassScope(
				ParserHandler.getInstance().getCurrentClassName());
		JavaMethod javaMethod = classScope.searchMethod(functionName);

		if(javaMethod == null) {
			CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.UNDECLARED_FUNCTION, functionName, lineNumber);
		}
		else {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Function found: " +functionName));
		}
	}

	private void verifyVariableOrConst(VariableDeclaratorIdContext varExprCtx) {
		JavaValue javaValue = null;

		if(ExecutionManager.getExecutionManager().isInFunctionExecution()) {
			JavaMethod javaMethod = ExecutionManager.getExecutionManager().getCurrentFunction();
			javaValue = VariableSearcher.searchVariableInFunction(javaMethod, varExprCtx.getText());
		}

		//if after function finding, Java value is still null, search class
		if(javaValue == null) {
			ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
			javaValue = VariableSearcher.searchVariableInClassIncludingLocal(classScope, varExprCtx.getText());
		}

		//after second pass, we conclude if it cannot be found already
		if(javaValue == null) {
			CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.UNDECLARED_VARIABLE, varExprCtx.getText(), lineNumber);
		}
	}

	/*
	 * Verifies a var or const identifier from a scan statement since scan grammar is different.
	 */
	public static void verifyVarOrConstForScan(String identifier, StatementContext statementCtx) {
		ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
		JavaValue javaValue = VariableSearcher.searchVariableInClassIncludingLocal(classScope, identifier);

		Token firstToken = statementCtx.getStart();

		if(javaValue == null) {
			CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.UNDECLARED_VARIABLE, identifier, firstToken.getLine());
		}
	}

}