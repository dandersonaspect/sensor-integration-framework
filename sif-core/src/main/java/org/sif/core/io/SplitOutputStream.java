package org.sif.core.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Create an OutputStream that writes its data to all of the given OutputStream instances.
 * 
 * @author David Anderson
 *
 */
public class SplitOutputStream extends OutputStream
{
	Vector<OutputStream> outputs = new Vector<OutputStream>();
	
	public SplitOutputStream()
	{
	}
	
	public SplitOutputStream(OutputStream... outputArgs)
	{
		for (int i = 0; i < outputArgs.length; i++)
		{
			if (outputArgs[i] == null)
			{
				throw new IllegalArgumentException("Cannot redirect to a null stream");
			}
			outputs.add(outputArgs[i]);
		}
	}
	
	public void addOutputStream(OutputStream out)
	{
		if (out == null)
		{
			throw new IllegalArgumentException("Cannot redirect to a null stream");
		}
		outputs.add(out);
	}

	@Override
	public void write(int ch) throws IOException
	{
		for (OutputStream out : outputs)
		{
			out.write(ch);
		}
	}

	@Override
	public void close() throws IOException
	{
		for (OutputStream out : outputs)
		{
			out.close();
		}
	}
	
	@Override
	public void flush() throws IOException
	{
		for (OutputStream out : outputs)
		{
			out.flush();
		}
	}
}
