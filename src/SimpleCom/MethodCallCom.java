package SimpleCom;

import antlr.Java8Parser.*;
import error.ParserHandler;
import Execution.command.ICommand;
import Execution.command.evaluation.EvaluationCommand;
import antlr.Java8Parser.ExpressionContext;
import semantic.analyzers.FunctionCallVerifier;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.representation.JavaValue.PrimitiveType;
import semantic.searching.VariableSearcher;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;

/**
 * Represents a function call command
 *
 */
public class MethodCallCom implements ICommand {

    private JavaMethod javaMethod;
    private MethodInvocationContext exprCtx;
    private String functionName;

    public MethodCallCom(String functionName, MethodInvocationContext exprCtx) {
        this.functionName = functionName;
        this.exprCtx = exprCtx;

        searchMethod();

        ParseTreeWalker functionWalker = new ParseTreeWalker();
        functionWalker.walk(new FunctionCallVerifier(), this.exprCtx);

        verifyParameters();
    }

    @Override
    public void execute() {
        mapParameters();
        javaMethod.execute();
    }

    private void searchMethod() {
        ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
        this.javaMethod = new JavaMethod(classScope.searchMethod(functionName));
    }

    private void verifyParameters() {
        if(exprCtx.argumentList() == null || exprCtx.argumentList().expression() == null) {
            return;
        }

        List<ExpressionContext> exprCtxList = exprCtx.argumentList().expression();
        //map values in parameters
        for(int i = 0; i < exprCtxList.size(); i++) {
            ExpressionContext parameterExprCtx = exprCtxList.get(i);
            javaMethod.verifyParameterByValueAt(parameterExprCtx, i);
        }
    }

    /*
     * Maps parameters when needed
     */
    private void mapParameters() {
        if(exprCtx.argumentList() == null || exprCtx.argumentList().expression() == null) {
            return;
        }

        List<ExpressionContext> exprCtxList = exprCtx.argumentList().expression();

        //map values in parameters
        for(int i = 0; i < exprCtxList.size(); i++) {
            ExpressionContext parameterExprCtx = exprCtxList.get(i);

            if(this.javaMethod.getParameterAt(i).getPrimitiveType() == PrimitiveType.ARRAY) {
                JavaValue javaValue = VariableSearcher.searchVariable(parameterExprCtx.getText());
                javaMethod.mapArrayAt(javaValue, i, parameterExprCtx.getText());
            }
            else {
                EvaluationCommand evaluationCommand = new EvaluationCommand(parameterExprCtx);
                evaluationCommand.execute();

                javaMethod.mapParameterByValueAt(evaluationCommand.getResult().toEngineeringString(), i);
            }
        }
    }

    public JavaValue getReturnValue() {
        return javaMethod.getReturnValue();
    }

}