package semantic.statements;

import Execution.ExecutionManager;
import Execution.command.ICommand;
import Execution.command.ICondCommand;
import Execution.command.ICtrlCommand;
import semantic.utils.StringUtils;

import java.util.Stack;

/**
 * A singleton class that detects if a certain statement is inside a controlled statement
 * Contains utility functions to add certain commands into the active controlled command.
 * This class makes nested statements possible.
 *
 */
public class StatementControlOverseer {
	
	private static StatementControlOverseer scOverseer = null;
	
	public static StatementControlOverseer getInstance() {
		return scOverseer;
	}
	
	private Stack<ICommand> procedureCallStack;
	private ICommand activeControlledCommand = null;
	
	private boolean isInPositive = true; //used for conditional statements to indicate if the series of commands should go to the positive command list.
	
	private StatementControlOverseer() {
		this.procedureCallStack = new Stack<>();
		
		System.err.println("StatementControlOverseer: Stack initialized!");
	}
	
	public static void initialize() {
		scOverseer = new StatementControlOverseer();
	}
	
	public static void reset() {
		scOverseer.procedureCallStack.clear();
		scOverseer.activeControlledCommand = null;
	}
	
	public void openConditionalCommand(ICondCommand command) {
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
	public void openControlledCommand(ICtrlCommand command) {
		this.procedureCallStack.push(command);
		this.activeControlledCommand = command;
	}
	
	public boolean isInPositiveRule() {
		return this.isInPositive;
	}
	
	public void reportExitPositiveRule() {
		this.isInPositive = false;
	}
	
	/*
	 * Processes the proper addition of commands.
	 */
	private void processAdditionOfCommand(ICommand command) {
		
		//if the current active controlled command is that of a conditional command,
		//we either add the newly opened command as either positive or a negative command
		if(this.isInConditionalCommand()) {
			ICondCommand conditionalCommand = (ICondCommand) this.activeControlledCommand;
			
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
			
			ICtrlCommand controlledCommand = (ICtrlCommand) this.activeControlledCommand;
			controlledCommand.addCommand(command);

			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Adding command to " + controlledCommand.getControlType()));
			
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
			ExecutionManager.getExecutionManager().addCommand(rootCommand);
			
			this.activeControlledCommand = null;
		}
		//we pop then add it to the next root node
		else if(this.procedureCallStack.size() > 1) {
			ICommand childCommand = this.procedureCallStack.pop();
			ICommand parentCommand = this.procedureCallStack.peek();
			this.activeControlledCommand = parentCommand;
			
			if(parentCommand instanceof ICtrlCommand) {
				ICtrlCommand controlledCommand = (ICtrlCommand) parentCommand;
				controlledCommand.addCommand(childCommand);

			}
		}
		else {
			System.out.println("StatementControlOverseer: Procedure call stack is now empty.");
		}
	}
	
	public boolean isInConditionalCommand() {
		return (this.activeControlledCommand != null && this.activeControlledCommand instanceof ICondCommand);
	}
	
	public boolean isInControlledCommand() {
		return (this.activeControlledCommand!= null && this.activeControlledCommand instanceof ICtrlCommand);
	}
	
	public ICommand getActiveControlledCommand() {
		return this.activeControlledCommand;
	}
}
