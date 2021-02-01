package error;

import antlr.Java8Parser;
import Execution.ExecutionManager;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.IntervalSet;

/**
 * Created by jasonsapdos on 15/11/2017.
 */
public class CustomErrorStrategy extends DefaultErrorStrategy {

    public static final int TYPE_MISMATCH = 0;
    public static final int UNDECLARED_VARIABLE = 1;
    public static final int UNDECLARED_FUNCTION = 2;
    public static final int CONST_REASSIGNMENT = 3;
    public static final int MULTIPLE_VARIABLE = 4;
    public static final int MULTIPLE_FUNCTION = 5;
    public static final int PARAMETER_COUNT_MISMATCH = 6;
    public static final int CONST_INTDEC = 7;

    @Override
    public void recover(Parser recognizer, RecognitionException e) {

        super.recover(recognizer, e);
        CommonTokenStream tokens = (CommonTokenStream) recognizer.getTokenStream();
        // verify current token is not EOF
        if (tokens.LA(1) != Java8Parser.EOF) {
            // move to next token
            tokens.consume();
        }
    }

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
        if(!this.inErrorRecoveryMode(recognizer)) {
            this.beginErrorCondition(recognizer);
            if(e instanceof NoViableAltException) {
                this.reportNoViableAlternative(recognizer, (NoViableAltException)e);
            } else if(e instanceof InputMismatchException) {
                this.reportInputMismatch(recognizer, (InputMismatchException)e);
            } else if(e instanceof FailedPredicateException) {
                this.reportFailedPredicate(recognizer, (FailedPredicateException)e);
            } else {
                ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Oops! Unknown recognition error type found: " + e.getClass().getName()));
                recognizer.notifyErrorListeners(e.getOffendingToken(), e.getMessage(), e);
            }

        }
    }

    public static void reportSemanticError(int errorType, String message, int lineNum){
        switch(errorType){
            case TYPE_MISMATCH: ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + lineNum + ": "
                    + "Oops! Type mismatch detected. " + message)); break;
            case UNDECLARED_VARIABLE: ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + lineNum + ": "
                    + "Oops! Undeclared variable " + message + " detected.")); break;
            case UNDECLARED_FUNCTION: ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + lineNum + ": "
                    + "Oops! Undeclared function " + message + " detected.")); break;
            case CONST_REASSIGNMENT: ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + lineNum + ": "
                    + "Oops! Reassignment of new value cannot be done on constant " + message + ".")); break;
            case MULTIPLE_VARIABLE: ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + lineNum + ": "
                    + "Oops! Multiple declaration of variable " + message + " detected.")); break;
            case MULTIPLE_FUNCTION: ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + lineNum + ": "
                    + "Oops! Multiple declaration of function " + message + " detected.")); break;
            case PARAMETER_COUNT_MISMATCH: ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + lineNum + ": "
                    + "Oops! Number of arguments for function call " + message + " do not match with original declaration.")); break;
            case CONST_INTDEC: ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + lineNum + ": "
                    + "Oops! Constant " + message + " cannot be incremented or decremented.")); break;
        }
        ExecutionManager.hasErrors = true;
    }

    @Override
    protected void reportNoViableAlternative(Parser recognizer, NoViableAltException e) {
        TokenStream tokens = recognizer.getInputStream();
        String input;
        if(tokens != null) {
            if(e.getStartToken().getType() == -1) {
                input = "<EOF>";
            } else {
                input = tokens.getText(e.getStartToken(), e.getOffendingToken());
            }
        } else {
            input = "<unknown input>";
        }

        String msg = "Oops! Input text cannot be parsed: " + input;
        recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
    }

    @Override
    protected void reportInputMismatch(Parser recognizer, InputMismatchException e) {
        String msg = "Oops! Input mismatch found for " + this.getTokenErrorDisplay(e.getOffendingToken()) + ", expecting " + e.getExpectedTokens().toString(recognizer.getVocabulary());
        recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
    }

    @Override
    protected void reportUnwantedToken(Parser recognizer) {
        if(!this.inErrorRecoveryMode(recognizer)) {
            this.beginErrorCondition(recognizer);
            Token t = recognizer.getCurrentToken();
            String tokenName = this.getTokenErrorDisplay(t);
            IntervalSet expecting = this.getExpectedTokens(recognizer);
            String msg = "Oops! An unwanted " + tokenName + " found, expecting " + expecting.toString(recognizer.getVocabulary());
            recognizer.notifyErrorListeners(t, msg, null);
        }
    }

    @Override
    protected void reportMissingToken(Parser recognizer) {
        if(!this.inErrorRecoveryMode(recognizer)) {
            this.beginErrorCondition(recognizer);
            Token t = recognizer.getCurrentToken();
            IntervalSet expecting = this.getExpectedTokens(recognizer);
            String msg = "Oops! We cannot see expected " + expecting.toString(recognizer.getVocabulary()) + " at " + this.getTokenErrorDisplay(t);
            recognizer.notifyErrorListeners(t, msg, null);
        }
    }
}
