package baraco.execution.commands.simple;
//package PrintScan

import baraco.antlr.parser.BaracoParser;
import baraco.builder.ParserHandler;
import baraco.execution.ExecutionManager;
import baraco.execution.ExecutionMonitor;
import baraco.execution.commands.EvaluationCommand;
import baraco.execution.commands.ICommand;
import baraco.execution.commands.evaluation.MappingCommand;
import baraco.representations.BaracoArray;
import baraco.representations.BaracoValue;
import baraco.representations.BaracoValueSearcher;
import baraco.semantics.searching.VariableSearcher;
import baraco.semantics.symboltable.SymbolTableManager;
import baraco.semantics.symboltable.scopes.ClassScope;
import baraco.semantics.utils.StringUtils;
import baraco.utils.notifications.*;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ScanCom implements ICommand, NotificationListener {

    //private final static String TAG = "MobiProg_ScanCommand";

    private String messageToDisplay;
    private String identifier;
    private BaracoParser.ExpressionContext array;

    public ScanCommand(String messageToDisplay, String identifier) {
        this.messageToDisplay = StringUtils.removeQuotes(messageToDisplay);
        this.identifier = identifier;

    }

    public ScanCommand(String messageToDisplay, BaracoParser.ExpressionContext array, String identifier) {
        this.messageToDisplay = StringUtils.removeQuotes(messageToDisplay);
        this.array = array;
        this.identifier = identifier;
    }

    @Override
    public void execute() {
        System.out.println("Found scan statement");
        NotificationCenter.getInstance().addObserver(Notifications.ON_SCAN_DIALOG_DISMISSED, this); //add an observer to listen to when the dialog has been dismissed

        Parameters params = new Parameters();
        params.putExtra(KeyNames.MESSAGE_DISPLAY_KEY, this.messageToDisplay);

        ExecutionManager.getInstance().blockExecution();

        NotificationCenter.getInstance().postNotification(Notifications.ON_FOUND_SCAN_STATEMENT, params);
    }

    private void acquireInputFromUser(Parameters params) {
        String valueEntered = params.getStringExtra(KeyNames.VALUE_ENTERED_KEY, "");

        boolean success;

        if(this.array == null) {
            BaracoValue baracoValue = BaracoValueSearcher.searchBaracoValue(identifier);
            //insert if array here
            try {
                baracoValue.setValue(valueEntered);
                success = true;
            } catch (NumberFormatException ex) {
                success = false;
                NotificationCenter.getInstance().removeObserver(Notifications.ON_SCAN_DIALOG_DISMISSED, this); //remove observer after using
                this.execute();

            }
        }
        else {
            handleArrayAssignment(valueEntered);
            success = true;
        }

        if(success) {
            NotificationCenter.getInstance().removeObserver(Notifications.ON_SCAN_DIALOG_DISMISSED, this); //remove observer after using
            ExecutionManager.getInstance().resumeExecution(); //resume execution of thread
        }

    }

    @Override
    public void onNotify(String notificationString, Parameters params) {
        if(notificationString == Notifications.ON_SCAN_DIALOG_DISMISSED) {
            this.acquireInputFromUser(params);
        }
    }

    private void handleArrayAssignment(String resultString) {
        BaracoParser.ExpressionContext arrayIndexExprCtx = this.array;

        BaracoValue baracoValue = VariableSearcher.searchVariable(this.identifier);
        BaracoArray baracoArray = (BaracoArray) baracoValue.getValue();

        EvaluationCommand evaluationCommand = new EvaluationCommand(arrayIndexExprCtx);
        evaluationCommand.execute();

        //create a new array value to replace value at specified index
        BaracoValue newArrayValue = new BaracoValue(null, baracoArray.getPrimitiveType());
        newArrayValue.setValue(resultString);
        baracoArray.updateValueAt(newArrayValue, evaluationCommand.getResult().intValue());

        //Console.log("Index to access: " +evaluationCommand.getResult().intValue()+ " Updated with: " +resultString);
    }
}
