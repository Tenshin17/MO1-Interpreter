package Execution.command.controlled;

import Execution.ExecutionManager;
import Execution.ExecutionMonitor;
import Execution.command.ICommand;
import Execution.command.ICtrlCommand;
import Execution.command.utils.CondEval;
import antlr.Java8Parser.ConditionalExpressionContext;
import semantic.mapping.IValueMapper;
import semantic.mapping.IdentifierMapper;
import semantic.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class WhileCom implements ICtrlCommand {

    protected List<ICommand> commandSequences; //the list of commands inside the WHILE statement

    protected ConditionalExpressionContext conditionalExpr;
    protected String modifiedConditionExpr;

    public WhileCom(ConditionalExpressionContext conditionalExpr) {
        this.commandSequences = new ArrayList<>();
        this.conditionalExpr = conditionalExpr;
    }

    @Override
    public void execute() {
        identifyVariables();

        ExecutionMonitor executionMonitor = ExecutionManager.getExecutionManager().getExecutionMonitor();

        try {
            //evaluate the given condition
            while(CondEval.evaluateCondition(this.conditionalExpr)) {
                for(ICommand command : this.commandSequences) {
                    executionMonitor.tryExecution();
                    command.execute();
                }

                identifyVariables(); //identify variables again to detect changes to such variables used.
            }

        } catch(InterruptedException e) {
            System.err.println("WhileCommand: Monitor block interrupted! " +e.getMessage());
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

        ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Added command to WHILE"));
        this.commandSequences.add(command);
    }

    public int getCommandCount() {
        return this.commandSequences.size();
    }

}
