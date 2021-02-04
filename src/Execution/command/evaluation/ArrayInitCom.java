package Execution.command.evaluation;

import Execution.ExecutionManager;
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
	private DimExprContext dimExprContext;
	
	public ArrayInitCom(JavaArray javaArray, DimExprContext dimExprContext) {
		this.assignedJavaArray = javaArray;
		this.dimExprContext = dimExprContext;
	}
	@Override
	public void execute() {
		ExpressionContext exprCtx = dimExprContext.expression();
        System.out.println("another one");

		if(exprCtx != null) {
			EvaluationCommand evaluationCommand = new EvaluationCommand(exprCtx);
			System.out.println("ahhhhh");
			evaluationCommand.arrayExecute();

			this.assignedJavaArray.initializeSize(evaluationCommand.getResult().intValue());

		}

	}

}
