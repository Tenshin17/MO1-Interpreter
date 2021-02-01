package SimpleCom;

import error.checkers.TypeChecker;
import error.checkers.UndeclaredChecker;
import Execution.command.ICommand;
import Execution.command.evaluation.EvaluationCommand;
import antlr.Java8Parser.ExpressionContext;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.utils.AssignmentUtils;

public class ReturnCom implements ICommand {

    private ExpressionContext expressionCtx;
    private JavaMethod assignedJavaMethod;

    public ReturnCom(ExpressionContext expressionCtx, JavaMethod javaMethod) {
        this.expressionCtx = expressionCtx;
        assignedJavaMethod = javaMethod;

        UndeclaredChecker undeclaredChecker = new UndeclaredChecker(this.expressionCtx);
        undeclaredChecker.verify();

        JavaValue javaValue = assignedJavaMethod.getReturnValue();
        TypeChecker typeChecker = new TypeChecker(javaValue, this.expressionCtx);
        typeChecker.verify();
    }

    @Override
    public void execute() {
        EvaluationCommand evaluationCommand = new EvaluationCommand(expressionCtx);
        evaluationCommand.execute();

        AssignmentUtils.assignAppropriateValue(assignedJavaMethod.getReturnValue(), evaluationCommand.getResult());
        //Console.log(LogType.DEBUG,"Return value is: " +evaluationCommand.getResult().toEngineeringString());
    }

}
