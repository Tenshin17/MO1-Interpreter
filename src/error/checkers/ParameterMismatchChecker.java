/**
 * 
 */
package error.checkers;

import antlr.Java8Parser.ArgumentListContext;
import antlr.Java8Parser.ExpressionContext;
import error.CustomErrorStrategy;
import semantic.representation.JavaMethod;

import java.util.List;

/**
 * Just checks if the number of parameters to be passed is equal to the required number

 *
 */
public class ParameterMismatchChecker implements IErrorChecker {
	
	private JavaMethod javaMethod;
	private List<ExpressionContext> exprCtxList;
	private int lineNumber;
	
	public ParameterMismatchChecker(JavaMethod javaMethod, ArgumentListContext argumentsCtx) {
		this.javaMethod = javaMethod;

		if(argumentsCtx != null) {
			exprCtxList = argumentsCtx.expression();
		}
		
		lineNumber = argumentsCtx.getStart().getLine();
	}

	@Override
	public void verify() {
		if(javaMethod == null) {
			return;
		}
		
		if(exprCtxList == null && javaMethod.getParameterValueSize() != 0) {
			CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.PARAMETER_COUNT_MISMATCH, javaMethod.getFunctionName(), lineNumber);
		}
		else if(exprCtxList != null && exprCtxList.size() != javaMethod.getParameterValueSize()) {
			CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.PARAMETER_COUNT_MISMATCH, javaMethod.getFunctionName(), lineNumber);
		}
	}

}
