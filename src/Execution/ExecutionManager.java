package Execution;

import Execution.command.adders.FunctionExecutionAdder;
import Execution.command.adders.IExecutionAdder;
import Execution.command.adders.MainExecutionAdder;
import Execution.command.ICommand;
import semantic.representation.JavaMethod;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Manages the sequence of execution of statements
 *
 */
public class ExecutionManager{

	public DefaultListModel consoleListModel = new DefaultListModel();
	private static ExecutionManager executionManager = null;
	public static boolean executionDone = true;
	public static boolean hasErrors = false;
	public static ExecutionManager getExecutionManager() {
		return executionManager;
	}
	
	private ArrayList<ICommand> executionList = new ArrayList<ICommand>();
	private boolean foundEntryPoint = false;
	private String entryClassName = null;
	
	private ExecutionThread executionThread;
	private ExecutionMonitor executionMonitor;
	
	private IExecutionAdder activeExecutionAdder;
	private MainExecutionAdder mainExecutionAdder;
	
	private ExecutionManager() {
		mainExecutionAdder = new MainExecutionAdder(executionList);
		activeExecutionAdder = mainExecutionAdder;
	}
	
	public static void initialize() {
		executionManager = new ExecutionManager();
	}
	
	public static void reset() {
		executionManager.foundEntryPoint = false;
		executionManager.entryClassName = null;
		executionManager.clearAllActions();
	}
	
	/*
	 * Reported by the parser walker if void main() has been found which means that an entry point for execution has been found.
	 * Required the class name in which main() has been found
	 */
	public void reportFoundEntryPoint(String entryClassName) {
		this.entryClassName = entryClassName;
		this.foundEntryPoint = true;
	}
	
	public boolean hasFoundEntryPoint() {
		return foundEntryPoint;
	}
	
	public String getEntryClassName() {
		return entryClassName;
	}
	
	public void addCommand(ICommand command) {
		activeExecutionAdder.addCommand(command);
	}
	
	/*
	 * Deletes a command from the main control flow
	 */
	public void deleteCommand(ICommand command) {
		executionList.remove(command);
	}
	
	/*
	 * Opens a function. Any succeeding commands to be added will be put to the function control flow.
	 */
	public void openFunctionExecution(JavaMethod javaMethod) {
		activeExecutionAdder = new FunctionExecutionAdder(javaMethod);
	}
	
	/*
	 * Returns true if the execution manager currently points to a function control flow.
	 */
	public boolean isInFunctionExecution() {
		return (activeExecutionAdder instanceof FunctionExecutionAdder);
	}
	
	/*
	 * Returns the current function that the execution manager is populating.
	 */
	public JavaMethod getCurrentFunction() {
		if(isInFunctionExecution()) {
			FunctionExecutionAdder functionExecAdder = (FunctionExecutionAdder) activeExecutionAdder;
			
			return functionExecAdder.getAssignedFunction();
		}
		else {
			System.err.println("Execution manager is not in a function!");
			return null;
		}
	}
	
	/*
	 * Closes a function. Control flow will be given back to the main execution adder.
	 */
	public void closeFunctionExecution() {
		activeExecutionAdder = mainExecutionAdder;
	}
	
	/*
	 * Blocks the execution of the thread. Can only be called once. At this point, resumeExecution() must be called by a specific command.
	 */
	public void blockExecution() {
		executionMonitor.claimExecutionFlag();
	}
	
	/*
	 * Resumes the execution of thread. Can only be called once. At this point, the execution thread should continue to do other actions.
	 */
	public void resumeExecution() {
		executionMonitor.releaseExecutionFlag();
	}
	
	/*
	 * Spawns a worker thread to handle execution of actions. A semaphore flag is included that may attempt to be claimed by specific commands (like SCAN statement).
	 * This causes the execution thread to temporarily halt until released.
	 */
	public void executeAllActions() {
		executionMonitor = new ExecutionMonitor();
		executionThread = new ExecutionThread(executionList, executionMonitor);
		executionThread.start();
	}
	
	public void clearAllActions() {
		executionList.clear();
	}
	
	/*
	 * Gets the execution monitor. This is used for controlled commands that also needs to check prior to execution.
	 */
	public ExecutionMonitor getExecutionMonitor() {
		return executionMonitor;
	}

}
