import org.antlr.v4.runtime.*;
import sun.plugin2.util.SystemUtil;

import java.util.Collections;
import java.util.List;

public class Java8ErrorListener extends BaseErrorListener {
    public int lastError = -1;
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg,
                            RecognitionException e)
    {
        Parser parser = (Parser) recognizer;
        String name = parser.getSourceName();
        TokenStream tokens = parser.getInputStream();

        Token offSymbol = (Token) offendingSymbol;
        int thisError = offSymbol.getTokenIndex();
        if (offSymbol.getType() == -1 && thisError == tokens.size() - 1) {
            System.err.println(name + ": Incorrect error: " + msg);
            return;
        }
        String offSymName = Java8Lexer.VOCABULARY.getSymbolicName(offSymbol.getType());

        List<String> stack = parser.getRuleInvocationStack();
        // Collections.reverse(stack);



        if (thisError > lastError + 10) {
            lastError = thisError - 10;
        }
        for (int idx = lastError + 1; idx <= thisError; idx++) {
            Token token = tokens.get(idx);
            if (token.getChannel() != Token.HIDDEN_CHANNEL) System.err.println(token.toString());
        }
        lastError = thisError;

        //List<String> stack = ((Parser)recognizer).getRuleInvocationStack();
        //Collections.reverse(stack);
        //System.err.println("rule stack: "+stack);
        System.err.println("ERROR: line "+line+":"+charPositionInLine+" at "+
                offendingSymbol+": "+msg);
    }
}