package Execution.command.utils;

import antlr.Java8Parser.*;
import com.udojava.evalex.Expression;
import Execution.command.evaluation.EvaluationCommand;

import java.math.BigDecimal;


public class CondEval {

    public static boolean evaluateCondition(ConditionalExpressionContext condExprCtx) {

        //catch rules if the if value has direct boolean flags
        if(condExprCtx.getText().contains("true")) {
            return true;
        }
        else if(condExprCtx.getText().contains("false")) {
            return false;
        }
        EvaluationCommand evaluationCommand = new EvaluationCommand(condExprCtx);
        evaluationCommand.execute();

        int result = evaluationCommand.getResult().intValue();

        System.out.println("Evaluating: " +condExprCtx.getText() + " Result: " +result);

        return (result == 1);
    }

    public static boolean evaluateCondition(ExpressionContext conditionExprCtx) {

        //catch rules if the if value has direct boolean flags
        if(conditionExprCtx.getText().contains("(true)")) {
            return true;
        }
        else if(conditionExprCtx.getText().contains("(false)")) {
            return false;
        }

        EvaluationCommand evaluationCommand = new EvaluationCommand(conditionExprCtx);
        evaluationCommand.execute();

        int result = evaluationCommand.getResult().intValue();

        System.out.println("Evaluating: " +conditionExprCtx.getText() + " Result: " +result);

        return (result == 1);
    }

}