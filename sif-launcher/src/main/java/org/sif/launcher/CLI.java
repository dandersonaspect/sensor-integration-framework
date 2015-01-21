package org.sif.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.exec.CommandLine;

import org.owasp.esapi.EncryptedProperties;
import org.sif.Job;
import org.sif.Notifier;
import org.sif.Publisher;
import org.sif.Sensor;
import org.sif.SensorIntegrationFramework;
import org.sif.StartupParameters;
import org.sif.core.concurrency.Executable;
import org.sif.core.io.SplitOutputStream;
import org.sifappscanplugin.sensor.AppScanSourcePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * CLI --command scan --serviceprovider org.acmescan.MySensor --parameters file
 * CLI --command publish --serviceprovider org.foorepo.MyPublisher --parameters file
 * </pre>
 * 
 */
public class CLI implements Notifier
{
	final Logger logger = LoggerFactory.getLogger( CLI.class );

	private SensorIntegrationFramework framework = new SensorIntegrationFramework();
	
	private StartupParameters startupParameters = new StartupParameters("src/test/resources");		// FIXME: Hardcoded path is a no good hack
	
	private String jobParametersFile;

	// public enum SIFCommand
	// {
	// SENSE, REPORT, PUBLISH, LIST, IMPORT;
	//
	// public static boolean contains(String s)
	// {
	// for(SIFCommand command: values())
	// if (command.name().equals(s))
	// return true;
	// return false;
	// }
	// };

	private String command;

	private String serviceProviderClass;

	/**
	 * Defaults to this class.
	 */
	private String notifierClass = getClass().getName();

//	Map<String, String> parameters = new HashMap<String, String>();

	// FIXME: Hack!
	PrintStream outputStream = System.out;


	public CLI()
	{
	}


	// FIXME: Need better error handling
	public void process() throws Exception
	{
		Job job = new Job();
		// Dynamically load the requested service class and instantiate it.
		// Notifier notifier = framework.getNotifier( notifierClass );
		Notifier notifier = this;

		if ( command == null )
		{
			throw new IllegalArgumentException( "Argument 'command' cannot be null" );
		}
		if ( serviceProviderClass == null )
		{
			throw new IllegalArgumentException( "Argument 'serviceProviderClass' cannot be null" );
		}

		if ( command.equals( "sense" ) )
		{
			// Dynamically load the requested service class and instantiate it.
			Sensor sensor = framework.getSensor(serviceProviderClass);
			if ( sensor == null )
			{
				throw new IllegalArgumentException( "Unknown sense serviceProviderClass:" + serviceProviderClass );
			}

			sensor.sense( job, notifier, startupParameters.getParameters() );
		}
		else if ( command.equals( "publish" ) )
		{
			// Dynamically load the requested service class and instantiate it.
			Publisher publisher = framework.getPublisher(serviceProviderClass);
			if ( publisher == null )
			{
				throw new IllegalArgumentException( "Unknown publish serviceProviderClass:" + serviceProviderClass );
			}

			publisher.publish( job, notifier, startupParameters.getParameters() );
		}
		else
		{
			throw new IllegalArgumentException( "Unknown command:" + command );
		}
	}


	@Override
	public void notify(String s)
	{
		print( s );
	}


	@Override
	public void notifySuccess(String s)
	{
		print( "SUCCESS: " + s );
	}


	@Override
	public void notifyFailure(String s)
	{
		print( "FAILURE: " + s );
	}


	@Override
	public void notifyFailure(String s, Exception e)
	{
		print( "FAILURE: " + s + " because of " + e.getMessage() );
	}


	protected void loadParameters()
	{
		startupParameters.loadParameters(jobParametersFile);
	}

	private void displayUsage()
	{
		// print(
		// "java -jar SIF.jar --command [scan|import|list|publish|report] --serviceprovider <classname> --config <file> -url <url> -folder <folder> -domain <domain> -user <username> -password <password> [-acceptssl true|false]"
		// );
		print( "java -jar SIF.jar --command [scan|import|list|publish|report] --serviceprovider <classname> --config <file>" );
		print( "" );
		// FIXME: Test mode options?
		print( "For test mode, the given URL maps to a test subdirectory of the current directory and all XML" );
		print( "documents are named node.xml:" );
	}


	private void parseCommandLineOptions(String[] args) throws Exception
	{
		Map<String, String> argsMap = parseCommandLineOptionPairs( args );

		if ( args.length == 0 )
		{
			displayUsage();
		}
		else
		{

			System.out.println( "Current dir is " + System.getProperty( "user.dir" ) );

			try
			{
				command = argsMap.get( "--command" );
				if ( command == null || !framework.containsCommand( command ) )
				{
					throw new ParseCommandLineException( "Illegal argument for --command: " + command );
				}

				serviceProviderClass = argsMap.get( "--serviceprovider" );
				if ( serviceProviderClass == null )
				{
					throw new ParseCommandLineException( "Illegal argument for --serviceprovider: "
							+ serviceProviderClass );
				}

				jobParametersFile = argsMap.get( "--config" );
				if ( jobParametersFile == null )
				{
					throw new ParseCommandLineException( "Illegal argument for --config: " + jobParametersFile );
				}
				else if ( !new File( jobParametersFile ).exists() )
				{
					throw new ParseCommandLineException( "Could not find config file: " + jobParametersFile );
				}

			}
			catch (ParseCommandLineException e)
			{
				print( e.getMessage() );
				displayUsage();
				throw new Exception( "Fatal initialization error" );
			}
		}
	}

	class ParseCommandLineException extends Exception
	{

		public ParseCommandLineException(String message)
		{
			super( message );
		}

	}


	private Map<String, String> parseCommandLineOptionPairs(String[] args)
	{
		Map<String, String> argsMap = new Hashtable<String, String>();

		for ( int i = 0; i < args.length; i++ )
		{
			if ( args[i].startsWith( "-" ) && args.length > i + 1 )
			{
				argsMap.put( args[i].toLowerCase(), args[i + 1] );
			}
		}

		return argsMap;
	}


	private void printConsoleDivider()
	{
		print( "######################################" );
	}


	private static void print(Object message)
	{
		System.out.println( message );
	}


	public static void main(String[] args)
	{
		CLI app = new CLI();
		try
		{
			app.parseCommandLineOptions( args );
			app.loadParameters();
			app.process();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
