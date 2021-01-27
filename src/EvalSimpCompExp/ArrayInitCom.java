package baraco.execution.commands.evaluation;
//package EvalSimpCompExp;

import baraco.antlr.parser.BaracoParser;
import baraco.builder.BuildChecker;
import baraco.builder.ErrorRepository;
import baraco.execution.ExecutionManager;
import baraco.execution.commands.EvaluationCommand;
import baraco.execution.commands.ICommand;
import baraco.representations.BaracoArray;
import org.antlr.v4.runtime.Token;

public class ArrayInitCom implements ICommand {

    private BaracoArray assignedBaracoArray;
    private BaracoParser.ArrayCreatorRestContext arrayCreatorCtx;

    public ArrayInitializeCommand(BaracoArray baracoArray, BaracoParser.ArrayCreatorRestContext arrayCreatorCtx) {
        this.assignedBaracoArray = baracoArray;
        this.arrayCreatorCtx = arrayCreatorCtx;

        if (arrayCreatorCtx.expression(0) != null) {
            if (arrayCreatorCtx.expression(0).getText().contains("\"") || arrayCreatorCtx.expression(0).getText().contains(".")) {
                Token firstToken = this.arrayCreatorCtx.getStart();
                int lineNumber = firstToken.getLine();

                BuildChecker.reportCustomError(ErrorRepository.INVALID_INDEX_ASSIGN, "", lineNumber);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.neildg.mobiprog.execution.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        BaracoParser.ExpressionContext exprCtx = this.arrayCreatorCtx.expression(0);

        if(exprCtx != null) {
            EvaluationCommand evaluationCommand = new EvaluationCommand(exprCtx);
            evaluationCommand.execute();

            ExecutionManager.getInstance().setCurrentCheckedLineNumber(exprCtx.getStart().getLine());
            this.assignedBaracoArray.initializeSize(evaluationCommand.getResult().intValue());
        }

    }
}
