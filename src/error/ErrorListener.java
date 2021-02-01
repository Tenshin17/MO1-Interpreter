package error;

import Execution.ExecutionManager;
import Execution.command.utils.StringUtils;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class ErrorListener extends BaseErrorListener {

    private ArrayList<Integer> errorPositionList = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg,
                            RecognitionException e)
    {

        ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Line " + line + ": " + msg));
        underlineError(recognizer, (Token)offendingSymbol, line, charPositionInLine);

    }

    protected void underlineError(Recognizer recognizer,
                                  Token offendingToken, int line,
                                  int charPositionInLine) {

        int start = offendingToken.getStartIndex();
        int stop = offendingToken.getStopIndex();

        // Add line, start, and stop positions to array
        errorPositionList.add(line-1);
        errorPositionList.add(start);
        errorPositionList.add(stop);

    }

    public ArrayList<Integer> getErrorPositionList() {
        return errorPositionList;
    }

}