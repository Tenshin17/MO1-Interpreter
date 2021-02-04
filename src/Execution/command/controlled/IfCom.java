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
        this.positiveCommands = new ArrayList<>();
        this.negativeCommands = new ArrayList<>();

        this.conditionalExpr = conditionalExpr;
    }

    @Override
    public void execute() {
        this.identifyVariables();

        ExecutionMonitor executionMonitor = ExecutionManager.getExecutionManager().getExecutionMonitor();

        try {
            //execute the positive commands
            //System.out.println(this.conditionalExpr.getText()+"Check this");
            if(CondEval.evaluateCondition(this.conditionalExpr)) {
                for(ICommand command : this.positiveCommands) {
                    executionMonitor.tryExecution();
                    command.execute();
                }
            }
            //execute the negative commands
            else {
                for(ICommand command : this.negativeCommands) {
                    executionMonitor.tryExecution();
                    command.execute();
                }
            }
        } catch(InterruptedException e) {
            System.err.println("IfCommand: Monitor block interrupted! " +e.getMessage());
        }

    }

    private void identifyVariables() {
        IValueMapper identifierMapper = new IdentifierMapper(this.conditionalExpr.getText());
        identifierMapper.analyze(this.conditionalExpr);

        this.modifiedConditionExpr = identifierMapper.getModifiedExp();
    }

    @Override
    public ControlTypeEnum getControlType() {
        return ControlTypeEnum.CONDITIONAL_IF;
    }

    @Override
    public void addPositiveCommand(ICommand command) {
        this.positiveCommands.add(command);
    }

    @Override
    public void addNegativeCommand(ICommand command) {
        this.negativeCommands.add(command);
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

}