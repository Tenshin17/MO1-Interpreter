package SimpleCom;

import antlr.Java8Parser.*;
import Execution.ExecutionManager;
import Execution.command.ICommand;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Populates and handles the print command Execution
 *
 */
public class PrintCom implements ICommand, ParseTreeListener {

    private PrintStatementContext expressionCtx;

    private String statementToPrint = "";
    private boolean complexExpr = false;
    private boolean arrayAccess = false;

    public PrintCom(PrintStatementContext expressionCtx) {
        this.expressionCtx = expressionCtx;

        //UndeclaredChecker undeclaredChecker = new UndeclaredChecker(this.expressionCtx);
        //undeclaredChecker.verify();
    }

    @Override
    public void execute() {
        ParseTreeWalker treeWalker = new ParseTreeWalker();
        treeWalker.walk(this, expressionCtx);
        ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatProgram(statementToPrint));
        System.out.println(statementToPrint);
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
            LiteralContext literalCtx = (LiteralContext) ctx;

            if(literalCtx.StringLiteral() != null) {
                String quotedString = literalCtx.StringLiteral().getText();
                statementToPrint += StringUtils.removeQuotes(quotedString);
            }
            else if(literalCtx.IntegerLiteral() != null) {
                int value = Integer.parseInt(literalCtx.IntegerLiteral().getText());
                this.statementToPrint += value;
            }

            else if(literalCtx.FloatingPointLiteral() != null) {
                float value = Float.parseFloat(literalCtx.FloatingPointLiteral().getText());
                this.statementToPrint += value;
            }

            else if(literalCtx.BooleanLiteral() != null) {
                this.statementToPrint += literalCtx.BooleanLiteral().getText();
            }

            else if(literalCtx.CharacterLiteral() != null) {
                this.statementToPrint += literalCtx.CharacterLiteral().getText();
            }
        }

        else if(ctx instanceof PrintExtensionContext) {
            PrintExtensionContext primaryCtx = (PrintExtensionContext) ctx;

            if(primaryCtx != null) {
                PrintExtensionContext exprCtx = primaryCtx;
                complexExpr = true;
                ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Complex expression detected: " + exprCtx.getText()));


                statementToPrint += primaryCtx.Identifier().getText();
            }
            /*
            else if(primaryCtx.Identifier() != null && !complexExpr) {
                String identifier = primaryCtx.getText();

                JavaValue value = JavaValueSearch.searchJavaValue(identifier);
                if(value != null) {
                    if (value.getPrimitiveType() == PrimitiveType.ARRAY) {
                        arrayAccess = true;
                        evaluateArrayPrint(value, primaryCtx);
                    } else if (!arrayAccess) {
                        statementToPrint += value.getValue();
                    }
                } else{
                    CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.UNDECLARED_VARIABLE,
                            primaryCtx.getText(), primaryCtx.getStart().getLine());
                }
            }
            */
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {

    }

    public String getStatementToPrint() {
        return statementToPrint;
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
