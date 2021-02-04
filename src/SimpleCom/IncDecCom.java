package SimpleCom;

import Execution.command.ICommand;
import antlr.Java8Lexer;
import antlr.Java8Parser.PostfixExpressionContext;
import semantic.mapping.IValueMapper;
import semantic.mapping.IdentifierMapper;
import semantic.representation.JavaValue;
import semantic.representation.JavaValueSearch;

/**
 * An increment or decrement command
 *
 */
public class IncDecCom implements ICommand {

    private PostfixExpressionContext exprCtx;
    private int tokenSign;

    public IncDecCom(PostfixExpressionContext exprCtx, int tokenSign) {
        this.exprCtx = exprCtx;
        this.tokenSign = tokenSign;
    }

    @Override
    public void execute() {
        String identifier = this.exprCtx.expressionName().Identifier().getText();
        JavaValue javaValue = JavaValueSearch.searchJavaValue(identifier);

        /*
        IValueMapper leftHandMapper = new IdentifierMapper(
                this.exprCtx.getText());
        leftHandMapper.analyze(this.exprCtx);

        JavaValue javaValue = leftHandMapper.getJavaValue();
        */
        this.performOperation(javaValue);
    }

    /*
     * Attempts to perform an increment/decrement operation
     */
    private void performOperation(JavaValue javaValue) {
        switch (javaValue.getPrimitiveType()) {
            case INT: {
                int value = Integer.valueOf(javaValue.getValue().toString());

                if (this.tokenSign == Java8Lexer.INC) {
                    value++;
                    javaValue.setValue(String.valueOf(value));
                } else if (this.tokenSign == Java8Lexer.DEC) {
                    value--;
                    javaValue.setValue(String.valueOf(value));
                }
                break;
            }
            case LONG: {
                long value = Long.valueOf(javaValue.getValue().toString());

                if (this.tokenSign == Java8Lexer.INC) {
                    value++;
                    javaValue.setValue(String.valueOf(value));
                } else if (this.tokenSign == Java8Lexer.DEC) {
                    value--;
                    javaValue.setValue(String.valueOf(value));
                }
                break;
            }
            case BYTE: {
                byte value = Byte.valueOf(javaValue.getValue().toString());

                if (this.tokenSign == Java8Lexer.INC) {
                    value++;
                    javaValue.setValue(String.valueOf(value));
                } else if (this.tokenSign == Java8Lexer.DEC) {
                    value--;
                    javaValue.setValue(String.valueOf(value));
                }
                break;
            }
            case SHORT: {
                short value = Short.valueOf(javaValue.getValue().toString());

                if (this.tokenSign == Java8Lexer.INC) {
                    value++;
                    javaValue.setValue(String.valueOf(value));
                } else if (this.tokenSign == Java8Lexer.DEC) {
                    value--;
                    javaValue.setValue(String.valueOf(value));
                }
                break;
            }
            case FLOAT: {
                float value = Float.valueOf(javaValue.getValue().toString());

                if (this.tokenSign == Java8Lexer.INC) {
                    value++;
                    javaValue.setValue(String.valueOf(value));
                } else if (this.tokenSign == Java8Lexer.DEC) {
                    value--;
                    javaValue.setValue(String.valueOf(value));
                }
                break;
            }
            case DOUBLE: {
                double value = Double.valueOf(javaValue.getValue().toString());

                if (this.tokenSign == Java8Lexer.INC) {
                    value++;
                    javaValue.setValue(String.valueOf(value));
                } else if (this.tokenSign == Java8Lexer.DEC) {
                    value--;
                    javaValue.setValue(String.valueOf(value));
                }
                break;
            }
        }
    }

}
