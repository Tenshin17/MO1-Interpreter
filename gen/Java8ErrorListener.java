import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Java8ErrorListener extends BaseErrorListener {
    public int lastError = -1;

    public List<String> Errors = new ArrayList<>();
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


        /*
        if (thisError > lastError + 10) {
            lastError = thisError - 10;
        }
        for (int idx = lastError + 1; idx <= thisError; idx++) {
            Token token = tokens.get(idx);
            if (token.getChannel() != Token.HIDDEN_CHANNEL)
                System.err.println(token.toString());
        }
        lastError = thisError;
        */

        offSymbol = tokens.get(thisError-1);

        //List<String> stack = ((Parser)recognizer).getRuleInvocationStack();
        //Collections.reverse(stack);
        //System.err.println("rule stack: "+stack);
        //System.err.println("ERROR: at line "+line+":"+charPositionInLine+" : "+msg);
        String ErrorMessage = "";
        String errmsgstmt = msg.split("'")[0].toString();
        String errmsgsymbol = msg.split("'")[1].toString();
        String erroffsymbol = offendingSymbol.toString().split("'")[1];
        //String errmsgsymbol = offSymbol.toString().split("'")[1];
//        System.out.println("line "+line);
//        System.out.println("charpos "+charPositionInLine);
//        System.out.println("errmsgstmt "+errmsgstmt);
//        System.out.println("errmsgsymbol"+errmsgsymbol);
//        System.out.println("ERROR: at line "+line+":"+charPositionInLine+" : "+msg);
//        System.out.println("ERROR: at line "+line+":"+charPositionInLine+" : "+msg);
//        System.out.println("errstmt bool "+errmsgstmt.contains("missing"));
//        System.out.println("message.contaitns "+ msg.contains("missing"));
        System.out.println("");
        if(errmsgstmt.contains("missing") || errmsgstmt.contains("cannot find symbol"))//
        {

//            System.out.println("\t\tMISSING");
            ErrorMessage = "[MISSING SYMBOL] At line: "+ line + " Character Position: " + charPositionInLine + " Possible missing symbol: \"" + errmsgsymbol +"\"";
        }
        else if(errmsgstmt.contains("mismatched input"))
        {

//            System.out.println("\t\tMISMATCHED");
            ErrorMessage = "[UNEXPECTED SYMBOL] At line: " + line +  " Character Position: " + charPositionInLine + " unexpected symbol: \"" + errmsgsymbol +"\"";
        }
        else if(errmsgstmt.contains("extraneous input"))
        {
//            System.out.println("ERROR: at line "+line+":"+charPositionInLine+" : "+msg + "OFFENDING SYMBOL :" +erroffsymbol);
//            System.out.println("\t\tEXTRANEOUS");
            errmsgsymbol = offSymbol.toString().split("'")[1];
            ErrorMessage = "[EXTRA SYMBOL] At line: " + line +  " Character Position: " + charPositionInLine + " extra symbol: \"" + errmsgsymbol +"\"";

         }
        else if(errmsgstmt.contains("no viable alternative"))
        {

//            System.out.println("ERROR: at line "+line+":"+charPositionInLine+" : "+msg + "OFFENDING SYMBOL :" +erroffsymbol);
//            System.out.println("\t\tNO ALTERNATIVE");
            erroffsymbol = offSymbol.toString().split("'")[1];
            ErrorMessage = "[SUGGESTION] At line: " + line + "  consider inserting a symbol or changing the symbol \"" + erroffsymbol +"\"";
        }
        else
        {
            ErrorMessage = "[OTHERS] At line: " + line +  " Character Position: " + charPositionInLine + "  consider adding \"" + errmsgsymbol +"\"";
        }


        Errors.add(ErrorMessage);
//        Errors.add("ERROR: at line "+line+":"+charPositionInLine+" : "+msg);
    }

    public List<String> getErrors() {
        return Errors;
    }
}