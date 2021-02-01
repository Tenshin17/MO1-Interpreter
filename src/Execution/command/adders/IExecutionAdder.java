/**
 * 
 */
package Execution.command.adders;

import Execution.command.ICommand;

/**
 * An interface for an execution adder. So far, we have a main execution adder, that adds a set commands to the main control flow.
 * And a function execution adder, that adds a set of commands to the function.
 *
 */
public interface IExecutionAdder {
	void addCommand(ICommand command);
}
