package EvalSimpCompExp;
//package EvalSimpCompExp;

import antlr.Java8Parser;
import java.builder.BuildChecker;
import java.builder.ErrorRepository;
import java.execution.ExecutionManager;
import java.execution.commands.EvaluationCommand;
import Command.ICommand;
import VarAndConstDec.javaArray;
import org.antlr.v4.runtime.Token;

public class ArrayInitCom implements ICommand {

    private javaArray assignedjavaArray;
    private Java8Parser.ArrayCreatorRestContext arrayCreatorCtx;

    public ArrayInitializeCommand(javaArray javaArray, Java8Parser.ArrayCreatorRestContext arrayCreatorCtx) {
        this.assignedjavaArray = javaArray;
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
        Java8Parser.ExpressionContext exprCtx = this.arrayCreatorCtx.expression(0);

        if(exprCtx != null) {
            EvaluationCommand evaluationCommand = new EvaluationCommand(exprCtx);
            evaluationCommand.execute();

            ExecutionManager.getInstance().setCurrentCheckedLineNumber(exprCtx.getStart().getLine());
            this.assignedjavaArray.initializeSize(evaluationCommand.getResult().intValue());
        }

    }
}
