package Execution;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A monitor class for handling thread execution.
 *
 */
public class ExecutionMonitor {
	private boolean executionFlag = true;
	
	private final Lock lock = new ReentrantLock();
	private final Condition executionGate = lock.newCondition();
	
	public ExecutionMonitor() {
		
	}
	
	/*
	 * Attempts to try execution process of a command. Thread sleeps if an existing command has claimed the flag.
	 */
	public void tryExecution() throws InterruptedException {
		lock.lock();
		
		try {
			while(!executionFlag) {
				System.out.println("ExecutionMonitor: Execution flag has been set to false. Execution sleeps!");
				executionGate.await();
			}
			
		}
		finally {
			System.out.println("Thread executing!");
			lock.unlock();
		}	
	}
	
	/*
	 * Claims the execution flag. Call this function if a certain command needs to halt the execution of succeeding commands.
	 */
	public void claimExecutionFlag() {
		executionFlag = false;
	}
	
	/*
	 * Releases the execution flag. Do not forget to call this function on the command who claimed the execution flag to resume execution!
	 */
	public void releaseExecutionFlag() {
		lock.lock();
		executionFlag = true;
		executionGate.signal();
		lock.unlock();
	}
}
