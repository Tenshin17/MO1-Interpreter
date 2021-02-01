/**
 * 
 */
package Execution.command.adders;

import Execution.command.ICommand;

import java.util.ArrayList;

/**
 * Handles adding of execution to the main control flow.
 *
 */
public class MainExecutionAdder implements IExecutionAdder {

	private ArrayList<ICommand> mainExecutionList;
	
	public MainExecutionAdder(ArrayList<ICommand> mainExecutionList) {
		this.mainExecutionList = mainExecutionList;
	}
	
	@Override
	public void addCommand(ICommand command) {
		this.mainExecutionList.add(command);
	}

}
