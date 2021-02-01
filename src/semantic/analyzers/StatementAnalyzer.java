package semantic.analyzers;

import Execution.command.controlled.ForCom;
import antlr.Java8Lexer;
import antlr.Java8Parser.*;
import antlr.Java8Parser.BlockContext;
import antlr.Java8Parser.ExpressionContext;
import antlr.Java8Parser.StatementContext;
import error.checkers.UndeclaredChecker;
import Execution.ExecutionManager;
import Execution.command.*;
import Execution.command.controlled.*;
import SimpleCom.PrintCom;
import SimpleCom.ReturnCom;
import SimpleCom.ScanCom;
import semantic.statements.StatementControlOverseer;
import semantic.symboltable.scope.LocalScopeCreator;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

/**
 * A bridge for statement listener
 *
 */
public class StatementAnalyzer {

	public StatementAnalyzer() {
		
	}
	
	public void analyze(StatementContext ctx) {

		//print statement
		if(ctx.printStatement() != null) handlePrintStatement(ctx);
		else if(ctx.scanStatement() != null) handleScanStatement(ctx);
		//an expression
		/*
		else if(ctx.statementWithoutTrailingSubstatement() != null) {
			StatementExpressionAnalyzer expressionAnalyzer = new StatementExpressionAnalyzer();
			expressionAnalyzer.analyze(ctx.statementWithoutTrailingSubstatement().expressionStatement().statementExpression());
		}*/

		else if(ctx.statementWithoutTrailingSubstatement() != null) {
			BlockContext blockCtx = ctx.statementWithoutTrailingSubstatement().block();
			System.out.println(blockCtx.getText()+"Im HERE");
			BlockAnalyzer blockAnalyzer = new BlockAnalyzer();
			blockAnalyzer.analyze(blockCtx.blockStatements());
		}

		//an IF statement
		else if(isIFStatement(ctx)) {

			//check if there is an ELSE statement
			if(isELSEStatement(ctx)) {
                IfThenElseStatementContext statementCtx = ctx.ifThenElseStatement();

                IfCom ifCommand = new IfCom(ctx.ifThenElseStatement().expression().assignmentExpression().conditionalExpression());
                StatementControlOverseer.getInstance().openConditionalCommand(ifCommand);

                StatementAnalyzer statementAnalyzer = new StatementAnalyzer();
                statementAnalyzer.analyze(statementCtx.statement());

                StatementControlOverseer.getInstance().reportExitPositiveRule();

				IfThenElseStatementContext statementCtx2 = ctx.ifThenElseStatement();

				statementAnalyzer.analyze(statementCtx2.statement());
			}
			else {
                IfThenStatementContext statementCtx = ctx.ifThenStatement();

                IfCom ifCommand = new IfCom(ctx.ifThenStatement().expression().assignmentExpression().conditionalExpression());
                StatementControlOverseer.getInstance().openConditionalCommand(ifCommand);

                StatementAnalyzer statementAnalyzer = new StatementAnalyzer();
                statementAnalyzer.analyze(statementCtx.statement());

                StatementControlOverseer.getInstance().reportExitPositiveRule();
            }

			StatementControlOverseer.getInstance().compileControlledCommand();
		}

		else if(isWHILEStatement(ctx)) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("While par expression: " +ctx.whileStatement().expression().getText()));

			WhileStatementContext statementCtx = ctx.whileStatement();

			WhileCom whileCommand = new WhileCom(ctx.whileStatement().expression().assignmentExpression().conditionalExpression());
			StatementControlOverseer.getInstance().openControlledCommand(whileCommand);

			StatementAnalyzer statementAnalyzer = new StatementAnalyzer();
			statementAnalyzer.analyze(statementCtx.statement());

			StatementControlOverseer.getInstance().compileControlledCommand();
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("End of WHILE expression: " +ctx.whileStatement().expression().getText()));
		}
        /*
		else if(isDOWHILEStatement(ctx)) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Do while expression: " +ctx.parExpression().getText()));

			StatementContext statementCtx = ctx.statement(0);

			DoWhileCom doWhileCom = new DoWhileCom(ctx.parExpression());
			StatementControlOverseer.getInstance().openControlledCommand(doWhileCom);

			//StatementAnalyzer statementAnalyzer = new StatementAnalyzer();
			//statementAnalyzer.analyze(statementCtx);

			StatementControlOverseer.getInstance().compileControlledCommand();
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("End of DO-WHILE expression: " +ctx.parExpression().getText()));
		}
        */
		else if(isFORStatement(ctx)) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("FOR expression: " +ctx.forStatement().basicForStatement().getText()));

			LocalScopeCreator.getInstance().openLocalScope();

			ForControlAnalyzer forControlAnalyzer = new ForControlAnalyzer();
			forControlAnalyzer.analyze(ctx.forStatement().basicForStatement());

			ForCom forCommand = new ForCom(forControlAnalyzer.getLocalVarDecContext(), forControlAnalyzer.getExprContext(), forControlAnalyzer.getUpdateCommand());
			StatementControlOverseer.getInstance().openControlledCommand(forCommand);

			StatementContext statementCtx = ctx.forStatement().basicForStatement().statement();
			StatementAnalyzer statementAnalyzer = new StatementAnalyzer();
			statementAnalyzer.analyze(statementCtx);

			StatementControlOverseer.getInstance().compileControlledCommand();

			LocalScopeCreator.getInstance().closeLocalScope();
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("End of FOR loop"));
		}

		else if(isRETURNStatement(ctx) && ExecutionManager.getExecutionManager().isInFunctionExecution()) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Detected return expression: " +ctx.statementWithoutTrailingSubstatement().returnStatement().expression().getText()));
			handleReturnStatement(ctx.statementWithoutTrailingSubstatement().returnStatement().expression());
		}
	}
	
	private void handlePrintStatement(StatementContext ctx) {
		PrintCom printCommand = new PrintCom(ctx.printStatement());
		
		StatementControlOverseer statementControl = StatementControlOverseer.getInstance();
		//add to conditional controlled command
		if(statementControl.isInConditionalCommand()) {
			ICondCommand conditionalCommand = (ICondCommand) statementControl.getActiveControlledCommand();
			
			if(statementControl.isInPositiveRule()) {
				conditionalCommand.addPositiveCommand(printCommand);
			}
			else {
				conditionalCommand.addNegativeCommand(printCommand);
			}
		}
		
		else if(statementControl.isInControlledCommand()) {
			ICtrlCommand controlledCommand = (ICtrlCommand) statementControl.getActiveControlledCommand();
			controlledCommand.addCommand(printCommand);
		}
		else {
			ExecutionManager.getExecutionManager().addCommand(printCommand);
		}
		
	}
	
	private void handleScanStatement(StatementContext ctx) {
		System.out.println("Hi! " + ctx.scanStatement().getText());
	    ScanCom scanCommand = new ScanCom(ctx.scanStatement().StringLiteral().getText(), ctx.scanStatement().Identifier().getText()); // not sure if edited right
		UndeclaredChecker.verifyVarOrConstForScan(ctx.scanStatement().Identifier().getText(), ctx);
		
		StatementControlOverseer statementControl = StatementControlOverseer.getInstance();
		//add to conditional controlled command
		if(statementControl.isInConditionalCommand()) {
			ICondCommand conditionalCommand = (ICondCommand) statementControl.getActiveControlledCommand();
			
			if(statementControl.isInPositiveRule()) {
				conditionalCommand.addPositiveCommand(scanCommand);
			}
			else {
				conditionalCommand.addNegativeCommand(scanCommand);
			}
		}
		
		else if(statementControl.isInControlledCommand()) {
			ICtrlCommand controlledCommand = (ICtrlCommand) statementControl.getActiveControlledCommand();
			controlledCommand.addCommand(scanCommand);
		}
		else {
			ExecutionManager.getExecutionManager().addCommand(scanCommand);
		}
		
	}
	
	private void handleReturnStatement(ExpressionContext exprCtx) {
		ReturnCom returnCommand = new ReturnCom(exprCtx, ExecutionManager.getExecutionManager().getCurrentFunction());
		/*
		 * TODO: Return command supposedly stops a controlled or conditional command and returns back the control to the caller.
		 * Find a way to halt such command if they are inside a controlled command.
		 */
		StatementControlOverseer statementControl = StatementControlOverseer.getInstance();
		
		if(statementControl.isInConditionalCommand()) {
			ICondCommand conditionalCommand = (ICondCommand) statementControl.getActiveControlledCommand();
			
			if(statementControl.isInPositiveRule()) {
				conditionalCommand.addPositiveCommand(returnCommand);
			}
			else {
				String functionName = ExecutionManager.getExecutionManager().getCurrentFunction().getFunctionName();
				conditionalCommand.addNegativeCommand(returnCommand);
			}
		}
		
		else if(statementControl.isInControlledCommand()) {
			ICtrlCommand controlledCommand = (ICtrlCommand) statementControl.getActiveControlledCommand();
			controlledCommand.addCommand(returnCommand);
		}
		else {
			ExecutionManager.getExecutionManager().addCommand(returnCommand);
		}
		
	}
	
	public static boolean isIFStatement(StatementContext ctx) {
		List<TerminalNode> tokenList = ctx.getTokens(Java8Lexer.IF);
		
		return (tokenList.size() != 0);
	}
	
	public static boolean isELSEStatement(StatementContext ctx) {
		List<TerminalNode> tokenList = ctx.getTokens(Java8Lexer.ELSE);
		
		return (tokenList.size() != 0);
	}
	
	public static boolean isWHILEStatement(StatementContext ctx) {
		List<TerminalNode> whileTokenList = ctx.getTokens(Java8Lexer.WHILE);
		List<TerminalNode> doTokenList = ctx.getTokens(Java8Lexer.DO);
		
		return (whileTokenList.size() != 0 && doTokenList.size() == 0);
	}
	
	public static boolean isDOWHILEStatement(StatementContext ctx) {
		List<TerminalNode> whileTokenList = ctx.getTokens(Java8Lexer.WHILE);
		List<TerminalNode> doTokenList = ctx.getTokens(Java8Lexer.DO);
		
		return (whileTokenList.size() != 0 && doTokenList.size() != 0);
	}
	
	public static boolean isFORStatement(StatementContext ctx) {
		List<TerminalNode> forTokenList = ctx.getTokens(Java8Lexer.FOR);
		
		return (forTokenList.size() != 0);
	}
	
	public static boolean isRETURNStatement(StatementContext ctx) {
		List<TerminalNode> returnTokenList = ctx.getTokens(Java8Lexer.RETURN);
		
		return (returnTokenList.size() != 0);
	}
}
