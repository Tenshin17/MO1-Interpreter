package Execution.command.evaluation;

import antlr.Java8Parser.DimExprContext;
import antlr.Java8Parser.ExpressionContext;
import Execution.command.ICommand;
import semantic.representation.JavaArray;

/**
 * Represents an initialization of an array using new int[x] for example.
 *
 */
public class ArrayInitCom implements ICommand {
	
	private JavaArray assignedJavaArray;
	private DimExprContext arrayCreatorCtx;
	
	public ArrayInitCom(JavaArray javaArray, DimExprContext arrayCreatorCtx) {
		assignedJavaArray = javaArray;
		this.arrayCreatorCtx = arrayCreatorCtx;
	}
	@Override
	public void execute() {
		ExpressionContext exprCtx = arrayCreatorCtx.expression();
        System.out.println("another one");

		if(exprCtx != null) {
			EvaluationCommand evaluationCommand = new EvaluationCommand(exprCtx);
			System.out.println("ahhhhh");
			evaluationCommand.arrayExecute();
			//if(evaluationCommand.checkFloatArray(exprCtx.getText())) {
                assignedJavaArray.initializeSize(evaluationCommand.getResult().intValue());
            //}

		}

	}

}
