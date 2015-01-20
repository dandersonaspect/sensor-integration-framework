package org.sifappscanplugin.publisher;

/**
 * Signals that an error has occurred when communicating to an AppScan
 * Enterprise server.
 * 
 */
public class ASEClientException extends Exception
{

	public ASEClientException()
	{
	}


	public ASEClientException(String message)
	{
		super( message );
	}


	public ASEClientException(Throwable cause)
	{
		super( cause );
	}


	public ASEClientException(String message, Throwable cause)
	{
		super( message, cause );
	}

}
