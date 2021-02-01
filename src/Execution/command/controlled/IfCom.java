package Execution.command.controlled;

import Execution.ExecutionManager;
import Execution.ExecutionMonitor;
import Execution.command.ICommand;
import Execution.command.ICtrlCommand.ControlTypeEnum;
import Execution.command.ICondCommand;
import Execution.command.utils.CondEval;
import antlr.Java8Parser.ConditionalExpressionContext;
import semantic.mapping.IValueMapper;
import semantic.mapping.IdentifierMapper;

import java.util.ArrayList;
import java.util.List;

public class IfCom implements ICondCommand {

    private List<ICommand> positiveCommands; //list of commands to execute if the condition holds true
    private List<ICommand> negativeCommands; //list of commands to execute if the condition holds false

    private ConditionalExpressionContext conditionalExpr;
    private String modifiedConditionExpr;

    public IfCom(ConditionalExpressionContext conditionalExpr) {
        positiveCommands = new ArrayList<>();
        negativeCommands = new ArrayList<>();

        this.conditionalExpr = conditionalExpr;
    }

    @Override
    public void execute() {
        this.identifyVariables();

        ExecutionMonitor executionMonitor = ExecutionManager.getExecutionManager().getExecutionMonitor();

        try {
            //execute the positive commands
            if(CondEval.evaluateCondition(conditionalExpr)) {
                for(ICommand command : positiveCommands) {
                    executionMonitor.tryExecution();
                    command.execute();
                }
            }
            //execute the negative commands
            else {
                for(ICommand command : negativeCommands) {
                    executionMonitor.tryExecution();
                    command.execute();
                }
            }
        } catch(InterruptedException e) {
            System.err.println("IfCommand: Monitor block interrupted! " +e.getMessage());
        }

    }

    private void identifyVariables() {
        IValueMapper identifierMapper = new IdentifierMapper(conditionalExpr.getText());
        identifierMapper.analyze(conditionalExpr);

        modifiedConditionExpr = identifierMapper.getModifiedExp();
    }

    @Override
    public ControlTypeEnum getControlType() {
        return ControlTypeEnum.CONDITIONAL_IF;
    }

    @Override
    public void addPositiveCommand(ICommand command) {
        positiveCommands.add(command);
    }

    @Override
    public void addNegativeCommand(ICommand command) {
        negativeCommands.add(command);
    }

    public void clearAllCommands() {
        positiveCommands.clear();
        negativeCommands.clear();
    }

    public int getPositiveCommandsCount() {
        return positiveCommands.size();
    }

    public int getNegativeCommandsCount() {
        return negativeCommands.size();
    }

}