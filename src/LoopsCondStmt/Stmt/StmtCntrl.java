package LoopsCondStmt.Stmt;
//package LoopsCondStmt.Stmt;
import baraco.execution.ExecutionManager;
import Command.ICommand;
import baraco.execution.commands.controlled.*;
import baraco.execution.commands.simple.MethodCallCommand;

import java.util.Stack;

public class StmtCntrl {

    private final static String TAG = "StatementControlOverseer";

    private static StmtCntrl sharedInstance = null;

    public static StmtCntrl getInstance() {
        return sharedInstance;
    }

    private Stack<ICommand> procedureCallStack;
    //private ICommand rootControlledCommand = null;
    private ICommand activeControlledCommand = null;

    private boolean isInPositive = true; //used for conditional statements to indicate if the series of commands should go to the positive command list.
    private boolean isInTry = false;

    private IAttemptCommand.CatchTypeEnum currentCatchType = null;

    private StmtCntrl() {
        this.procedureCallStack = new Stack<ICommand>();

        System.out.println(TAG + ": " + "Stack initialized!");
    }

    public static void initialize() {
        sharedInstance = new StmtCntrl();
    }

    public static void reset() {
        sharedInstance.procedureCallStack.clear();
        //sharedInstance.rootControlledCommand = null;
        sharedInstance.activeControlledCommand = null;
        sharedInstance.currentCatchType = null;
    }

    public void openConditionalCommand(IConditionalCommand command) {
        if(this.procedureCallStack.isEmpty()) {
            this.procedureCallStack.push(command);
            this.activeControlledCommand = command;
        }
        else {
            this.processAdditionOfCommand(command);
        }

        this.isInPositive = true;

    }

    /*
     * Opens a new controlled command
     */
    public void openControlledCommand(IControlledCommand command) {
        this.procedureCallStack.push(command);
        this.activeControlledCommand = command;
    }

    public void openAttemptCommand(IAttemptCommand command) {
        this.procedureCallStack.push(command);
        this.activeControlledCommand = command;

        this.isInTry = true;
    }

    public boolean isInPositiveRule() {
        return this.isInPositive;
    }

    public void reportExitPositiveRule() {
        this.isInPositive = false;
    }

    public boolean isInTryBlock() {
        return this.isInTry;
    }

    public IAttemptCommand.CatchTypeEnum getCurrentCatchType () {
        return this.currentCatchType;
    }

    public void reportExitTryBlock() {
        this.isInTry = false;
    }

    public void setCurrentCatchClause(IAttemptCommand.CatchTypeEnum catchTypeEnum) {
        this.currentCatchType = catchTypeEnum;
    }

    /*
     * Processes the proper addition of commands.
     */
    private void processAdditionOfCommand(ICommand command) {

        //if the current active controlled command is that of a conditional command,
        //we either add the newly opened command as either positive or a negative command
        if(this.isInConditionalCommand()) {
            IConditionalCommand conditionalCommand = (IConditionalCommand) this.activeControlledCommand;

            if(this.isInPositiveRule()) {
                conditionalCommand.addPositiveCommand(command);
            }
            else {
                conditionalCommand.addNegativeCommand(command);
            }

            this.procedureCallStack.push(command);
            this.activeControlledCommand = command;
        }
        //just add the newly opened command as a command under the last active controlled command.
        else {

            IControlledCommand controlledCommand = (IControlledCommand) this.activeControlledCommand;
            controlledCommand.addCommand(command);

            System.out.println("Adding to " +controlledCommand.getControlType());

            this.procedureCallStack.push(command);
            this.activeControlledCommand = command;
        }
    }


    /*
     * Closes the current active controlled command and adds the root controlled command to the execution manager.
     * The active controlled command is set to null.
     */
    public void compileControlledCommand() {

        //we arrived at the root node, therefore we add this now to the execution manager
        if(this.procedureCallStack.size() == 1) {
            ICommand rootCommand = this.procedureCallStack.pop();
            ExecutionManager.getInstance().addCommand(rootCommand);

            this.activeControlledCommand = null;
        }
        //we pop then add it to the next root node
        else if(this.procedureCallStack.size() > 1) {
            ICommand childCommand = this.procedureCallStack.pop();
            ICommand parentCommand = this.procedureCallStack.peek();
            this.activeControlledCommand = parentCommand;


            if (childCommand instanceof ForCommand || childCommand instanceof WhileCommand || childCommand instanceof TryCommand) {
                if (parentCommand instanceof ForCommand || parentCommand instanceof WhileCommand) {
                    IControlledCommand controlledCommand = (IControlledCommand) parentCommand;
                    controlledCommand.addCommand(childCommand);
                } else if (parentCommand instanceof IfCommand) {
                    IConditionalCommand controlledCommand = (IConditionalCommand) parentCommand;

                    if(isInPositiveRule())
                        controlledCommand.addPositiveCommand(childCommand);
                    else
                        controlledCommand.addNegativeCommand(childCommand);
                } else if (parentCommand instanceof TryCommand) {
                    IAttemptCommand attemptCommand = (IAttemptCommand) parentCommand;

                    if(isInTryBlock()) {
                        attemptCommand.addTryCommand(childCommand);
                    } else {
                        attemptCommand.addCatchCommand(this.currentCatchType, childCommand);
                    }
                }
            }
        }
        else {
            System.out.println(TAG + ": " + "Procedure call stack is now empty.");
        }
    }

    public boolean isInConditionalCommand() {
        return (this.activeControlledCommand != null && activeControlledCommand instanceof IConditionalCommand);
    }

    public boolean isInControlledCommand() {
        return (this.activeControlledCommand!= null && this.activeControlledCommand instanceof IControlledCommand);
    }

    public boolean isInAttemptCommand() {
        return (this.activeControlledCommand!= null && this.activeControlledCommand instanceof IAttemptCommand);
    }

    public ICommand getActiveControlledCommand() {
        return this.activeControlledCommand;
    }
}
