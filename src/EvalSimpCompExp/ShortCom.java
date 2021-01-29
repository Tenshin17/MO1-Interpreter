package EvalSimpCompExp;

import antlr.Java8Lexer;
import antlr.Java8Parser;
import java.builder.errorcheckers.ConstChecker;
import java.builder.errorcheckers.TypeChecker;
import java.builder.errorcheckers.UndeclaredChecker;
import java.execution.ExecutionManager;
import java.execution.commands.EvaluationCommand;
import Command.ICommand;
import VarAndConstDec.javaArray;
import VarAndConstDec.javaValue;
import java.semantics.analyzers.MethodCallVerifier;
import java.semantics.searching.VariableSearcher;
import java.semantics.utils.AssignmentUtils;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class ShortCom implements ICommand {

    private Java8Parser.ExpressionContext leftHandExprCtx;
    private Java8Parser.ExpressionContext rightHandExprCtx;
    int tokenSign;

    public ShorthandCommand(Java8Parser.ExpressionContext leftHandExprCtx,
                            Java8Parser.ExpressionContext rightHandExprCtx, int tokenSign) {
        this.leftHandExprCtx = leftHandExprCtx;
        this.rightHandExprCtx = rightHandExprCtx;
        this.tokenSign = tokenSign;

        UndeclaredChecker undeclaredChecker = new UndeclaredChecker(this.leftHandExprCtx);
        undeclaredChecker.verify();

        ConstChecker constChecker = new ConstChecker(this.leftHandExprCtx);
        constChecker.verify();

        undeclaredChecker = new UndeclaredChecker(this.rightHandExprCtx);
        undeclaredChecker.verify();

        ParseTreeWalker functionWalker = new ParseTreeWalker();
        functionWalker.walk(new MethodCallVerifier(), this.rightHandExprCtx);

        //type check the mobivalue
        javaValue javaValue;
        if(ExecutionManager.getInstance().isInFunctionExecution()) {
            javaValue = VariableSearcher.searchVariableInFunction(ExecutionManager.getInstance().getCurrentFunction(), this.leftHandExprCtx.getText());
        }
        else {
            javaValue = VariableSearcher.searchVariable(this.leftHandExprCtx.getText());
        }

        TypeChecker typeChecker = new TypeChecker(javaValue, this.rightHandExprCtx);
        typeChecker.verify();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.neildg.mobiprog.execution.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        EvaluationCommand evaluationCommand = new EvaluationCommand(this.rightHandExprCtx);
        evaluationCommand.execute();

        if (evaluationCommand.hasException())
            return;

        if(this.isLeftHandArrayAccessor()) {
            this.handleArrayAssignment(evaluationCommand.getResult().toEngineeringString());
        }
        else {
            javaValue javaValue = VariableSearcher.searchVariable(this.leftHandExprCtx.getText());

            if (evaluationCommand.isNumericResult()) {

                if (!javaValue.isFinal()) {
                    // Add checking for shorthand expressions
                    AssignmentUtils.assignAppropriateValue(javaValue, evaluationCommand.getResult(), tokenSign);
                }

            } else {
                if (!javaValue.isFinal()) {
                    if (this.tokenSign == Java8Lexer.ADD_ASSIGN) {
                        AssignmentUtils.addAssignAppropriateValue(javaValue, evaluationCommand.getStringResult());
                    }
                    else {
                        AssignmentUtils.assignAppropriateValue(javaValue, evaluationCommand.getStringResult());
                    }
                }
            }
        }
    }

    private boolean isLeftHandArrayAccessor() {
        List<TerminalNode> lBrackTokens = this.leftHandExprCtx.getTokens(Java8Lexer.LBRACK);
        List<TerminalNode> rBrackTokens = this.leftHandExprCtx.getTokens(Java8Lexer.RBRACK);

        return(lBrackTokens.size() > 0 && rBrackTokens.size() > 0);
    }

    private void handleArrayAssignment(String resultString) {
        TerminalNode identifierNode = this.leftHandExprCtx.expression(0).primary().Identifier();
        Java8Parser.ExpressionContext arrayIndexExprCtx = this.leftHandExprCtx.expression(1);

        javaValue javaValue = VariableSearcher.searchVariable(identifierNode.getText());
        javaArray javaArray = (javaArray) javaValue.getValue();

        EvaluationCommand evaluationCommand = new EvaluationCommand(arrayIndexExprCtx);
        evaluationCommand.execute();

        ExecutionManager.getInstance().setCurrentCheckedLineNumber(identifierNode.getSymbol().getLine());

        //create a new array value to replace value at specified index
        javaValue newArrayValue = new javaValue(null, javaArray.getPrimitiveType());
        newArrayValue.setValue(resultString);
        javaArray.updateValueAt(newArrayValue, evaluationCommand.getResult().intValue());

        //Console.log("Index to access: " +evaluationCommand.getResult().intValue()+ " Updated with: " +resultString);
    }
}
