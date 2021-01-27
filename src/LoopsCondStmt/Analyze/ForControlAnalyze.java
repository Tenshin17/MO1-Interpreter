package baraco.semantics.analyzers;
//package LoopsCondStmt.Analyze;

import baraco.antlr.lexer.BaracoLexer;
import baraco.antlr.parser.BaracoParser;
import baraco.antlr.parser.BaracoParser.ExpressionContext;
import baraco.antlr.parser.BaracoParser.ForUpdateContext;
import baraco.antlr.parser.BaracoParser.LocalVariableDeclarationContext;
import baraco.execution.commands.ICommand;
import baraco.execution.commands.evaluation.AssignmentCommand;
import baraco.execution.commands.evaluation.ShorthandCommand;
import baraco.execution.commands.simple.IncDecCommand;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ForControlAnalyze implements ParseTreeListener {

    private BaracoParser.LocalVariableDeclarationContext localVarDecCtx;
    private BaracoParser.ExpressionContext exprCtx;
    private ICommand updateCommand;

    public ForControlAnalyzer() {

    }

    public void analyze(BaracoParser.ForControlContext forControlCtx) {

        //we don't need to walk the expression anymore, therefore, immediately assign it.
        if(forControlCtx.expression() != null) {
            this.exprCtx = forControlCtx.expression();
        }

        ParseTreeWalker treeWalker = new ParseTreeWalker();
        treeWalker.walk(this, forControlCtx);
    }

    public void analyzeForLoop(ParserRuleContext ctx) {

        if(ctx instanceof BaracoParser.ForInitContext) {
            BaracoParser.ForInitContext forInitCtx = (BaracoParser.ForInitContext) ctx;

            this.localVarDecCtx = forInitCtx.localVariableDeclaration();

            LocalVariableAnalyzer localVariableAnalyzer = new LocalVariableAnalyzer();
            localVariableAnalyzer.analyze(this.localVarDecCtx);
        }

        else if(ctx instanceof ForUpdateContext) {
            ForUpdateContext forUpdateCtx = (ForUpdateContext) ctx;
            ExpressionContext exprCtx = forUpdateCtx.expressionList().expression(0);

            if(StatementExpressionAnalyzer.isAssignmentExpression(exprCtx)) {
                //this.updateCommand = new AssignmentCommand(exprCtx.expression(0), exprCtx.expression(1));
                this.updateCommand = new AssignmentCommand(exprCtx.expression(0), exprCtx.expression(1));
            }
            else if(StatementExpressionAnalyzer.isAddAssignExpression(exprCtx)) {
                this.updateCommand = new ShorthandCommand(exprCtx.expression(0), exprCtx.expression(1), BaracoLexer.ADD_ASSIGN);
            }
            else if(StatementExpressionAnalyzer.isSubAssignExpression(exprCtx)) {
                this.updateCommand = new ShorthandCommand(exprCtx.expression(0), exprCtx.expression(1), BaracoLexer.SUB_ASSIGN);
            }
            else if(StatementExpressionAnalyzer.isMulAssignExpression(exprCtx)) {
                this.updateCommand = new ShorthandCommand(exprCtx.expression(0), exprCtx.expression(1), BaracoLexer.MUL_ASSIGN);
            }
            else if(StatementExpressionAnalyzer.isDivAssignExpression(exprCtx)) {
                this.updateCommand = new ShorthandCommand(exprCtx.expression(0), exprCtx.expression(1), BaracoLexer.DIV_ASSIGN);
            }
            else if(StatementExpressionAnalyzer.isModAssignExpression(exprCtx)) {
                this.updateCommand = new ShorthandCommand(exprCtx.expression(0), exprCtx.expression(1), BaracoLexer.MOD_ASSIGN);
            }
            else if(StatementExpressionAnalyzer.isIncrementExpression(exprCtx)) {
                this.updateCommand = new IncDecCommand(exprCtx.expression(0), BaracoLexer.INC);
            }
            else if(StatementExpressionAnalyzer.isDecrementExpression(exprCtx)) {
                this.updateCommand = new IncDecCommand(exprCtx.expression(0), BaracoLexer.DEC);
            }
        }
    }

    public ExpressionContext getExprContext() {
        return this.exprCtx;
    }

    public LocalVariableDeclarationContext getLocalVarDecContext() {
        return this.localVarDecCtx;
    }

    public ICommand getUpdateCommand() {
        return this.updateCommand;
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
        this.analyzeForLoop(ctx);
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        // TODO Auto-generated method stub

    }
}
