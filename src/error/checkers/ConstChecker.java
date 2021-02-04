package error.checkers;

import antlr.Java8Parser.*;
import error.CustomErrorStrategy;
import error.ParserHandler;
import Execution.ExecutionManager;
import Execution.command.evaluation.EvaluationCommand;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.searching.VariableSearcher;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ConstChecker implements IErrorChecker, ParseTreeListener {

	private ExpressionStatementContext exprCtx;
	private LeftHandSideContext lhsCtx;
	private int lineNumber;

	public ConstChecker(ExpressionStatementContext exprCtx) {
		this.exprCtx = exprCtx;
		this.lineNumber = this.exprCtx.getStart().getLine();
	}

	public ConstChecker(LeftHandSideContext exprCtx) {
		this.lhsCtx = exprCtx;
		this.lineNumber = this.lhsCtx.getStart().getLine();
	}

	@Override
	public void verify() {
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		if(this.exprCtx != null)
			treeWalker.walk(this, this.exprCtx);
		else if(this.lhsCtx != null)
			treeWalker.walk(this,this.lhsCtx);
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
		if(ctx instanceof VariableDeclaratorIdContext) {
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

	private void verifyVariableOrConst(VariableDeclaratorIdContext varExprCtx) {
		JavaValue javaValue = null;

		if(ExecutionManager.getExecutionManager().isInFunctionExecution()) {
			JavaMethod javaMethod = ExecutionManager.getExecutionManager().getCurrentFunction();
			javaValue = VariableSearcher.searchVariableInFunction(javaMethod, varExprCtx.getText());
		}

		//if after function finding, java value is still null, search class
		if(javaValue == null) {
			ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
			javaValue = VariableSearcher.searchVariableInClassIncludingLocal(classScope, varExprCtx.getText());
		}

		if(javaValue != null && isConstFormat(varExprCtx)) {
			javaValue.markFinal();
			CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.CONST_REASSIGNMENT, varExprCtx.getText(), this.lineNumber);
		}
	}

	public static boolean isConstFormat(VariableDeclaratorIdContext exprCtx)
	{
		String s = exprCtx.getText();
		for (int i=0; i<s.length(); i++)
		{
			if (Character.isLowerCase(s.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}

	public static boolean isConstFormat(ExpressionNameContext exprCtx)
	{
		String s = exprCtx.getText();
		for (int i=0; i<s.length(); i++)
		{
			if (Character.isLowerCase(s.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}

}