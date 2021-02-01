package Execution;

import Execution.command.ICommand;

import java.util.ArrayList;

/**
 * A worker thread that handles the execution of actions from ExecutionManager
 *
 */
public class ExecutionThread extends Thread {
	
	private ArrayList<ICommand> executionList = new ArrayList<>();
	private ExecutionMonitor executionMonitor;
	
	public ExecutionThread(ArrayList<ICommand> executionList, ExecutionMonitor executionMonitor) {
		this.executionList = executionList;
		this.executionMonitor = executionMonitor;
	}
	
	/*
	 * Runs the thread by executing all actions provided that the execution flag isn't acquired by any other commands.
	 * If a command attempts to acquire the flag, this thread will block until its flag is released(presumably by the command 
	 * who acquired it or another command).
	 */
	@Override
	public void run() {
		try {
			System.out.println("Thread running! " + executionList.size());
			for(ICommand command : executionList) {
				executionMonitor.tryExecution();
				command.execute();
			}
			ExecutionManager.executionDone = false;
		}
		catch(InterruptedException e) {
			System.err.println("ExecutionThread: Monitor block interrupted! " + e.getMessage());
		}
	}
}
