package baraco.execution.commands.simple;
//package PrintScan

import baraco.builder.ParserHandler;
import baraco.execution.commands.EvaluationCommand;
import baraco.execution.commands.ICommand;
import baraco.antlr.parser.BaracoParser.*;
import baraco.representations.BaracoMethod;
import baraco.representations.BaracoValue;
import baraco.semantics.analyzers.MethodCallVerifier;
import baraco.semantics.searching.VariableSearcher;
import baraco.semantics.symboltable.SymbolTableManager;
import baraco.semantics.symboltable.scopes.ClassScope;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;

public class MethodCallCom implements ICommand {
    private final static String TAG = "MobiProg_FunctionCallCommand";

    private BaracoMethod baracoMethod;
    private ExpressionContext exprCtx;
    private String functionName;

    public MethodCallCommand(String functionName, ExpressionContext exprCtx) {
        this.functionName = functionName;
        this.exprCtx = exprCtx;

        System.out.println("SEARCH FOR " + functionName);

        this.searchFunction();

        ParseTreeWalker functionWalker = new ParseTreeWalker();
        functionWalker.walk(new MethodCallVerifier(), this.exprCtx);

        this.verifyParameters();
    }

    /* (non-Javadoc)
     * @see com.neildg.mobiprog.execution.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        this.mapParameters();
        this.baracoMethod.execute();
    }

    private void searchFunction() {
        ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
        this.baracoMethod = classScope.searchMethod(this.functionName);
    }

    private void verifyParameters() {
        if(this.exprCtx.arguments() == null || this.exprCtx.arguments().expressionList() == null
                || this.exprCtx.arguments().expressionList().expression() == null) {
            return;
        }

        List<ExpressionContext> exprCtxList = this.exprCtx.arguments().expressionList().expression();
        //map values in parameters
        for(int i = 0; i < exprCtxList.size(); i++) {
            ExpressionContext parameterExprCtx = exprCtxList.get(i);
            this.baracoMethod.verifyParameterByValueAt(parameterExprCtx, i);
        }
    }

    /*
     * Maps parameters when needed
     */
    private void mapParameters() {
        System.out.println("MethodCallCommand: mapping parameters");
        //System.out.println("exprctx: " + this.exprCtx.expressionList().getText());
        /*if(this.exprCtx.arguments() == null || this.exprCtx.arguments().expressionList() == null
                || this.exprCtx.arguments().expressionList().expression() == null) {
            return;
        }*/

        if (this.exprCtx.expressionList() == null) {
            return;
        }

        List<ExpressionContext> exprCtxList = this.exprCtx.expressionList().expression();

        //map values in parameters
        for(int i = 0; i < exprCtxList.size(); i++) {
            ExpressionContext parameterExprCtx = exprCtxList.get(i);

            if(this.baracoMethod.getParameterAt(i).getPrimitiveType() == BaracoValue.PrimitiveType.ARRAY) {
                BaracoValue baracoValue = VariableSearcher.searchVariable(parameterExprCtx.getText());
                this.baracoMethod.mapArrayAt(baracoValue, i, parameterExprCtx.getText());
            }
            else {
                EvaluationCommand evaluationCommand = new EvaluationCommand(parameterExprCtx);
                evaluationCommand.execute();

                if (evaluationCommand.isNumericResult()) {
                    this.baracoMethod.mapParameterByValueAt(evaluationCommand.getResult().toEngineeringString(), i);
                }
                else {
                    this.baracoMethod.mapParameterByValueAt(evaluationCommand.getStringResult(), i);
                }
            }
        }
    }

    public BaracoValue getReturnValue() {
        return this.baracoMethod.getReturnValue();
    }

}
