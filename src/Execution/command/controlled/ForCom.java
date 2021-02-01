package Execution.command.controlled;

import Execution.ExecutionManager;
import Execution.ExecutionMonitor;
import Execution.command.ICommand;
import Execution.command.ICtrlCommand;
import Execution.command.utils.CondEval;
import antlr.Java8Parser.ExpressionContext;
import antlr.Java8Parser.LocalVariableDeclarationContext;
import semantic.analyzers.LocalVariableAnalyzer;
import semantic.mapping.IValueMapper;
import semantic.mapping.IdentifierMapper;
import semantic.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ForCom implements ICtrlCommand{

    private List<ICommand> commandSequences;

    private LocalVariableDeclarationContext localVarDecCtx; //a local variable ctx that is evaluated at the start of the for loop
    private ExpressionContext conditionalExpr; //the condition to satisfy
    private ICommand updateCommand; //the update command aftery ever iteration

    private String modifiedConditionExpr;

    public ForCom(LocalVariableDeclarationContext localVarDecCtx, ExpressionContext conditionalExpr, ICommand updateCommand) {
        this.localVarDecCtx = localVarDecCtx;
        this.conditionalExpr = conditionalExpr;
        this.updateCommand = updateCommand;

        commandSequences = new ArrayList<>();
    }

    @Override
    public void execute() {
        evaluateLocalVariable();
        identifyVariables();

        ExecutionMonitor executionMonitor = ExecutionManager.getExecutionManager().getExecutionMonitor();

        try {
            //evaluate the given condition
            while(CondEval.evaluateCondition(conditionalExpr)) {
                for(ICommand command : commandSequences) {
                    executionMonitor.tryExecution();
                    command.execute();
                }

                updateCommand.execute(); //execute the update command
                identifyVariables(); //identify variables again to detect changes to such variables used.
            }

        } catch(InterruptedException e) {
            System.err.println("ForCommand: Monitor block interrupted! " +e.getMessage());
        }
    }

    private void evaluateLocalVariable() {
        if(localVarDecCtx != null) {
            LocalVariableAnalyzer localVarAnalyzer = new LocalVariableAnalyzer();
            localVarAnalyzer.markImmediateExecution();
            localVarAnalyzer.analyze(localVarDecCtx);
        }
    }

    private void identifyVariables() {
        IValueMapper identifierMapper = new IdentifierMapper(conditionalExpr.getText());
        identifierMapper.analyze(conditionalExpr);

        modifiedConditionExpr = identifierMapper.getModifiedExp();
    }

    @Override
    public ControlTypeEnum getControlType() {
        return ControlTypeEnum.FOR_CONTROL;
    }

    @Override
    public void addCommand(ICommand command) {
        ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Added command to FOR"));
        commandSequences.add(command);
    }

    public int getCommandCount() {
        return commandSequences.size();
    }

}