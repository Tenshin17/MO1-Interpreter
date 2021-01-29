package PrintScan
import baraco.antlr.lexer.BaracoLexer;
import baraco.builder.errorcheckers.ConstChecker;
import baraco.builder.errorcheckers.UndeclaredChecker;
import baraco.execution.commands.ICommand;
import baraco.antlr.parser.BaracoParser.*;
import baraco.representations.BaracoValue;
import baraco.semantics.mapping.IValueMapper;
import baraco.semantics.mapping.IdentifierMapper;

public class IncDecCom implements ICommand {

    private ExpressionContext exprCtx;
    private int tokenSign;

    public IncDecCommand(ExpressionContext exprCtx, int tokenSign) {
        this.exprCtx = exprCtx;
        this.tokenSign = tokenSign;

        ConstChecker constChecker = new ConstChecker(this.exprCtx);
        constChecker.verify();

        UndeclaredChecker undeclaredChecker = new UndeclaredChecker(this.exprCtx);
        undeclaredChecker.verify();
    }

    /* (non-Javadoc)
     * @see com.neildg.mobiprog.execution.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        //String identifier = this.exprCtx.primary().Identifier().getText();
        //MobiValue mobiValue = MobiValueSearcher.searchMobiValue(identifier);

        IValueMapper leftHandMapper = new IdentifierMapper(
                this.exprCtx.getText());
        leftHandMapper.analyze(this.exprCtx);

        BaracoValue baracoValue = leftHandMapper.getBaracoValue();

        if(!baracoValue.isFinal())
            this.performOperation(baracoValue);
    }

    /*
     * Attempts to perform an increment/decrement operation
     */
    private void performOperation(BaracoValue baracoValue) {
        if(baracoValue.getPrimitiveType() == BaracoValue.PrimitiveType.INT) {
            int value = Integer.valueOf(baracoValue.getValue().toString());

            if(this.tokenSign == BaracoLexer.INC) {
                value++;
                baracoValue.setValue(String.valueOf(value));
            }
            else if(this.tokenSign == BaracoLexer.DEC) {
                value--;
                baracoValue.setValue(String.valueOf(value));
            }
        }
        else if(baracoValue.getPrimitiveType() == BaracoValue.PrimitiveType.DECIMAL) {
            float value = Float.valueOf(baracoValue.getValue().toString());

            if(this.tokenSign == BaracoLexer.INC) {
                value++;
                baracoValue.setValue(String.valueOf(value));
            }
            else if(this.tokenSign == BaracoLexer.DEC) {
                value--;
                baracoValue.setValue(String.valueOf(value));
            }
        }
    }

    public String getIdentifierString() {
        return exprCtx.primary().Identifier().getText();
    }
}
