package org.sif.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * A Thread that fully reads from the given InputStream and writes to the given
 * OutputStream.
 * 
 * Use this class to asynchronously read from an InputStream that will block
 * when its buffer is full (e.g. from a Process).
 * 
 * @see Process.getInputStream()
 * 
 */
public class StreamGobbler extends Thread
{
	InputStream is;

	OutputStream os;

	Writer capture;


	public StreamGobbler(InputStream is)
	{
		this( is, null );
	}


	public StreamGobbler(InputStream is, OutputStream redirect)
	{
		this( is, null, null );
	}


	public StreamGobbler(InputStream is, OutputStream redirect, Writer capture)
	{
		this.is = is;
		this.os = redirect;
		this.capture = capture;
	}


	public void run()
	{
		try
		{
			int nextCharacter;
			while ( ( nextCharacter = is.read() ) != -1 )
			{
				if ( os != null )
					os.write( nextCharacter );

				if ( capture != null )
					capture.write( nextCharacter );
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if ( os != null )
			{
				try
				{
					os.flush();
				}
				catch (IOException e)
				{
					// Ignore
				}
			}
			if ( capture != null )
			{
				try
				{
					capture.flush();
				}
				catch (IOException e)
				{
					// Ignore
				}
			}
		}
	}
}
