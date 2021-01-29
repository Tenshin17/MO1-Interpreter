package java.execution.commands.evaluation;
//package EvalSimpCompExp;

import antlr.Java8Parser;
import java.builder.errorcheckers.UndeclaredChecker;
import java.execution.commands.EvaluationCommand;
import Command.ICommand;
import VarAndConstDec.javaValue;
import java.semantics.analyzers.MethodCallVerifier;
import java.semantics.searching.VariableSearcher;
import java.semantics.utils.AssignmentUtils;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class MapCom implements ICommand{

    private String identifierString;
    private Java8Parser.ExpressionContext parentExprCtx;

    private String modifiedExp;

    public MappingCommand(String identifierString, Java8Parser.ExpressionContext exprCtx) {
        this.identifierString = identifierString;
        this.parentExprCtx = exprCtx;

        UndeclaredChecker undeclaredChecker = new UndeclaredChecker(this.parentExprCtx);
        undeclaredChecker.verify();

        ParseTreeWalker functionWalker = new ParseTreeWalker();
        functionWalker.walk(new MethodCallVerifier(), this.parentExprCtx);

    }


    /* (non-Javadoc)
     * @see com.neildg.mobiprog.execution.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        this.modifiedExp = this.parentExprCtx.getText();

        EvaluationCommand evaluationCommand = new EvaluationCommand(this.parentExprCtx);
        evaluationCommand.execute();

        if (evaluationCommand.hasException())
            return;

        javaValue javaValue = VariableSearcher.searchVariable(this.identifierString);

        if (evaluationCommand.isNumericResult())
            AssignmentUtils.assignAppropriateValue(javaValue, evaluationCommand.getResult());
        else
            AssignmentUtils.assignAppropriateValue(javaValue, evaluationCommand.getStringResult());
    }

    /*
     * Returns the modified exp, with mapped values.
     */
    public String getModifiedExp() {
        return this.modifiedExp;
    }

    public String getIdentifierString() {
        return identifierString;
    }

    public Java8Parser.ExpressionContext getParentExprCtx() {
        return parentExprCtx;
    }
}
