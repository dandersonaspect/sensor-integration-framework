package org.sif.core.concurrency;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sif.core.io.SplitOutputStream;
import org.sif.core.io.StreamGobbler;

public class Executable
{
	private final static Logger logger = LoggerFactory.getLogger(Executable.class); 
	
	private List<String> command = new Vector<String>();
	private Map<String, String> env = null;
	private String input;
	private int successCode;
	private File workingDirectory = null;
	OutputStream redirectedOut = null;
	OutputStream redirectedErr = null;
	Future<String> future;
	PrintStream outStream;
	
	public Executable(String command, int successCode) throws InterruptedException, ExecutionException
	{
		this(command, null, successCode);
	}

	public Executable(String command, Map<String, String> env, int successCode) throws InterruptedException, ExecutionException
	{
		this.command.add(command);
		this.env = env;
		this.successCode = successCode;
	}
	
	public Executable(List<String> command, int successCode) throws InterruptedException, ExecutionException
	{
		this(command, null, successCode);
	}

	public Executable(List<String> command, Map<String, String> env, int successCode) throws InterruptedException, ExecutionException
	{
		this.command = command;
		this.env = env;
		this.successCode = successCode;
	}
	
	public String execute() throws InterruptedException, ExecutionException
	{
		String output = null;
		
		ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
		ExecutableCallable callable = new ExecutableCallable(command, env, workingDirectory, input, successCode);
		future = threadExecutor.submit(callable);
		output = future.get();
		threadExecutor.shutdownNow();
		
		return output;
	}
	
	public String execute(final int timeoutInSeconds) throws InterruptedException, ExecutionException, TimeoutException
	{
		String output = null;
		
		ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
		ExecutableCallable callable = new ExecutableCallable(command, env, workingDirectory, input, successCode);
		future = threadExecutor.submit(callable);
		output = future.get(timeoutInSeconds, TimeUnit.SECONDS);
		threadExecutor.shutdownNow();

		return output;
	}
	
	public void setInputContent(String input)
	{
		this.input = input;
	}
	
	public void redirectStdOut(OutputStream out)
	{
		this.redirectedOut = out;
	}
	
	public void redirectStdErr(OutputStream out)
	{
		this.redirectedErr = out;
	}
	
	public void setDirectory(File directory)
	{
		this.workingDirectory = directory;
	}
	
	public void setOutputStream(PrintStream outStream)
	{
		this.outStream = outStream;
	}
	
	/**
	 * FIXME: Remove this main() when we have a unit test to replace it.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		PrintStream out = System.out;
		List<String> commandArray = new ArrayList<String>();
		commandArray.add("/Users/David/Desktop/interactive_script.sh");
		String answers;
		try
		{
			answers = readTextFile(new File("/Users/David/Desktop/answers.txt"));
			Executable executable = new Executable(commandArray, 0);
			executable.setInputContent(answers);
			executable.redirectStdOut(new SplitOutputStream(out));
			executable.redirectStdErr(new SplitOutputStream(out));
			out.println("Executing command: " + commandArray);
			String output = executable.execute(6000);
			out.println("Output: " + output);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static String readTextFile(File file) throws IOException
	{
		StringWriter writer = new StringWriter();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try
		{
	        String line;
	        while ((line = reader.readLine()) != null)
	        {
	        	writer.write(line);
	        	writer.write(System.getProperty("line.separator"));
	        }
	        writer.flush();
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException ignore) {}
			}
		}
        
        return writer.toString();
	}


	class ExecutableCallable implements Callable<String>
	{
		private List<String> command;
		private Map<String, String> env;
		private File workingDirectory;
		private String input;
		private int successCode;
		
		public ExecutableCallable(List<String> command, Map<String, String> env, File workingDirectory, String input, int successCode)
		{
			this.command = command;
			this.env = env;
			this.workingDirectory = workingDirectory;
			this.input = input;
			this.successCode = successCode;
		}
		
		public String call() throws NativeExecutionException, InterruptedException
		{
			String output = null;
			String errorOutput = null;
			int returnCode;
			
			ProcessBuilder pb = new ProcessBuilder(command);
			if (workingDirectory != null)
				pb.directory(workingDirectory);
			
			//Get existing environment variables and add any passed by the caller
			Map<String, String> exitstingEnv = pb.environment();
			if (env != null)
			{
				for (Entry<String, String> entry : env.entrySet())
				{
					exitstingEnv.put(entry.getKey(), entry.getValue());
				}
			}
			
			logger.debug("Environment for child process: " + formatAndSanitizeMap(exitstingEnv));
			logger.debug("Executing command: " + command);
			outStream.println("Environment for child process: " + formatAndSanitizeMap(exitstingEnv));
			outStream.println("Executing command: " + command);

			InputStream processOut = null;
			InputStream processError = null;
			try
			{
				Process process = pb.start();
				processOut = process.getInputStream();
				processError = process.getErrorStream();
				OutputStream processIn = process.getOutputStream();
				// Should have a reasonable limit on these dynamically sized Writer buffers.
				CharArrayWriter out = new CharArrayWriter( 8192 );
				CharArrayWriter errorOut = new CharArrayWriter( 128 );

				// Write to the child process' stdin.
				if ( input != null )
				{
					processIn.write( input.getBytes() );
					processIn.flush();
					processIn.close();
				}
				
				Thread inGobbler = new StreamGobbler(processOut, redirectedOut, out);
				inGobbler.start();
				Thread errorGobbler = new StreamGobbler(processError, redirectedErr, errorOut);
				errorGobbler.start();
				
				inGobbler.join();
				errorGobbler.join();
				
				output = out.toString();

				errorOutput = errorOut.toString();
				
				// Check the return value of the child process.
				returnCode = process.waitFor();
				if ( returnCode != successCode )
				{
					throw new NativeExecutionException( "Execution of \"" + command +
							"\" failed, outputting <" + output + errorOutput + ">", returnCode );
				}
			}
			catch (IOException e)
			{
				throw new NativeExecutionException( "Execution of \"" + command + "\" IO problem.", e );
			}
			finally
			{
				if (processOut != null)
				{
					try
					{
						processOut.close();
					}
					catch (IOException ignore) {}
				}
				if (processError != null)
				{
					try
					{
						processError.close();
					}
					catch (IOException ignore) {}
				}
			}
			
			return output;
		}
		
	}

	public static String formatAndSanitizeMap(Map<String, String> map) {
		StringWriter formatted = new StringWriter();
		PrintWriter out = new PrintWriter(formatted);
		
		out.println("=== Begin map ===");
		if (map != null)
		{
			for (Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (entry.getKey().contains("PWORD"))
					value = "******";
				out.println("SET " + key + "=" + value);
			}
		}
		out.println("=== End map ===");
		out.flush();
		out.close();
		
		return formatted.toString();
	}
}

