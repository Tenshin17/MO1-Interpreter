package baraco.execution.commands.evaluation;
//package EvalSimpCompExp;

import baraco.antlr.parser.BaracoParser;
import baraco.builder.errorcheckers.UndeclaredChecker;
import baraco.execution.commands.EvaluationCommand;
import baraco.execution.commands.ICommand;
import baraco.representations.BaracoValue;
import baraco.semantics.analyzers.MethodCallVerifier;
import baraco.semantics.searching.VariableSearcher;
import baraco.semantics.utils.AssignmentUtils;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class MapCom implements ICommand{

    private String identifierString;
    private BaracoParser.ExpressionContext parentExprCtx;

    private String modifiedExp;

    public MappingCommand(String identifierString, BaracoParser.ExpressionContext exprCtx) {
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

        BaracoValue baracoValue = VariableSearcher.searchVariable(this.identifierString);

        if (evaluationCommand.isNumericResult())
            AssignmentUtils.assignAppropriateValue(baracoValue, evaluationCommand.getResult());
        else
            AssignmentUtils.assignAppropriateValue(baracoValue, evaluationCommand.getStringResult());
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

    public BaracoParser.ExpressionContext getParentExprCtx() {
        return parentExprCtx;
    }
}
