package Execution.command.evaluation;

import antlr.Java8Parser.ExpressionContext;
import error.checkers.UndeclaredChecker;
import Execution.command.ICommand;
import semantic.analyzers.FunctionCallVerifier;
import semantic.representation.JavaValue;
import semantic.searching.VariableSearcher;
import semantic.utils.AssignmentUtils;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * A mapping command that evaluates a given expression context then maps
 * its corresponding value. Has an identifier string that assigns the value to it.
 * This is different from assignment command. This one is used for any variable initialization.
 *
 */
public class MappingCommand implements ICommand {
	
	private String identifierString;
	private ExpressionContext parentExprCtx;
	
	private String modifiedExp;
	
	public MappingCommand(String identifierString, ExpressionContext exprCtx) {
		this.identifierString = identifierString;
		this.parentExprCtx = exprCtx;
		
		UndeclaredChecker undeclaredChecker = new UndeclaredChecker(this.parentExprCtx);
		undeclaredChecker.verify();
		
		ParseTreeWalker functionWalker = new ParseTreeWalker();
		functionWalker.walk(new FunctionCallVerifier(), this.parentExprCtx);
		
	}

	@Override
	public void execute() {
		this.modifiedExp = parentExprCtx.getText();
		
		EvaluationCommand evaluationCommand = new EvaluationCommand(this.parentExprCtx);
		evaluationCommand.execute();
		
		JavaValue javaValue = VariableSearcher.searchVariable(this.identifierString);
		if(evaluationCommand.isNumericResult())
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
}
