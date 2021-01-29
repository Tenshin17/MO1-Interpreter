package PrintScan;

import baraco.builder.BuildChecker;
import baraco.builder.ErrorRepository;
import baraco.builder.errorcheckers.TypeChecker;
import baraco.builder.errorcheckers.UndeclaredChecker;
import baraco.execution.commands.EvaluationCommand;
import baraco.execution.commands.ICommand;
import baraco.antlr.parser.BaracoParser.*;
import baraco.representations.BaracoMethod;
import baraco.representations.BaracoValue;
import baraco.semantics.utils.AssignmentUtils;
import org.antlr.v4.runtime.Token;

public class ReturnCom implements ICommand {
    private final static String TAG = "MobiProg_ReturnCommand";

    private ExpressionContext expressionCtx;
    private BaracoMethod assignedBaracoMethod;

    public ReturnCommand(ExpressionContext expressionCtx, BaracoMethod baracoMethod) {
        this.expressionCtx = expressionCtx;
        this.assignedBaracoMethod = baracoMethod;

        UndeclaredChecker undeclaredChecker = new UndeclaredChecker(this.expressionCtx);
        undeclaredChecker.verify();

        BaracoValue baracoValue = this.assignedBaracoMethod.getReturnValue();

        if (baracoValue == null) {
            Token firstToken = this.expressionCtx.getStart();
            int lineNumber = firstToken.getLine();

            BuildChecker.reportCustomError(ErrorRepository.RETURN_IN_VOID, "", lineNumber);
        }

        TypeChecker typeChecker = new TypeChecker(baracoValue, this.expressionCtx);
        typeChecker.verify();
    }

    /* (non-Javadoc)
     * @see com.neildg.mobiprog.execution.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        EvaluationCommand evaluationCommand = new EvaluationCommand(this.expressionCtx);
        evaluationCommand.execute();

        BaracoValue baracoValue = this.assignedBaracoMethod.getReturnValue();

        if (evaluationCommand.isNumericResult())
            AssignmentUtils.assignAppropriateValue(baracoValue, evaluationCommand.getResult());
        else
            AssignmentUtils.assignAppropriateValue(baracoValue, evaluationCommand.getStringResult());
        //Console.log(LogType.DEBUG,"Return value is: " +evaluationCommand.getResult().toEngineeringString());
    }

}
