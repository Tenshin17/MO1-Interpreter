package baraco.execution.commands.evaluation;
//package EvalSimpCompExp;

import baraco.antlr.lexer.BaracoLexer;
import baraco.antlr.parser.BaracoParser;
import baraco.builder.errorcheckers.ConstChecker;
import baraco.builder.errorcheckers.TypeChecker;
import baraco.builder.errorcheckers.UndeclaredChecker;
import baraco.execution.ExecutionManager;
import baraco.execution.commands.EvaluationCommand;
import baraco.execution.commands.ICommand;
import baraco.representations.BaracoArray;
import baraco.representations.BaracoValue;
import baraco.semantics.analyzers.MethodCallVerifier;
import baraco.semantics.searching.VariableSearcher;
import baraco.semantics.utils.AssignmentUtils;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class ShortCom implements ICommand {

    private BaracoParser.ExpressionContext leftHandExprCtx;
    private BaracoParser.ExpressionContext rightHandExprCtx;
    int tokenSign;

    public ShorthandCommand(BaracoParser.ExpressionContext leftHandExprCtx,
                            BaracoParser.ExpressionContext rightHandExprCtx, int tokenSign) {
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
        BaracoValue baracoValue;
        if(ExecutionManager.getInstance().isInFunctionExecution()) {
            baracoValue = VariableSearcher.searchVariableInFunction(ExecutionManager.getInstance().getCurrentFunction(), this.leftHandExprCtx.getText());
        }
        else {
            baracoValue = VariableSearcher.searchVariable(this.leftHandExprCtx.getText());
        }

        TypeChecker typeChecker = new TypeChecker(baracoValue, this.rightHandExprCtx);
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
            BaracoValue baracoValue = VariableSearcher.searchVariable(this.leftHandExprCtx.getText());

            if (evaluationCommand.isNumericResult()) {

                if (!baracoValue.isFinal()) {
                    // Add checking for shorthand expressions
                    AssignmentUtils.assignAppropriateValue(baracoValue, evaluationCommand.getResult(), tokenSign);
                }

            } else {
                if (!baracoValue.isFinal()) {
                    if (this.tokenSign == BaracoLexer.ADD_ASSIGN) {
                        AssignmentUtils.addAssignAppropriateValue(baracoValue, evaluationCommand.getStringResult());
                    }
                    else {
                        AssignmentUtils.assignAppropriateValue(baracoValue, evaluationCommand.getStringResult());
                    }
                }
            }
        }
    }

    private boolean isLeftHandArrayAccessor() {
        List<TerminalNode> lBrackTokens = this.leftHandExprCtx.getTokens(BaracoLexer.LBRACK);
        List<TerminalNode> rBrackTokens = this.leftHandExprCtx.getTokens(BaracoLexer.RBRACK);

        return(lBrackTokens.size() > 0 && rBrackTokens.size() > 0);
    }

    private void handleArrayAssignment(String resultString) {
        TerminalNode identifierNode = this.leftHandExprCtx.expression(0).primary().Identifier();
        BaracoParser.ExpressionContext arrayIndexExprCtx = this.leftHandExprCtx.expression(1);

        BaracoValue baracoValue = VariableSearcher.searchVariable(identifierNode.getText());
        BaracoArray baracoArray = (BaracoArray) baracoValue.getValue();

        EvaluationCommand evaluationCommand = new EvaluationCommand(arrayIndexExprCtx);
        evaluationCommand.execute();

        ExecutionManager.getInstance().setCurrentCheckedLineNumber(identifierNode.getSymbol().getLine());

        //create a new array value to replace value at specified index
        BaracoValue newArrayValue = new BaracoValue(null, baracoArray.getPrimitiveType());
        newArrayValue.setValue(resultString);
        baracoArray.updateValueAt(newArrayValue, evaluationCommand.getResult().intValue());

        //Console.log("Index to access: " +evaluationCommand.getResult().intValue()+ " Updated with: " +resultString);
    }
}
