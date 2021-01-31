package Command.controlled;

import antlr.Java8Parser;
import Java8.execution.ExecutionManager;
import Java8.execution.ExecutionMonitor;
import Command.ICommand;
import Command.ICondCommand;
import Java8.execution.commands.evaluation.AssignmentCommand;
import Java8.execution.commands.evaluation.MappingCommand;
import Java8.execution.commands.simple.IncDecCommand;
import Java8.execution.commands.simple.ReturnCommand;
import Java8.execution.commands.utils.ConditionEvaluator;
import VarAndConstDec.javaValue;
import Java8.semantics.mapping.IValueMapper;
import Java8.semantics.mapping.IdentifierMapper;
import Java8.semantics.searching.VariableSearcher;
import Java8.semantics.utils.LocalVarTracker;

import java.util.ArrayList;
import java.util.List;

public class IfCom implements ICondCommand {

    private List<ICommand> positiveCommands; //list of commands to execute if the condition holds true
    private List<ICommand> negativeCommands; //list of commands to execute if the condition holds false

    private Java8Parser.IfThenStatementContext conditionalExpr;
    private String modifiedConditionExpr;

    private boolean returned;

    private ArrayList<String> localVars = new ArrayList<>();

    public IfCom(Java8Parser.IfThenStatementContext conditionalExpr) {
        this.positiveCommands = new ArrayList<ICommand>();
        this.negativeCommands = new ArrayList<ICommand>();

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

        try {
            //execute the positive commands
            if (ConditionEvaluator.evaluateCondition(this.conditionalExpr)) {
                for (ICommand command : this.positiveCommands) {
                    executionMonitor.tryExecution();
                    command.execute();

                    LocalVarTracker.getInstance().populateLocalVars(command);

                    if (command instanceof ReturnCommand) {
                        returned = true;
                        break;
                    }

                    if (ExecutionManager.getInstance().isAborted())
                        break;
                }
            }
            //execute the negative commands
            else {
                for (ICommand command : this.negativeCommands) {
                    executionMonitor.tryExecution();
                    command.execute();

                    LocalVarTracker.getInstance().populateLocalVars(command);

                    if (command instanceof ReturnCommand) {
                        returned = true;
                        break;
                    }
                    if (ExecutionManager.getInstance().isAborted())
                        break;
                }
            }
        } catch (InterruptedException e) {
            //Log.e(TAG, "Monitor block interrupted! " +e.getMessage());
            System.out.println("Monitor block interrupted! " + e.getMessage());
        }

    }

    private void identifyVariables() {
        IValueMapper identifierMapper = new IdentifierMapper(this.conditionalExpr.getText());
        identifierMapper.analyze(this.conditionalExpr);

        this.modifiedConditionExpr = identifierMapper.getModifiedExp();
    }

    @Override
    public IControlledCommand.ControlTypeEnum getControlType() {
        return IControlledCommand.ControlTypeEnum.CONDITIONAL_IF;
    }

    @Override
    public void addPositiveCommand(ICommand command) {
        this.positiveCommands.add(command);
    }

    @Override
    public void addNegativeCommand(ICommand command) {
        this.negativeCommands.add(command);
    }

    public boolean isReturned() {
        return returned;
    }

    public void resetReturnFlag() {
       returned = false;
    }

    public void clearAllCommands() {
        this.positiveCommands.clear();
        this.negativeCommands.clear();
    }

    public int getPositiveCommandsCount() {
        return this.positiveCommands.size();
    }

    public int getNegativeCommandsCount() {
        return this.negativeCommands.size();

    }

    public ArrayList<String> getLocalVars() {
        return localVars;
    }
}
