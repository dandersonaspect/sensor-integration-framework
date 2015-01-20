package org.sif.core.concurrency;

import java.util.concurrent.ExecutionException;

public class NativeExecutionException extends ExecutionException {

	private int returnCode = 0;

	public NativeExecutionException() {
		super();
	}

	public NativeExecutionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public NativeExecutionException(String arg0, int returnCode) {
		super(arg0);
		this.returnCode = returnCode;
	}

	public NativeExecutionException(String arg0) {
		super(arg0);
	}

	public NativeExecutionException(Throwable arg0) {
		super(arg0);
	}
	
	public int getReturnCode()
	{
		return returnCode;
	}
}
