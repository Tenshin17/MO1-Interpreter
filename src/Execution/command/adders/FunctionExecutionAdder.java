/**
 * 
 */
package Execution.command.adders;

import Execution.command.ICommand;
import semantic.representation.JavaMethod;

/**
 * Handles adding of commands to a certain function

 *
 */
public class FunctionExecutionAdder implements IExecutionAdder {

	private JavaMethod assignedJavaMethod;
	
	public FunctionExecutionAdder(JavaMethod javaMethod) {
		this.assignedJavaMethod = javaMethod;
	}

	@Override
	public void addCommand(ICommand command) {
		this.assignedJavaMethod.addCommand(command);
	}
	
	public JavaMethod getAssignedFunction() {
		return this.assignedJavaMethod;
	}

}
