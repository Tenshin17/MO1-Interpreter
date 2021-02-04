package Execution.command.evaluation;

import antlr.Java8Parser.*;
import antlr.Java8Parser.ExpressionContext;
import error.CustomErrorStrategy;
import error.ParserHandler;
import Execution.ExecutionManager;
import Execution.command.ICommand;
import semantic.representation.JavaArray;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.representation.JavaValueSearch;
import semantic.searching.VariableSearcher;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import semantic.utils.Expression;
import semantic.utils.RecognizedKeywords;
import semantic.utils.StringUtils;
import semantic.utils.Expression.LazyNumber;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A command that evaluates a given expression at runtime.
 *
 */
public class EvaluationCommand implements ICommand, ParseTreeListener {

	private ExpressionContext parentExprCtx;
	private ConditionalExpressionContext parentCondCtx;
	private String modifiedExp;
	private BigDecimal resultValue;
	private String stringResult = "";
	private int lineNumber;

	private boolean isNumeric;
	public EvaluationCommand(ParserRuleContext exprCtx) {
		if(exprCtx instanceof ExpressionContext)
			this.parentExprCtx = (ExpressionContext) exprCtx;
		else if(exprCtx instanceof ConditionalExpressionContext)
			this.parentCondCtx = (ConditionalExpressionContext) exprCtx;
		this.lineNumber = exprCtx.getStart().getLine();
	}

	@Override
	public void execute() {

		if(parentExprCtx != null)
			this.modifiedExp = this.parentExprCtx.getText();
		else if(parentCondCtx != null)
			this.modifiedExp = this.parentCondCtx.getText();



		ParseTreeWalker treeWalker = new ParseTreeWalker();
		if(parentExprCtx != null)
			treeWalker.walk(this, this.parentExprCtx);
		else if(parentCondCtx != null)
			treeWalker.walk(this, this.parentCondCtx);

		isNumeric = !this.modifiedExp.contains("\"") && !this.modifiedExp.contains("\'");

		if (!isNumeric) {

			if (this.modifiedExp.contains("==") || this.modifiedExp.contains("!=")) {


				String[] strings = {"", ""};

				if (this.modifiedExp.contains("=="))
					strings = this.modifiedExp.split("==");

				if (this.modifiedExp.contains("!="))
					strings = this.modifiedExp.split("!=");

				String equalityFunction = "STREQ("+strings[0]+", " + strings[1] + ")";

				if (this.modifiedExp.contains("!="))
					equalityFunction = "not(" + equalityFunction + ")";

				Expression e = new Expression(equalityFunction);

				e.addLazyFunction(e.new LazyFunction("STREQ", 2) {

					private LazyNumber ZERO = new LazyNumber() {
						public BigDecimal eval() {
							return BigDecimal.ZERO;
						}
						public String getString() {
							return "0";
						}
					};

					private LazyNumber ONE = new LazyNumber() {
						public BigDecimal eval() {
							return BigDecimal.ONE;
						}
						public String getString() {
							return null;
						}
					};

					public LazyNumber lazyEval(List<LazyNumber> lazyParams) {
						if (lazyParams.get(0).getString().equals(lazyParams.get(1).getString())) {
							return ONE;
						}
						return ZERO;
					}
				});

				this.resultValue = e.eval();
				isNumeric = true;
			}
			else {
				this.stringResult = StringUtils.removeQuotes(this.modifiedExp);System.out.println(this.modifiedExp+" "+this.stringResult);
			}

		} else {

			if (this.modifiedExp.contains("!")) {
				this.modifiedExp = this.modifiedExp.replaceAll("!", "not");
				this.modifiedExp = this.modifiedExp.replaceAll("not=", "!=");
			}


			Expression evalEx = new Expression(this.modifiedExp);

			try {
				this.resultValue = evalEx.eval(false);
				this.stringResult = this.resultValue.toEngineeringString();
			} catch (Expression.ExpressionException ex) {
				this.resultValue = new BigDecimal(0);
				this.stringResult = "";
			} catch (ArithmeticException ex) {
				//StatementControlOverseer.getInstance().setCurrentCatchClause(IAttemptCommand.CatchTypeEnum.ARITHMETIC_EXCEPTION);

				ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Unknown Arithmetic Expression at"+this.parentExprCtx.getStart().getLine()));

				this.resultValue = new BigDecimal(0);
				this.stringResult = "";
			}

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
				this.evaluateFunctionCall(exprCtx);
			}
		}
		else if(ctx instanceof ExpressionNameContext) {
			ExpressionNameContext exprCtx = ((ExpressionNameContext) ctx);
			if(EvaluationCommand.isVariableOrConst(exprCtx)) {
				this.evaluateVariable(exprCtx);
			}

		}
		else if(ctx instanceof ArrayAccessContext) {
			ArrayAccessContext exprCtx = (ArrayAccessContext) ctx;
			if(EvaluationCommand.isArrayElement(exprCtx)) {
				this.evaluateArray(exprCtx);
			}
		}
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {

	}

	public static boolean isFunctionCall(MethodInvocationContext exprCtx) {
		Pattern functionPattern = Pattern.compile("([a-zA-Z0-9]+)\\(([ ,.a-zA-Z0-9]*)\\)");

		if (exprCtx.argumentList() != null || functionPattern.matcher(exprCtx.getText()).matches()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isVariableOrConst(ExpressionNameContext exprCtx) {
		return VariableSearcher.searchVariable(exprCtx.getText()) != null;
	}

	public static boolean isVariableOrConst(VariableDeclaratorIdContext exprCtx) {
		return VariableSearcher.searchVariable(exprCtx.getText()) != null;
	}

	public static boolean isArrayElement(ArrayAccessContext exprCtx) {
		if (exprCtx.expressionName() != null && exprCtx.expression(0) != null) {
			JavaValue value = JavaValueSearch.searchJavaValue(exprCtx.expressionName().Identifier().getText());

			if (value != null)
				return value.getPrimitiveType() == JavaValue.PrimitiveType.ARRAY;
			else
				return false;
		} else {
			return false;
		}
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

		System.out.println("EvaluationCommand: Before modified EXP function call: " + this.modifiedExp);
		if (javaMethod.getReturnType() == JavaMethod.FunctionType.STRING_TYPE) {
			this.modifiedExp = this.modifiedExp.replace(exprCtx.getText(),
					"\"" + javaMethod.getReturnValue().getValue().toString() + "\"");
		} else {
			this.modifiedExp = this.modifiedExp.replace(exprCtx.getText(),
					javaMethod.getReturnValue().getValue().toString());
		}
		System.out.println("EvaluationCommand: After modified EXP function call: " + this.modifiedExp);

	}

	private void evaluateVariable(ExpressionNameContext exprCtx) {
		JavaValue javaValue = VariableSearcher
				.searchVariable(exprCtx.getText());
		System.out.println(exprCtx.getText()+" "+javaValue.getValue().toString());
		this.modifiedExp = this.modifiedExp.replaceFirst(exprCtx.getText(),
				javaValue.getValue().toString());
	}

	public boolean checkFloatArray(){
		if(this.modifiedExp.contains(".")||(this.modifiedExp.endsWith("f")) ){
			int i = 0;
			System.out.println("I AM A FLOAT");
			String additionalMessage = "assigning non-integer to array size";
            ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + this.lineNumber + ": "
                    + "Oops! Type mismatch detected. " + additionalMessage));
			CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.TYPE_MISMATCH,additionalMessage,this.lineNumber);
			/*this.modifiedExp = this.modifiedExp.substring(0,this.modifiedExp.length()-1);
			System.out.println("new modified " + this.modifiedExp); */
			return false;
		}
		return true;
	}

    public void arrayExecute() {
        this.modifiedExp = this.parentExprCtx.getText();

        checkFloatArray();

        //catch rules if the value has direct boolean flags
        if(this.modifiedExp.contains(RecognizedKeywords.BOOLEAN_TRUE)) {
            this.resultValue = new BigDecimal(1);
        }
        else if(this.modifiedExp.contains(RecognizedKeywords.BOOLEAN_FALSE)) {
            this.resultValue = new BigDecimal(0);
        }
        else {
            ParseTreeWalker treeWalker = new ParseTreeWalker();
            treeWalker.walk(this, this.parentExprCtx);

			if(this.modifiedExp.contains("[") && this.modifiedExp.contains("]")) {
				String[] temp = this.modifiedExp.split("\\[");
				System.out.println(temp[0]);
				JavaValue javaValue = VariableSearcher.searchVariable(temp[0]);
				JavaArray javaArray = (JavaArray) javaValue.getValue();
				String index = temp[1].replaceFirst("\\]","");
				this.modifiedExp = javaArray.getValueAt(Integer.parseInt(index)).toString();
			}
            System.out.println("Modified exp to eval: " +this.modifiedExp);
            Expression evalEx = new Expression(this.modifiedExp);
            //Log.i(TAG,"Modified exp to eval: " +this.modifiedExp);

            this.resultValue = evalEx.eval();
        }

    }

	private void evaluateArray(ArrayAccessContext exprCtx) {
		JavaValue value = JavaValueSearch.searchJavaValue(exprCtx.expressionName().getText());

		if (value != null) {
			if (value.getPrimitiveType() == JavaValue.PrimitiveType.ARRAY) {

				JavaArray javaArray = (JavaArray) value.getValue();

				EvaluationCommand evCmd = new EvaluationCommand(exprCtx.expression(1));
				evCmd.execute();

				//ExecutionManager.getInstance().setCurrentCheckedLineNumber(exprCtx.getStart().getLine());
				JavaValue javaValue = javaArray.getValueAt(evCmd.getResult().intValue());

				if (javaValue == null)
					return;

				if (javaValue.getPrimitiveType() == JavaValue.PrimitiveType.STRING) {
					//this.modifiedExp = this.modifiedExp.replaceFirst(exprCtx.expression(0).getText() + "\\[([a-zA-Z0-9]*)]", "\"" + javaValue.getValue().toString() + "\"");
					this.modifiedExp = this.modifiedExp.replace(exprCtx.getText(), "\"" + javaValue.getValue().toString() + "\"");
				} else {
					//this.modifiedExp = this.modifiedExp.replaceFirst(exprCtx.expression(0).getText() + "\\[([a-zA-Z0-9]*)]", javaValue.getValue().toString());
					this.modifiedExp = this.modifiedExp.replace(exprCtx.getText(), javaValue.getValue().toString());
				}

			}
		}

	}
	
    /*
     * Returns the result
     */
    public BigDecimal getResult() {
        return this.resultValue;
    }

    public String getStringResult() {
        return stringResult;
    }

    public String getModifiedExp() { return modifiedExp;}

    public boolean isNumericResult() {
        return isNumeric;
    }
}