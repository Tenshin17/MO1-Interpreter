package Execution.command.evaluation;

import antlr.Java8Parser.*;
import antlr.Java8Parser.ExpressionContext;
import error.CustomErrorStrategy;
import error.ParserHandler;
import Execution.ExecutionManager;
import Execution.command.ICommand;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.searching.VariableSearcher;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import semantic.utils.Expression;
import semantic.utils.RecognizedKeywords;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.util.List;

/**
 * A command that evaluates a given expression at runtime.
 *
 */
public class EvaluationCommand implements ICommand, ParseTreeListener {

	private ExpressionContext parentExprCtx;
	private ConditionalExpressionContext parentCondCtx;
	private String modifiedExp;
	private BigDecimal resultValue;
	private int lineNumber;

	public EvaluationCommand(ExpressionContext exprCtx) {
		parentExprCtx = exprCtx;
		lineNumber = exprCtx.getStart().getLine();
	}

	public EvaluationCommand(ConditionalExpressionContext exprCtx) {
		parentCondCtx = exprCtx;
		lineNumber = exprCtx.getStart().getLine();
	}

	@Override
	public void execute() {
		if(parentCondCtx != null) {
			modifiedExp = parentCondCtx.getText();
		}
		else {
			modifiedExp = parentExprCtx.getText();
		}

		//catch rules if the value has direct boolean flags
		if(modifiedExp.contains(RecognizedKeywords.BOOLEAN_TRUE)) {
			resultValue = new BigDecimal(1);
		}
		else if(modifiedExp.contains(RecognizedKeywords.BOOLEAN_FALSE)) {
			resultValue = new BigDecimal(0);
		}
		else {
			ParseTreeWalker treeWalker = new ParseTreeWalker();
			treeWalker.walk(this, parentExprCtx);

			Expression evalEx = new Expression(modifiedExp);
			//Log.i(TAG,"Modified exp to eval: " +this.modifiedExp);
			resultValue = evalEx.eval();
		}

	}

	@Override
	public void visitTerminal(TerminalNode node) {

	}

	@Override
	public void visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		if(ctx instanceof MethodInvocationContext) {
			MethodInvocationContext exprCtx = (MethodInvocationContext) ctx;
			if (EvaluationCommand.isFunctionCall(exprCtx)) {
				evaluateFunctionCall(exprCtx);
			}
		}
		else if(ctx instanceof VariableDeclaratorIdContext) {
			VariableDeclaratorIdContext exprCtx = (VariableDeclaratorIdContext) ctx;
			if(EvaluationCommand.isVariableOrConst(exprCtx)) {
				evaluateVariable(exprCtx);
			}
		}
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {

	}

	public static boolean isFunctionCall(MethodInvocationContext exprCtx) {
		return exprCtx.argumentList() != null;
	}

	public static boolean isVariableOrConst(VariableDeclaratorIdContext exprCtx) {
		return VariableSearcher.searchVariable(exprCtx.getText()) != null;
	}

	private void evaluateFunctionCall(MethodInvocationContext exprCtx) {
		String functionName = exprCtx.methodName().Identifier().getText();

		ClassScope classScope = SymbolTableManager.getInstance().getClassScope(
				ParserHandler.getInstance().getCurrentClassName());
		JavaMethod javaMethod = classScope.searchMethod(functionName);

		if (exprCtx.argumentList().expression() != null) {
			List<ExpressionContext> exprCtxList = exprCtx.argumentList().expression();

			for (int i = 0; i < exprCtxList.size(); i++) {
				ExpressionContext parameterExprCtx = exprCtxList.get(i);

				EvaluationCommand evaluationCommand = new EvaluationCommand(parameterExprCtx);
				evaluationCommand.execute();

				javaMethod.mapParameterByValueAt(evaluationCommand.getResult().toEngineeringString(), i);
			}
		}

		javaMethod.execute();

		System.out.println("EvaluationCommand: Before modified EXP function call: " + modifiedExp);
		modifiedExp = modifiedExp.replace(exprCtx.getText(),
				javaMethod.getReturnValue().getValue().toString());
		System.out.println("EvaluationCommand: After modified EXP function call: " + modifiedExp);

	}

	private void evaluateVariable(VariableDeclaratorIdContext exprCtx) {
		JavaValue javaValue = VariableSearcher
				.searchVariable(exprCtx.getText());

		modifiedExp = modifiedExp.replaceFirst(exprCtx.getText(),
				javaValue.getValue().toString());
	}

	public boolean checkFloatArray(){
		if(modifiedExp.contains(".")||(modifiedExp.endsWith("f")) ){
			int i = 0;
			System.out.println("I AM A FLOAT");
			String additionalMessage = "assigning non-integer to array size";
            ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + lineNumber + ": "
                    + "Oops! Type mismatch detected. " + additionalMessage));
			CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.TYPE_MISMATCH,additionalMessage,lineNumber);
			/*this.modifiedExp = this.modifiedExp.substring(0,this.modifiedExp.length()-1);
			System.out.println("new modified " + this.modifiedExp); */
			return false;
		}
		return true;
	}

    public void arrayExecute() {
        modifiedExp = parentExprCtx.getText();

        checkFloatArray();

        //catch rules if the value has direct boolean flags
        if(modifiedExp.contains(RecognizedKeywords.BOOLEAN_TRUE)) {
            resultValue = new BigDecimal(1);
        }
        else if(modifiedExp.contains(RecognizedKeywords.BOOLEAN_FALSE)) {
            resultValue = new BigDecimal(0);
        }
        else {
            ParseTreeWalker treeWalker = new ParseTreeWalker();
            treeWalker.walk(this, parentExprCtx);


            System.out.println("Modified exp to eval: " +this.modifiedExp);
            Expression evalEx = new Expression(modifiedExp);
            //Log.i(TAG,"Modified exp to eval: " +this.modifiedExp);

            resultValue = evalEx.eval();
        }

    }

	/*
	 * Returns the result
	 */
	public BigDecimal getResult() {
		return resultValue;
	}
}