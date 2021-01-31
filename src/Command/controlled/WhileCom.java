package Command.controlled;

import baraco.antlr.parser.BaracoParser;
import baraco.execution.ExecutionManager;
import baraco.execution.ExecutionMonitor;
import baraco.execution.commands.ICommand;
import baraco.execution.commands.simple.ScanCommand;
import baraco.execution.commands.utils.ConditionEvaluator;
import baraco.representations.BaracoValueSearcher;
import baraco.semantics.mapping.IValueMapper;
import baraco.semantics.mapping.IdentifierMapper;
import baraco.semantics.utils.LocalVarTracker;

import java.util.ArrayList;
import java.util.List;

public class WhileCom implements IControlledCommand {

    protected List<ICommand> commandSequences; //the list of commands inside the WHILE statement

    protected BaracoParser.ParExpressionContext conditionalExpr;
    protected String modifiedConditionExpr;

    private boolean lastLineFlag = false;

    private ArrayList<String> localVars = new ArrayList<>();

    public WhileCom(BaracoParser.ParExpressionContext conditionalExpr) {
        this.commandSequences = new ArrayList<ICommand>();
        this.conditionalExpr = conditionalExpr;
    }

    /*
     * Executes the command
     * (non-Javadoc)
     * @see com.neildg.mobiprog.execution.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        this.identifyVariables();

        ExecutionMonitor executionMonitor = ExecutionManager.getInstance().getExecutionMonitor();

        LocalVarTracker.resetLocalVars(localVars);

        try {
            //evaluate the given condition
            while(ConditionEvaluator.evaluateCondition(this.modifiedConditionExpr)) {
                for(ICommand command : this.commandSequences) {
                    executionMonitor.tryExecution();
                    command.execute();

                    LocalVarTracker.getInstance().populateLocalVars(command);

                    if (ExecutionManager.getInstance().isAborted())
                        break;
                }

                if (ExecutionManager.getInstance().isAborted())
                    break;

                executionMonitor.tryExecution();
                this.identifyVariables(); //identify variables again to detect changes to such variables used.
            }

        } catch(InterruptedException e) {
            //Log.e(TAG, "Monitor block interrupted! " +e.getMessage());
            System.out.println("WhileCom: Monitor block interrupted " + e.getMessage());
        }
    }

    protected void identifyVariables() {
        IValueMapper identifierMapper = new IdentifierMapper(this.conditionalExpr.getText());
        identifierMapper.analyze(this.conditionalExpr);

        this.modifiedConditionExpr = identifierMapper.getModifiedExp();
    }

    @Override
    public ControlTypeEnum getControlType() {
        return ControlTypeEnum.WHILE_CONTROL;
    }

    @Override
    public void addCommand(ICommand command) {

        //Console.log(LogType.DEBUG, "		Added command to WHILE");
        this.commandSequences.add(command);
    }

    public int getCommandCount() {
        return this.commandSequences.size();
    }

    public ArrayList<String> getLocalVars() {
        return localVars;
    }
}
