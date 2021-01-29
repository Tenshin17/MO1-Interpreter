package LoopsCondStmt.Analyze;

import baraco.antlr.lexer.Java8Lexer;
import baraco.antlr.parser.BaracoParser.*;
import baraco.builder.ParserHandler;
import baraco.execution.ExecutionManager;
import baraco.execution.commands.controlled.*;
import baraco.execution.commands.simple.PrintCommand;
import baraco.execution.commands.simple.ReturnCom;
import baraco.execution.commands.simple.ScanCommand;
import baraco.representations.BaracoMethod;
import baraco.semantics.statements.StmtCntrl;
import baraco.semantics.symboltable.SymbolTableManager;
import baraco.semantics.symboltable.scopes.ClassScope;
import baraco.semantics.symboltable.scopes.LocalScopeCreator;


import antlr.*;
import PrintScan.PrintCom;
import PrintScan.ReturnCom;
import PrintScan.ScanCom;
import VarAndConstDec.javaMethod;
import LoopsCondStmt.Stmt.StmtCntrl;
import symboltable.SymbolTableManager;
import symboltable.scope.ClassScope;
import symboltable.scope.LocalScopeCreator;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class StmtAnalyze {

    public StmtAnalyze() {

    }

    public void analyze(StatementContext ctx) {

        if(ctx.PRINT() != null || ctx.PRINTLN() != null) {
            this.handlePrintStatement(ctx);
        }

        else if (ctx.scanStatement() != null) {
            this.handleScanStatement(ctx.scanStatement());
        }
        // Add Scan implementation here
        //an expression
        else if(ctx.statementExpression() != null) {
            System.out.println("STATEMENT ANALYZER: " + ctx.statementExpression().expression().start.getText());
            StmtExprAnalyze expressionAnalyzer = new StmtExprAnalyze();
            expressionAnalyzer.analyze(ctx.statementExpression());
        }

        //a block statement
        else if(isTRYStatement(ctx)) {

            BlockContext blockContext = ctx.block();

            TryCommand tryCommand = new TryCommand();
            StmtCntrl.getInstance().openAttemptCommand(tryCommand);

            BlockAnalyze tryBlockAnalyzer = new BlockAnalyze();
            tryBlockAnalyzer.analyze(blockContext);

            StmtCntrl.getInstance().reportExitTryBlock();

            // loop through catch clauses available
            for (CatchClauseContext cCContext:
                    ctx.catchClause()) {

                StmtCntrl.getInstance().setCurrentCatchClause(determineCatchType(cCContext.catchType()));

                BlockAnalyze catchBlockAnalyzer = new BlockAnalyze();
                catchBlockAnalyzer.analyze(cCContext.block());

            }

            StmtCntrl.getInstance().compileControlledCommand();
            StmtCntrl.getInstance().setCurrentCatchClause(null);
        }
        else if(ctx.block() != null) {
            BlockContext blockCtx = ctx.block();

            BlockAnalyze blockAnalyzer = new BlockAnalyze();
            blockAnalyzer.analyze(blockCtx);
        }
        else if(isIFStatement(ctx)) {
            //Console.log("Par expression: " +ctx.parExpression().getText());

            StatementContext statementCtx = ctx.statement(0);

            //Console.log(LogType.DEBUG, "IF statement: " +statementCtx.getText());

            IfCommand ifCommand = new IfCommand(ctx.parExpression());
            StmtCntrl.getInstance().openConditionalCommand(ifCommand);

            StmtAnalyze statementAnalyzer = new StmtAnalyze();
            statementAnalyzer.analyze(statementCtx);

            StmtCntrl.getInstance().reportExitPositiveRule();

            //check if there is an ELSE statement
            if (isELSEStatement(ctx)) {
                statementCtx = ctx.statement(1);

                //Console.log(LogType.DEBUG, "ELSE statement: " +statementCtx.getText());
                statementAnalyzer.analyze(statementCtx);
            }

            StmtCntrl.getInstance().compileControlledCommand();
        }
        else if(isFORStatement(ctx)) {
            System.out.println("FOR expression: " +ctx.forControl().getText());

            LocalScopeCreator.getInstance().openLocalScope();

            ForControlAnalyze forControlAnalyzer = new ForControlAnalyze();
            forControlAnalyzer.analyze(ctx.forControl());

            ForCommand forCommand = new ForCommand(forControlAnalyzer.getLocalVarDecContext(), forControlAnalyzer.getExprContext(), forControlAnalyzer.getUpdateCommand());
            StmtCntrl.getInstance().openControlledCommand(forCommand);

            StatementContext statementCtx = ctx.statement(0);
            StmtExprAnalyze statementAnalyzer = new StmtExprAnalyze();
            statementAnalyzer.analyze(statementCtx);

            StmtCntrl.getInstance().compileControlledCommand();

            LocalScopeCreator.getInstance().closeLocalScope();
            System.out.println("End of FOR loop");
        }
        else if(isWHILEStatement(ctx)) {
            //Console.log(LogType.DEBUG, "While par expression: " +ctx.parExpression().getText());

            StatementContext statementCtx = ctx.statement(0);

            WhileCommand whileCommand = new WhileCommand(ctx.parExpression());
            StmtCntrl.getInstance().openControlledCommand(whileCommand);

            StmtAnalyze statementAnalyzer = new StmtAnalyze();
            statementAnalyzer.analyze(statementCtx);

            StmtCntrl.getInstance().compileControlledCommand();
            //Console.log(LogType.DEBUG, "End of WHILE expression: " +ctx.parExpression().getText());
        }
        else if(isDOWHILEStatement(ctx)) {
            //Console.log(LogType.DEBUG, "Do while expression: " +ctx.parExpression().getText());

            StatementContext statementCtx = ctx.statement(0);

            DoWhileCommand doWhileCommand = new DoWhileCommand(ctx.parExpression());
            StmtCntrl.getInstance().openControlledCommand(doWhileCommand);

            StmtAnalyze statementAnalyzer = new StmtAnalyze();
            statementAnalyzer.analyze(statementCtx);

            StmtCntrl.getInstance().compileControlledCommand();
            //Console.log(LogType.DEBUG, "End of DO-WHILE expression: " +ctx.parExpression().getText());
        }
        else if(isRETURNStatement(ctx) && ExecutionManager.getInstance().isInFunctionExecution()) {
            System.out.println("Detected return expression: " +ctx.expression(0).getText());
            this.handleReturnStatement(ctx.expression(0));
        }
    }

    private void handlePrintStatement(StatementContext ctx) {
        System.out.println("HANDLE PRINT: " + ctx.expression().size());

        PrintCommand printCommand = new PrintCommand(ctx);

        StmtCntrl statementControl = StmtCntrl.getInstance();
        //add to conditional controlled command
        if(statementControl.isInConditionalCommand()) {
            IConditionalCommand conditionalCommand = (IConditionalCommand) statementControl.getActiveControlledCommand();

            if(statementControl.isInPositiveRule()) {
                conditionalCommand.addPositiveCommand(printCommand);
            }
            else {
                conditionalCommand.addNegativeCommand(printCommand);
            }
        }

        else if(statementControl.isInControlledCommand()) {
            IControlledCommand controlledCommand = (IControlledCommand) statementControl.getActiveControlledCommand();
            controlledCommand.addCommand(printCommand);
        }
        else if (statementControl.isInAttemptCommand()) {
            IAttemptCommand attemptCommand = (IAttemptCommand) statementControl.getActiveControlledCommand();

            if(statementControl.isInTryBlock()) {
                attemptCommand.addTryCommand(printCommand);
            } else {
                attemptCommand.addCatchCommand(statementControl.getCurrentCatchType(), printCommand);
            }
        }
        else {
            ExecutionManager.getInstance().addCommand(printCommand);
        }

    }

    private void handleScanStatement(ScanStatementContext ctx) {
        //System.out.println("HANDLE SCAN: " + ctx.expression().getText() + ctx.Identifier().getText());
        ScanCommand scanCommand;

        if(ctx.expression(1) != null)
            scanCommand = new ScanCommand(ctx.expression(0).getText(), ctx.expression(1), ctx.Identifier().getText());
        else
            scanCommand = new ScanCommand(ctx.expression(0).getText(), ctx.Identifier().getText());

        StmtCntrl statementControl = StmtCntrl.getInstance();

        if(statementControl.isInConditionalCommand()) {
            IConditionalCommand conditionalCommand = (IConditionalCommand) statementControl.getActiveControlledCommand();

            if(statementControl.isInPositiveRule()) {
                conditionalCommand.addPositiveCommand(scanCommand);
            }
            else {
                conditionalCommand.addNegativeCommand(scanCommand);
            }
        }

        else if(statementControl.isInControlledCommand()) {
            IControlledCommand controlledCommand = (IControlledCommand) statementControl.getActiveControlledCommand();
            controlledCommand.addCommand(scanCommand);
        }
        else if (statementControl.isInAttemptCommand()) {
            IAttemptCommand attemptCommand = (IAttemptCommand) statementControl.getActiveControlledCommand();

            if(statementControl.isInTryBlock()) {
                attemptCommand.addTryCommand(scanCommand);
            } else {
                attemptCommand.addCatchCommand(statementControl.getCurrentCatchType(), scanCommand);
            }
        }
        else {
            ExecutionManager.getInstance().addCommand(scanCommand);
        }

    }

    private void handleReturnStatement(ExpressionContext exprCtx) {
        ReturnCom returnCommand = new ReturnCom(exprCtx, ExecutionManager.getInstance().getCurrentFunction());
        /*
         * TODO: Return commands supposedly stops a controlled or conditional command and returns back the control to the caller.
         * Find a way to halt such commands if they are inside a controlled command.
         */
        StmtCntrl statementControl = StmtCntrl.getInstance();

        if(statementControl.isInConditionalCommand()) {
            IConditionalCommand conditionalCommand = (IConditionalCommand) statementControl.getActiveControlledCommand();

            if(statementControl.isInPositiveRule()) {
                conditionalCommand.addPositiveCommand(returnCommand);
            }
            else {
                String functionName = ExecutionManager.getInstance().getCurrentFunction().getMethodName();
                ExecutionManager.getInstance().getCurrentFunction().setValidReturns(true);
                conditionalCommand.addNegativeCommand(returnCommand);
            }
        }

        else if(statementControl.isInControlledCommand()) {
            IControlledCommand controlledCommand = (IControlledCommand) statementControl.getActiveControlledCommand();
            controlledCommand.addCommand(returnCommand);
        }
        else if (statementControl.isInAttemptCommand()) {
            IAttemptCommand attemptCommand = (IAttemptCommand) statementControl.getActiveControlledCommand();

            if(statementControl.isInTryBlock()) {
                attemptCommand.addTryCommand(returnCommand);
            } else {
                attemptCommand.addCatchCommand(statementControl.getCurrentCatchType(), returnCommand);
            }
        }
        else {
            ExecutionManager.getInstance().getCurrentFunction().setValidReturns(true);

            ExecutionManager.getInstance().addCommand(returnCommand);
        }

    }

    public static boolean isTRYStatement(StatementContext ctx) {
        List<TerminalNode> tryTokenList = ctx.getTokens(Java8Lexer.TRY);

        return (tryTokenList.size() != 0);
    }
    /*
        public static boolean isCATCHStatement(StatementContext ctx) {
            List<TerminalNode> catchTokenList = ctx.getTokens(Java8Lexer.CATCH);

            return (catchTokenList.size() != 0);
        }

        public static boolean isFINALLYStatement(StatementContext ctx) {
            List<TerminalNode> finallyTokenList = ctx.getTokens(Java8Lexer.FINALLY);

            return (finallyTokenList.size() != 0);
        }
        */
    public static IAttemptCommand.CatchTypeEnum determineCatchType(CatchTypeContext ctx) {
        if (ctx.getTokens(Java8Lexer.ARITHMETIC_EXCEPTION).size() > 0) {
            return IAttemptCommand.CatchTypeEnum.ARITHMETIC_EXCEPTION;
        } else if (ctx.getTokens(Java8Lexer.ARRAY_BOUNDS_EXCEPTION).size() > 0) {
            return IAttemptCommand.CatchTypeEnum.ARRAY_OUT_OF_BOUNDS;
        } else if (ctx.getTokens(Java8Lexer.NEGATIVE_ARRSIZE_EXCEPTION).size() > 0) {
            return IAttemptCommand.CatchTypeEnum.NEGATIVE_ARRAY_SIZE;
        }

        return null;
    }

    public static boolean isFORStatement(StatementContext ctx) {
        List<TerminalNode> forTokenList = ctx.getTokens(Java8Lexer.FOR);

        return (forTokenList.size() != 0);
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
    public static boolean isRETURNStatement(StatementContext ctx) {
        List<TerminalNode> returnTokenList = ctx.getTokens(Java8Lexer.RETURN);

        return (returnTokenList.size() != 0);
    }

}
