package baraco.semantics.analyzers;
//package LoopsCondStmt.Analyze;

import baraco.antlr.lexer.BaracoLexer;
import baraco.antlr.parser.BaracoParser.*;
import baraco.builder.BuildChecker;
import baraco.builder.ErrorRepository;
import baraco.execution.ExecutionManager;
import baraco.execution.commands.EvaluationCommand;
import baraco.execution.commands.ICommand;
import baraco.execution.commands.controlled.IAttemptCommand;
import baraco.execution.commands.controlled.IConditionalCommand;
import baraco.execution.commands.controlled.IControlledCommand;
import baraco.execution.commands.evaluation.AssignmentCommand;
import baraco.execution.commands.evaluation.ShorthandCommand;
import baraco.execution.commands.simple.IncDecCommand;
import baraco.execution.commands.simple.MethodCallCommand;
import baraco.semantics.statements.StatementControlOverseer;
import baraco.semantics.utils.Expression;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class StmtExprAnalyze implements ParseTreeListener {

    private int called = 0;
    private ExpressionContext readRightHandExprCtx; //used to avoid mistakenly reading right hand expressions as direct function calls as well.

    //TODO: find a way to not rely on tree depth for function calls.
    public final static int FUNCTION_CALL_NO_PARAMS_DEPTH = 13;
    public final static int FUNCTION_CALL_WITH_PARAMS_DEPTH = 14;

    public StatementExpressionAnalyzer() {

    }

    public void analyze(StatementExpressionContext statementExprCtx) {
        ParseTreeWalker treeWalker = new ParseTreeWalker();
        treeWalker.walk(this, statementExprCtx);
    }

    public void analyze(ExpressionContext exprCtx) {
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
        if(ctx instanceof ExpressionContext) {
            ExpressionContext exprCtx = (ExpressionContext) ctx;

            if(isAssignmentExpression(exprCtx)) {
                System.out.println("Assignment expr detected: " +exprCtx.getText());

                List<ExpressionContext> exprListCtx = exprCtx.expression();
                AssignmentCommand assignmentCommand = new AssignmentCommand(exprListCtx.get(0), exprListCtx.get(1));

                this.readRightHandExprCtx = exprListCtx.get(1);
                this.handleStatementExecution(assignmentCommand);

            }
            else if(isAddAssignExpression(exprCtx)) {
                System.out.println("Add assign expr detected: " + exprCtx.getText());

                List<ExpressionContext> exprListCtx = exprCtx.expression();
                ShorthandCommand shorthandCommand = new ShorthandCommand(exprListCtx.get(0), exprListCtx.get(1), BaracoLexer.ADD_ASSIGN);

                this.readRightHandExprCtx = exprListCtx.get(1);
                this.handleStatementExecution(shorthandCommand);
            }
            else if(isSubAssignExpression(exprCtx)) {
                System.out.println("Sub assign expr detected: " + exprCtx.getText());

                List<ExpressionContext> exprListCtx = exprCtx.expression();
                ShorthandCommand shorthandCommand = new ShorthandCommand(exprListCtx.get(0), exprListCtx.get(1), BaracoLexer.SUB_ASSIGN);

                this.readRightHandExprCtx = exprListCtx.get(1);
                this.handleStatementExecution(shorthandCommand);
            }
            else if(isMulAssignExpression(exprCtx)) {
                System.out.println("Mul assign expr detected: " + exprCtx.getText());

                List<ExpressionContext> exprListCtx = exprCtx.expression();
                ShorthandCommand shorthandCommand = new ShorthandCommand(exprListCtx.get(0), exprListCtx.get(1), BaracoLexer.MUL_ASSIGN);

                this.readRightHandExprCtx = exprListCtx.get(1);
                this.handleStatementExecution(shorthandCommand);
            }
            else if(isDivAssignExpression(exprCtx)) {
                System.out.println("Div assign expr detected: " + exprCtx.getText());

                List<ExpressionContext> exprListCtx = exprCtx.expression();
                ShorthandCommand shorthandCommand = new ShorthandCommand(exprListCtx.get(0), exprListCtx.get(1), BaracoLexer.DIV_ASSIGN);

                this.readRightHandExprCtx = exprListCtx.get(1);
                this.handleStatementExecution(shorthandCommand);
            }
            else if(isModAssignExpression(exprCtx)) {
                System.out.println("Mod assign expr detected: " + exprCtx.getText());

                List<ExpressionContext> exprListCtx = exprCtx.expression();
                ShorthandCommand shorthandCommand = new ShorthandCommand(exprListCtx.get(0), exprListCtx.get(1), BaracoLexer.MOD_ASSIGN);

                this.readRightHandExprCtx = exprListCtx.get(1);
                this.handleStatementExecution(shorthandCommand);
            }
            else if(isIncrementExpression(exprCtx)) {
                System.out.println("Increment expr detected: " +exprCtx.getText());

                List<ExpressionContext> exprListCtx = exprCtx.expression();

                IncDecCommand incDecCommand = new IncDecCommand(exprListCtx.get(0), BaracoLexer.INC);
                this.handleStatementExecution(incDecCommand);
            }

            else if(isDecrementExpression(exprCtx)) {
                System.out.println("Decrement expr detected: " +exprCtx.getText());

                List<ExpressionContext> exprListCtx = exprCtx.expression();

                IncDecCommand incDecCommand = new IncDecCommand(exprListCtx.get(0), BaracoLexer.DEC);
                this.handleStatementExecution(incDecCommand);

            }
            else if(isFunctionCall(exprCtx))
                handleFunctionCall(exprCtx);
            else {

                ParserRuleContext prCtx = exprCtx;

                while(!(prCtx instanceof StatementExpressionContext)) {
                    prCtx = prCtx.getParent();
                }

                StatementExpressionContext stExCtx = (StatementExpressionContext) prCtx;

                if(stExCtx.expression() != null) {
                    ExpressionContext expCtx = stExCtx.expression();

                    if (!(isAssignmentExpression(expCtx) ||
                            isAddAssignExpression(expCtx) ||
                            isSubAssignExpression(expCtx) ||
                            isMulAssignExpression(expCtx) ||
                            isDivAssignExpression(expCtx) ||
                            isModAssignExpression(expCtx) ||
                            isIncrementExpression(expCtx) ||
                            isDecrementExpression(expCtx) ||
                            isFunctionCall(expCtx))) {

                        int lineNumber = expCtx.getStart().getLine();

                        BuildChecker.reportCustomError(ErrorRepository.NOT_A_STATEMENT, "", expCtx.getText(), lineNumber);

                        expCtx.children = null;

                    }
                }

            }

            /*else if(this.isFunctionCallWithParams(exprCtx)) {
                this.handleFunctionCallWithParams(exprCtx);
            }

            else if(isFunctionCallWithNoParams(exprCtx)) {
               int i = exprCtx.depth();
               TerminalNode n = exprCtx.Identifier();
                called++;
                if(called % 2 == 1) {
                    System.out.println("depth: " + exprCtx.depth());
                    this.handleFunctionCallWithNoParams(exprCtx);
                }
            }*/
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
            IConditionalCommand conditionalCommand = (IConditionalCommand) statementControl.getActiveControlledCommand();

            if(statementControl.isInPositiveRule()) {
                conditionalCommand.addPositiveCommand(command);
            }
            else {
                conditionalCommand.addNegativeCommand(command);
            }
        }

        else if(statementControl.isInControlledCommand()) {
            IControlledCommand controlledCommand = (IControlledCommand) statementControl.getActiveControlledCommand();
            controlledCommand.addCommand(command);
        }
        else if (statementControl.isInAttemptCommand()) {
            IAttemptCommand attemptCommand = (IAttemptCommand) statementControl.getActiveControlledCommand();

            if(statementControl.isInTryBlock()) {
                attemptCommand.addTryCommand(command);
            } else {
                attemptCommand.addCatchCommand(statementControl.getCurrentCatchType(), command);
            }
        }
        else {
            ExecutionManager.getInstance().addCommand(command);
        }

    }

    private void handleFunctionCallWithParams(ExpressionContext funcExprCtx) {
        ExpressionContext functionExprCtx = funcExprCtx.expression(0);
        String functionName = functionExprCtx.getText();
        //String functionName = functionExprCtx.Identifier().getText();

        MethodCallCommand functionCallCommand = new MethodCallCommand(functionName, funcExprCtx);
        this.handleStatementExecution(functionCallCommand);

        System.out.println("Function call with params detected: " +functionName);
    }

    private void handleFunctionCallWithNoParams(ExpressionContext funcExprCtx) {
        System.out.println("HANDLEEEE: " + funcExprCtx.expression(0).getText());
        String functionName = funcExprCtx.start.getText();

        MethodCallCommand methodCallCommand = new MethodCallCommand(functionName, funcExprCtx);
        this.handleStatementExecution(methodCallCommand);

        System.out.println("Function call with no params detected: " +functionName);
    }

    private void handleFunctionCall(ExpressionContext funcExprCtx) {
        String functionName = funcExprCtx.expression(0).getText();

        MethodCallCommand methodCallCommand = new MethodCallCommand(functionName, funcExprCtx);
        this.handleStatementExecution(methodCallCommand);

    }

    public static boolean isAssignmentExpression(ExpressionContext exprCtx) {
        List<TerminalNode> tokenList = exprCtx.getTokens(BaracoLexer.ASSIGN);

        return (tokenList.size() > 0);
    }

    public static boolean isAddAssignExpression(ExpressionContext exprCtx) {
        List<TerminalNode> tokenList = exprCtx.getTokens(BaracoLexer.ADD_ASSIGN);

        return (tokenList.size() > 0);
    }

    public static boolean isSubAssignExpression(ExpressionContext exprCtx) {
        List<TerminalNode> tokenList = exprCtx.getTokens(BaracoLexer.SUB_ASSIGN);

        return (tokenList.size() > 0);
    }

    public static boolean isMulAssignExpression(ExpressionContext exprCtx) {
        List<TerminalNode> tokenList = exprCtx.getTokens(BaracoLexer.MUL_ASSIGN);

        return (tokenList.size() > 0);
    }

    public static boolean isDivAssignExpression(ExpressionContext exprCtx) {
        List<TerminalNode> tokenList = exprCtx.getTokens(BaracoLexer.DIV_ASSIGN);

        return (tokenList.size() > 0);
    }

    public static boolean isModAssignExpression(ExpressionContext exprCtx) {
        List<TerminalNode> tokenList = exprCtx.getTokens(BaracoLexer.MOD_ASSIGN);

        return (tokenList.size() > 0);
    }

    public static boolean isIncrementExpression(ExpressionContext exprCtx) {
        List<TerminalNode> incrementList = exprCtx.getTokens(BaracoLexer.INC);

        return (incrementList.size() > 0);
    }

    public static boolean isDecrementExpression(ExpressionContext exprCtx) {
        List<TerminalNode> decrementList = exprCtx.getTokens(BaracoLexer.DEC);

        return (decrementList.size() > 0);
    }

    public boolean isFunctionCall(ExpressionContext exprCtx) {
        return exprCtx.expression(0) != null && exprCtx != this.readRightHandExprCtx && EvaluationCommand.isFunctionCall(exprCtx);
    }

    public boolean isFunctionCallWithParams(ExpressionContext exprCtx) {
        ExpressionContext firstExprCtx = exprCtx.expression(0);

        if(firstExprCtx != null) {
            if(exprCtx != this.readRightHandExprCtx) {
                //ThisKeywordChecker thisChecker = new ThisKeywordChecker(firstExprCtx);
                //thisChecker.verify();

                return (exprCtx.expressionList() != null);
            }
        }

        return false;

    }

    private boolean isFunctionCallWithNoParams(ExpressionContext exprCtx) {
        //ThisKeywordChecker thisChecker = new ThisKeywordChecker(exprCtx);
        //thisChecker.verify();
        //if(exprCtx.Identifier() != null)
        return exprCtx.depth() == FUNCTION_CALL_NO_PARAMS_DEPTH || exprCtx.depth() == 17;

    }
}
