package SimpleCom;

import antlr.Java8Parser.*;
import Execution.ExecutionManager;
import Execution.command.ICommand;
import error.CustomErrorStrategy;
import semantic.representation.JavaArray;
import semantic.representation.JavaValue;
import semantic.representation.JavaValueSearch;
import semantic.searching.VariableSearcher;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

/**
 * Populates and handles the print command Execution
 *
 */
public class PrintCom implements ICommand, ParseTreeListener {

    private PrintStatementContext expressionCtx;

    private String statementToPrint = "";
    private boolean isLN = false;
    private boolean complexExpr = false;
    private boolean arrayAccess = false;

    public PrintCom(PrintStatementContext expressionCtx) {
        isLN = expressionCtx.PRINTLN() != null;
        this.expressionCtx = expressionCtx;

        //UndeclaredChecker undeclaredChecker = new UndeclaredChecker(this.expressionCtx);
        //undeclaredChecker.verify();
    }

    @Override
    public void execute() {
        ParseTreeWalker treeWalker = new ParseTreeWalker();
        treeWalker.walk(this, expressionCtx);

        if(isLN)
            statementToPrint += "\n";

        ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatProgram(statementToPrint));
        //System.out.println(statementToPrint);
        statementToPrint = ""; //reset statement to print afterwards
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitErrorNode(ErrorNode node) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        if(ctx instanceof LiteralContext) {

        }

        else if(ctx instanceof PrintExpressionContext) {
            List<PrintExtensionContext> primaryCtx = ((PrintExpressionContext) ctx).printExtension();

            for(PrintExtensionContext exprCtx : primaryCtx) {
                complexExpr = true;
                //ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Complex expression detected: " + exprCtx.getText()));

                if(exprCtx.literal() != null) {
                    LiteralContext literalCtx = exprCtx.literal();

                    if (literalCtx.StringLiteral() != null) {
                        String quotedString = literalCtx.StringLiteral().getText();
                        statementToPrint += StringUtils.removeQuotes(quotedString);
                    } else if (literalCtx.IntegerLiteral() != null) {
                        int value = Integer.parseInt(literalCtx.IntegerLiteral().getText());
                        this.statementToPrint += value;
                    } else if (literalCtx.FloatingPointLiteral() != null) {
                        float value = Float.parseFloat(literalCtx.FloatingPointLiteral().getText());
                        this.statementToPrint += value;
                    } else if (literalCtx.BooleanLiteral() != null) {
                        this.statementToPrint += literalCtx.BooleanLiteral().getText();
                    } else if (literalCtx.CharacterLiteral() != null) {
                        this.statementToPrint += literalCtx.CharacterLiteral().getText();
                    }
                }
                else if(exprCtx.Identifier() != null){
                    JavaValue javaValue = VariableSearcher.searchVariable(exprCtx.Identifier().getText());
                    if(javaValue != null) {
                        if(javaValue.getPrimitiveType() == JavaValue.PrimitiveType.ARRAY) {
                            JavaArray javaArray = (JavaArray) javaValue.getValue();

                        }
                        else {
                            statementToPrint += javaValue.getValue().toString();
                        }
                    }
                    else {
                        CustomErrorStrategy.reportSemanticError(1,exprCtx.getText(),exprCtx.getStart().getLine());
                    }
                }
                else if(exprCtx.arrayAccess() != null) {
                    JavaValue javaValue = VariableSearcher.searchVariable(exprCtx.arrayAccess().expressionName().Identifier().getText());
                    if(javaValue != null) {
                        JavaArray javaArray = (JavaArray) javaValue.getValue();
                        JavaValue arrElement = javaArray.getValueAt(Integer.parseInt(exprCtx.arrayAccess().expression(0).getText()));
                        statementToPrint += arrElement.getValue().toString();
                    }
                    else {
                        CustomErrorStrategy.reportSemanticError(1,exprCtx.arrayAccess().expressionName().Identifier().getText(),exprCtx.getStart().getLine());
                    }
                }

            }
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {

    }

    public String getStatementToPrint() {
        return this.statementToPrint;
    }
    /*
    private void evaluateArrayPrint(JavaValue javaValue, PrintExtensionContext primaryCtx) {

        //move up and determine expression contexts
        ExpressionContext parentExprCtx = (ExpressionContext) primaryCtx.getParent().getParent();
        ExpressionContext arrayIndexExprCtx = parentExprCtx.(1);

        EvaluationCommand evaluationCommand = new EvaluationCommand(arrayIndexExprCtx);
        evaluationCommand.execute();

        JavaArray javaArray = (JavaArray) javaValue.getValue();
        JavaValue arrayJavaValue = javaArray.getValueAt(evaluationCommand.getResult().intValue());

        statementToPrint += arrayJavaValue.getValue().toString();
    }
    */


}
