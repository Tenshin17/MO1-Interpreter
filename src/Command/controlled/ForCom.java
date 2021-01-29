package Command.controlled;

import antlr.Java8Parser;
import Java8.builder.BuildChecker;
import Java8.builder.ErrorRepository;
import Java8.execution.ExecutionManager;
import Java8.execution.ExecutionMonitor;
import Command.ICommand;
import Command.ICtrlCommand;
import Java8.execution.commands.evaluation.AssignmentCommand;
import Java8.execution.commands.evaluation.MappingCommand;
import Java8.execution.commands.simple.IncDecCommand;
import Java8.execution.commands.utils.ConditionEvaluator;
import Java8.representations.Java8Value;
import Java8.semantics.analyzers.LocalVariableAnalyzer;
import Java8.semantics.mapping.IValueMapper;
import Java8.semantics.mapping.IdentifierMapper;
import Java8.semantics.searching.VariableSearcher;
import Java8.semantics.utils.LocalVarTracker;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class ForCom implements ICtrlCommand {

    private final static String TAG = "MobiProg_ForCommand";

    private List<ICommand> commandSequences;

    private Java8Parser.LocalVariableDeclarationContext localVarDecCtx; //a local variable ctx that is evaluated at the start of the for loop
    private Java8Parser.ExpressionContext conditionalExpr; //the condition to satisfy
    private ICommand updateCommand; //the update command aftery ever iteration

    private String modifiedConditionExpr;

    private ArrayList<String> localVars = new ArrayList<>();

    public ForCom(Java8Parser.LocalVariableDeclarationContext localVarDecCtx, Java8Parser.ExpressionContext conditionalExpr, ICommand updateCommand) {
        this.localVarDecCtx = localVarDecCtx;
        this.conditionalExpr = conditionalExpr;
        this.updateCommand = updateCommand;

        this.commandSequences = new ArrayList<ICommand>();
    }

    /* (non-Javadoc)
     * @see com.neildg.mobiprog.execution.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        //this.evaluateLocalVariable();
        this.identifyVariables();

        ExecutionMonitor executionMonitor = ExecutionManager.getInstance().getExecutionMonitor();

        LocalVarTracker.resetLocalVars(localVars);

        try {
            //evaluate the given condition
            while(ConditionEvaluator.evaluateCondition(this.conditionalExpr)) {
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
                this.updateCommand.execute(); //execute the update command
                this.identifyVariables(); //identify variables again to detect changes to such variables used.
            }

        } catch(InterruptedException e) {
            System.out.println(TAG + ": " + "Monitor block interrupted! " +e.getMessage());
        }
    }

    private void evaluateLocalVariable() {
        if(this.localVarDecCtx != null) {
            LocalVariableAnalyzer localVarAnalyzer = new LocalVariableAnalyzer();
            localVarAnalyzer.markImmediateExecution();
            localVarAnalyzer.analyze(this.localVarDecCtx);
        }
    }

    private void identifyVariables() {
        IValueMapper identifierMapper = new IdentifierMapper(this.conditionalExpr.getText());
        identifierMapper.analyze(this.conditionalExpr);

        this.modifiedConditionExpr = identifierMapper.getModifiedExp();
    }

    /* (non-Javadoc)
     * @see com.neildg.mobiprog.execution.commands.controlled.IControlledCommand#getControlType()
     */
    @Override
    public ControlTypeEnum getControlType() {
        return ControlTypeEnum.FOR_CONTROL;
    }

    /* (non-Javadoc)
     * @see com.neildg.mobiprog.execution.commands.controlled.IControlledCommand#addCommand(com.neildg.mobiprog.execution.commands.ICommand)
     */
    @Override
    public void addCommand(ICommand command) {

        System.out.println("		Added command to FOR");
        this.commandSequences.add(command);
    }

    public int getCommandCount() {
        return this.commandSequences.size();
    }

    public ArrayList<String> getLocalVars() {
        return localVars;
    }
}