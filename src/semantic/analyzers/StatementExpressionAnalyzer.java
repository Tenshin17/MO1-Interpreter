package semantic.analyzers;

import antlr.Java8Lexer;
import antlr.Java8Parser.*;
import antlr.Java8Parser.StatementExpressionContext;
import error.CustomErrorStrategy;
import error.checkers.ConstChecker;
import Execution.ExecutionManager;
import Execution.command.ICommand;
import Execution.command.ICondCommand;
import Execution.command.ICtrlCommand;
import Execution.command.evaluation.AssignCom;
import SimpleCom.MethodCallCom;
import SimpleCom.IncDecCom;
import semantic.statements.StatementControlOverseer;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

/**
 * Analyzes a given expression on the statement level.
 * This does not include field declaration analysis.
 *
 */
public class StatementExpressionAnalyzer implements ParseTreeListener {

	private ExpressionContext readRightHandExprCtx; //used to avoid mistakenly reading right hand expressions as direct function calls as well.
	
	public final static int FUNCTION_CALL_NO_PARAMS_DEPTH = 13;
	public final static int FUNCTION_CALL_WITH_PARAMS_DEPTH = 14;
	
	public StatementExpressionAnalyzer() {
		
	}
	
	public void analyze(StatementExpressionContext statementExprCtx) {
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, statementExprCtx);
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
		if(ctx instanceof StatementExpressionContext) {
			StatementExpressionContext exprCtx = (StatementExpressionContext) ctx;
			
			if(exprCtx.assignment() != null) {
				ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Assignment expr detected: " +exprCtx.getText()));
				//System.out.println("assign expr:"+exprCtx.getText());
				AssignmentContext exprListCtx = exprCtx.assignment();
				AssignCom assignmentCommand = new AssignCom(exprListCtx.leftHandSide(), exprListCtx.expression());
				
				this.readRightHandExprCtx = exprListCtx.expression();
				this.handleStatementExecution(assignmentCommand);
				
			}
			else if(exprCtx.postIncrementExpression() != null) {
				ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Increment expr detected: " +exprCtx.getText()));

				PostIncrementExpressionContext exprListCtx = exprCtx.postIncrementExpression();
				if(!ConstChecker.isConstFormat(exprListCtx.postfixExpression().expressionName())) {
					IncDecCom incDecCommand = new IncDecCom(exprListCtx.postfixExpression(), Java8Lexer.INC);
					this.handleStatementExecution(incDecCommand);
				} else {
					CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.CONST_INTDEC,
							exprListCtx.postfixExpression().getText(), exprListCtx.postfixExpression().getStart().getLine());
				}
			}

			else if(exprCtx.postDecrementExpression() != null) {
				ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Decrement expr detected: " +exprCtx.getText()));

				PostDecrementExpressionContext exprListCtx = exprCtx.postDecrementExpression();
				if(!ConstChecker.isConstFormat(exprListCtx.postfixExpression().expressionName())) {
					IncDecCom incDecCommand = new IncDecCom(exprListCtx.postfixExpression(), Java8Lexer.DEC);
					this.handleStatementExecution(incDecCommand);
				} else {
					CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.CONST_INTDEC,
							exprListCtx.postfixExpression().getText(), exprListCtx.postfixExpression().getStart().getLine());
				}
			}
			
			else if(exprCtx.methodInvocation() != null && exprCtx.methodInvocation().argumentList() != null) {
				this.handleFunctionCallWithParams(exprCtx.methodInvocation());
			}
			
			else if(exprCtx.methodInvocation() != null && exprCtx.methodInvocation().argumentList() == null) {
				this.handleFunctionCallWithNoParams(exprCtx.methodInvocation());
			}
		}
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub
		
	}
	
	private void handleStatementExecution(ICommand command) {
		
		StatementControlOverseer statementControl = StatementControlOverseer.getInstance();
		
		//add to conditional controlled command
		if(statementControl.isInConditionalCommand()) {
			ICondCommand conditionalCommand = (ICondCommand) statementControl.getActiveControlledCommand();
			
			if(statementControl.isInPositiveRule()) {
				conditionalCommand.addPositiveCommand(command);
			}
			else {
				conditionalCommand.addNegativeCommand(command);
			}
		}
		
		else if(statementControl.isInControlledCommand()) {
			ICtrlCommand controlledCommand = (ICtrlCommand) statementControl.getActiveControlledCommand();
			controlledCommand.addCommand(command);
		}
		else {
			ExecutionManager.getExecutionManager().addCommand(command);
		}
		
	}
	
	private void handleFunctionCallWithParams(MethodInvocationContext funcExprCtx) {
		MethodInvocationContext functionExprCtx = funcExprCtx;
		String functionName = functionExprCtx.methodName().Identifier().getText();
		
		MethodCallCom functionCallCommand = new MethodCallCom(functionName, funcExprCtx);
		this.handleStatementExecution(functionCallCommand);

		ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Function call with params detected: " +functionName));
	}
	
	private void handleFunctionCallWithNoParams(MethodInvocationContext funcExprCtx) {
		String functionName = funcExprCtx.methodName().Identifier().getText();
		
		MethodCallCom functionCallCommand = new MethodCallCom(functionName, funcExprCtx);
		this.handleStatementExecution(functionCallCommand);

		ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Function call with no params detected: " +functionName));
	}
	
	public static boolean isAssignmentExpression(StatementExpressionContext exprCtx) {
		List<TerminalNode> tokenList = exprCtx.getTokens(Java8Lexer.ASSIGN);
		return (tokenList.size() > 0);
	}
	
	public static boolean isIncrementExpression(StatementExpressionContext exprCtx) {
		List<TerminalNode> incrementList = exprCtx.postIncrementExpression().getTokens(Java8Lexer.INC);
		
		return (incrementList.size() > 0);
	}
	
	public static boolean isDecrementExpression(StatementExpressionContext exprCtx) {
		List<TerminalNode> decrementList = exprCtx.postDecrementExpression().getTokens(Java8Lexer.DEC);
		
		return (decrementList.size() > 0);
	}
	/*
	private boolean isFunctionCallWithParams(StatementExpressionContext exprCtx) {
		StatementExpressionContext firstExprCtx = exprCtx.expression(0);
		
		if(firstExprCtx != null) {
			if(exprCtx != this.readRightHandExprCtx) {
				
				return (firstExprCtx.Identifier() != null);
			}
		}
		
		return false;
		
	}
	
	private boolean isFunctionCallWithNoParams(StatementExpressionContext exprCtx) {
		if(exprCtx.depth() == FUNCTION_CALL_NO_PARAMS_DEPTH) {
			if(exprCtx.Identifier() != null)
				return true;
		}
		
		return false;
	}
	*/
}
