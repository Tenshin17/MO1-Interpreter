package LoopsCondStmt.Analyze;

import baraco.antlr.parser.BaracoParser;
import baraco.builder.ParserHandler;
import baraco.builder.errorcheckers.ParameterMismatchChecker;
import baraco.execution.commands.EvaluationCommand;
import baraco.representations.BaracoMethod;
import baraco.semantics.symboltable.SymbolTableManager;
import baraco.semantics.symboltable.scopes.ClassScope;

import antlr.Java8Parser;
import VarAndConstDec.javaMethod;
import symboltable.SymbolTableManager;
import symboltable.scope.ClassScope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

public class MethodCallVerify implements ParseTreeListener {

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
        if(ctx Java8Parser Java8Parser.ExpressionContext) {
            BaracoParser.ExpressionContext exprCtx = (Java8Parser.ExpressionContext) ctx;
            if (EvaluationCommand.isFunctionCall(exprCtx)) {
                if(exprCtx.expression(0) == null)
                    return;

                String functionName = exprCtx.expression(0).getText();

                ClassScope classScope = SymbolTableManager.getInstance().getClassScope(
                        ParserHandler.getInstance().getCurrentClassName());
                javaMethod barac = classScope.searchMethod(functionName);

                if (exprCtx.arguments() != null) {
                    ParameterMismatchChecker paramsMismatchChecker = new ParameterMismatchChecker(barac, exprCtx.arguments());
                    paramsMismatchChecker.verify();
                }
            }
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        // TODO Auto-generated method stub

    }
}
