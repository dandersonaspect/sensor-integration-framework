package org.sif.core.concurrency;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;


public class ExecutableObsolete implements Callable<Integer>
{
	private Executor executor;
	private String commandLine;
	private PrintStream outputStream;
	private long timeoutInSeconds;
	private Object interruptLock;
	private boolean isInterrupted = false;
	private Map<String,String> environmentVars;
	
	public ExecutableObsolete(Executor executor, String commandLine, PrintStream outputStream, long timeoutInSeconds, Object interruptLock, Map<String,String> environment)
	{
		this.executor = executor;
		this.commandLine = commandLine;
		this.outputStream = outputStream;
		this.timeoutInSeconds = timeoutInSeconds;
		this.interruptLock = interruptLock;
		this.environmentVars = environment;
	}
	
	public Integer call() throws Exception {
		
		int exitValue = -1;

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(stdout);	// FIXME: What is the reason for this change?
		
//		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

		CommandLine cmdLine = CommandLine.parse(commandLine);
		
		outputStream.println("Executing command: " + cmdLine.toString());
		printMap("Environment variables passed to sub-process", environmentVars, outputStream);
		long timeoutInMillis = 1000L * timeoutInSeconds;
		outputStream.println("Timeout (ms): " + timeoutInMillis);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(timeoutInMillis);
		
		executor.setExitValue(0);
		executor.setStreamHandler(streamHandler);
		executor.setWatchdog(watchdog);

		try{
			exitValue = executor.execute(cmdLine, environmentVars);
		} catch(ExecuteException e) {
			if(executor.isFailure(exitValue) && executor.getWatchdog().killedProcess())
			{
				if (isInterrupted)					
					outputStream.println("Process was stopped by the user.");
				else
					outputStream.println("Process was stopped due to timeout of " + timeoutInSeconds + " seconds.");
			}
			else
				outputStream.println("Failure executing  \"" + cmdLine + "\": " + e.getMessage());
		}

		// Tell our main job thread to stop waiting for Stop interrupts from the user
		synchronized (interruptLock)
		{
			interruptLock.notify();
		}
			
		return exitValue;
	}
	
	public void terminate()
	{
		isInterrupted = true;
		outputStream.println("Terminating process");
		executor.getWatchdog().destroyProcess();
	}

	public static void printMap(String mapName, Map<String, String> map,
			PrintStream out) {
		out.println("");
		out.println("");
		out.println(mapName + ":");
		for (Entry<String, String> entry : map.entrySet()) {
			if (entry.getKey().contains("PWORD")) {
				int pwordLength = entry.getValue().length();
				String pwordMasked = "";
				for (int i = 0; i < pwordLength; i++) {
					pwordMasked += "*";
				}
				out.println("SET " + entry.getKey() + "=" + pwordMasked);
			} else {
				out.println("SET " + entry.getKey() + "=" + entry.getValue());
			}
		}
		out.println("End " + mapName);
		out.println("");
	}
}
