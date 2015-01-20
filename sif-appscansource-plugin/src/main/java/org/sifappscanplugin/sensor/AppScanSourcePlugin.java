package org.sifappscanplugin.sensor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.sif.Job;
import org.sif.Notifier;
import org.sif.Publisher;
import org.sif.Reporter;
import org.sif.Sensor;
import org.sif.core.io.SplitOutputStream;
import org.sif.core.concurrency.Executable;
import org.sifappscanplugin.publisher.ASERestServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppScanSourcePlugin implements Sensor, Publisher, Reporter
{

	final Logger logger = LoggerFactory.getLogger( AppScanSourcePlugin.class );

	private static final String APPSCAN_SOURCE_EXE = "\\bin\\AppScanSrcCli.exe";

	// 90 minute timeout for scans
	private int actualTimeout = 5400;


	/**
	 * Need a no-argument constructor to support loading by ServiceLoader.
	 */
	public AppScanSourcePlugin()
	{

	}


	// public AppScanSourcePlugin(HashMap<String, String> envVarMap, String
	// scanWorkspaceDirectory,
	// String appScanHomeDirectory, String asseDirectory, PrintStream
	// outputStream)
	// {
	//
	// // Set variables from parameters
	// this.scanWorkspaceDirectory = scanWorkspaceDirectory;
	// this.appScanHomeDirectory = appScanHomeDirectory;
	// this.outputStream = outputStream;
	// this.asseDirectory = asseDirectory;
	//
	// this.envVars = envVarMap;
	//
	// this.asseLogsDirectory = asseDirectory + "\\logs";
	// this.assessmentFile = asseDirectory + "\\assessments\\assessment.ozasmt";
	// this.filteredAssessmentFile = asseDirectory +
	// "\\assessments\\assessment_filtered.ozasmt";
	// this.baselineAssessmentFile = asseDirectory +
	// "\\assessments\\assessment-baseline.ozasmt";
	// this.reportFile = asseDirectory + "\\reports\\report.html";
	// this.differentialReportFile = asseDirectory +
	// "\\reports\\report-differential.html";
	//
	// this.asseScriptLocation = asseDirectory + "/resources/cli_script.txt";
	// writeResource( "resources/scripts/cli_script.txt",
	// this.asseScriptLocation );
	//
	// this.aseScriptLocation = asseDirectory + "/resources/cli_script_ASE.txt";
	// writeResource( "resources/scripts/cli_script_ASE.txt",
	// this.aseScriptLocation );
	//
	// }

	private void copyFile(String fromFile, String toFile) throws IOException
	{
		logger.info( "Writing AppScan Source CLI script from " + fromFile + " to " + toFile );
		FileChannel inChannel = new FileInputStream( fromFile ).getChannel();
		FileChannel outChannel = new FileOutputStream( toFile ).getChannel();
		try
		{
			inChannel.transferTo( 0, inChannel.size(), outChannel );
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			if ( inChannel != null )
				inChannel.close();
			if ( outChannel != null )
				outChannel.close();
		}
	}


	private void copyResource(String resourcePath, String outputPath) throws IOException
	{
		logger.info( "Writing AppScan Source CLI script from " + resourcePath + " to " + outputPath );
		logger.info( "Runtime classpath: " + System.getProperty( "java.class.path" ) );

		InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream( resourcePath );
		// FIXME: Verify that the resource was found (input != null)
		BufferedReader reader = new BufferedReader( new InputStreamReader( input ) );
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter( new BufferedWriter( new FileWriter( resourcePath ) ) );
			String line;
			while ( ( line = reader.readLine() ) != null )
			{
				writer.println( line );
			}

		}
		finally
		{
			if ( writer != null )
			{
				writer.flush();
				writer.close();
			}
		}
	}


	private void printConsoleDivider()
	{
		logger.info( "######################################" );
	}


	public void addMarkup(String waflPath, String username, String password, String hostname, String installDir)
			throws Exception
	{
		// java -jar AddMarkup.jar
		// C:\DDrive\src\frameworkSamples\jsf2Sample\jsf2Sample\src\main\java
		// USERNAME PASSWORD HOSTNAME C:\DDrive\IBM\AppScanSource

		printConsoleDivider();
		logger.info( "Invoking Add Markup" );

		HashMap<String, String> environment = new HashMap<String, String>();

		// commandLine = "ping -c 10 localhost"; // DEBUG
		List<String> commandArray = new ArrayList<String>();
		commandArray.add( "java" );
		commandArray.add( "-jar" );
		commandArray.add( "CustomSolutions.jar" );
		commandArray.add( "addCustomMarkup" );
		commandArray.add( waflPath );
		commandArray.add( username );
		commandArray.add( password );
		commandArray.add( hostname );
		commandArray.add( installDir );
		Executable executable = new Executable( commandArray, environment, 0 );
		executable.setDirectory( new File( "C:\\DDrive\\JarFiles" ) ); // FIXME:
																		// Hack!
		executable.setOutputStream( System.out );
		executable.redirectStdOut( new SplitOutputStream( System.out ) );
		executable.redirectStdErr( new SplitOutputStream( System.out ) );
		String output = executable.execute( actualTimeout );

		// AddMarkup oneLineSetter = new AddMarkup(waflPath, username, password,
		// hostname, installDir, outputStream);
	}


	public void generateDifferentialReport(String installDir, String destinationDir, String baselineAssessment,
			String currentAssessment) throws Exception
	{
		// java -jar CustomSolutions.jar diffReportGen
		// C:\DDrive\IBM\AppScanSource
		// C:\SCAN\jsf2Sample-Continous-Integration\Scan_Workspace\ASSE\assessments
		// C:\SCAN\jsf2Sample-Continous-Integration\Scan_Workspace\ASSE\assessments\assessment-baseline.ozasmt
		// C:\SCAN\jsf2Sample-Continous-Integration\Scan_Workspace\ASSE\assessments\assessment.ozasmt

		printConsoleDivider();
		logger.info( "Invoking Differential Report Generation" );

		HashMap<String, String> environment = new HashMap<String, String>();

		// commandLine = "ping -c 10 localhost"; // DEBUG
		List<String> commandArray = new ArrayList<String>();
		commandArray.add( "java" );
		commandArray.add( "-jar" );
		commandArray.add( "CustomSolutions.jar" );
		commandArray.add( "diffReportGen" );
		commandArray.add( installDir );
		commandArray.add( destinationDir );
		commandArray.add( baselineAssessment );
		commandArray.add( currentAssessment );
		Executable executable = new Executable( commandArray, environment, 0 );
		executable.setDirectory( new File( "C:\\DDrive\\JarFiles" ) ); // FIXME:
																		// Hack!
		executable.setOutputStream( System.out );
		executable.redirectStdOut( new SplitOutputStream( System.out ) );
		executable.redirectStdErr( new SplitOutputStream( System.out ) );
		String output = executable.execute( actualTimeout );

		// AddMarkup oneLineSetter = new AddMarkup(waflPath, username, password,
		// hostname, installDir);
	}


	@Override
	public void sense(Job job, Notifier notifier, Map<String, String> parameters) throws Exception
	{
		logger.info( this.getClass().getSimpleName() + " performing scan actions" );

		HashMap<String, String> environment = new HashMap<String, String>();
		// String url = parameters.get( "url" );
		// String username = parameters.get( "username" );
		// String password = parameters.get( "password" );
		// this.scanWorkspaceDirectory = parameters.get( "scanWorkspace.dir" );
		// this.appScanHomeDirectory = parameters.get( "appScanSource.dir" );
		// this.asseDirectory = parameters.get( "appScanEnterprise.dir" );
		// this.assessmentFile = parameters.get( "assessmentFile" );
		// System.out.println( "scanWorkspaceDirectory: " +
		// scanWorkspaceDirectory );
		environment.put( "asse.url", parameters.get( "asse.url" ) );
		environment.put( "asse.username", parameters.get( "asse.username" ) );
		environment.put( "asse.password", parameters.get( "asse.password" ) );
		String scanWorkspaceDirectory = parameters.get( "scanWorkspace.dir" );
		String asseDirectory = parameters.get( "asse.dir" );
		String aseDirectory = parameters.get( "asse.dir" );
		environment.put( "paf.file", parameters.get( "paf.file" ) );
		environment.put( "asse.url", parameters.get( "asse.url" ) );
		logger.info( "scanWorkspace.dir: " + parameters.get( "scanWorkspace.dir" ) );

		// FIXME: Need to validate all paths (file or dir).
		// For the CLI this is: new File(path).exists()
		// For the Jenkins plugin it is: new FilePath( new File( this.pafFile )
		// ).exists()

		// this.envVars = envVarMap;

		// this.asseLogsDirectory = asseDirectory + "\\logs";
		// this.filteredAssessmentFile = asseDirectory +
		// "\\assessments\\assessment_filtered.ozasmt";
		// this.baselineAssessmentFile = asseDirectory +
		// "\\assessments\\assessment-baseline.ozasmt";
		// this.reportFile = asseDirectory + "\\reports\\report.html";
		// this.differentialReportFile = asseDirectory +
		// "\\reports\\report-differential.html";

		String scriptSourceDirectory = "src/main/resources"; // FIXME: Need to
																// know where
																// SIF is
																// installed

		String asseScriptLocation = scanWorkspaceDirectory + "/scripts/cli_script.txt";
		copyFile( scriptSourceDirectory + "/cli_script.txt", asseScriptLocation );

		String command = doublequote( asseDirectory + APPSCAN_SOURCE_EXE ) + " script "
				+ doublequote( asseScriptLocation );

		executeCommand( command, environment );
	}


	private String doublequote(String s)
	{
		return "\"" + s + "\"";
	}


	private String executeCommand(String commandLine) throws Exception
	{
		return executeCommand( commandLine, new HashMap<String, String>() );
	}


	private String executeCommand(String commandLine, Map<String, String> environment) throws Exception
	{
		printConsoleDivider();
		logger.info( "Executing command: " + commandLine );
		logger.info( "Environment: " + environment );

		// commandLine = "ping -c 10 localhost"; // DEBUG
		List<String> commandArray = Arrays.asList( CommandLine.parse( commandLine ).toStrings() );
		Executable executable = new Executable( commandArray, environment, 0 );
		executable.setOutputStream( System.out );
		executable.redirectStdOut( new SplitOutputStream( System.out ) );
		executable.redirectStdErr( new SplitOutputStream( System.out ) );
		String output = executable.execute( actualTimeout );

		return output;
	}


	@Override
	public void report(Job job, Notifier notifier, Map<String, String> parameters) throws Exception
	{
		logger.debug( "Reporting not yet implemented." );

		// FIXME: Generate a full or a differential report depending on a
		// parameter

	}


	private void copyEntryToMap(Map<String, String> sourceMap, Map<String, String> destinationMap, String key)
	{
		destinationMap.put( key, sourceMap.get( key ) );
	}


	/**
	 * This should be refactored into an ASE Reporting Console plugin so we can
	 * decouple ASSE integration and ASE integration.
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	@Override
	public void publish(Job job, Notifier notifier, Map<String, String> parameters) throws Exception
	{
		logger.info( this.getClass().getSimpleName() + " performing publish actions" );

		HashMap<String, String> environment = new HashMap<String, String>();
		PublishParameters publishParameters = new PublishParameters( parameters );

		// FIXME: Need error handling
		copyEntryToMap( parameters, environment, "asse.url" );
		copyEntryToMap( parameters, environment, "asse.username" );
		copyEntryToMap( parameters, environment, "asse.password" );
		copyEntryToMap( parameters, environment, "asse.acceptSsl" );
		copyEntryToMap( parameters, environment, "ase.url" );
		copyEntryToMap( parameters, environment, "ase.domain" );
		copyEntryToMap( parameters, environment, "ase.username" );
		copyEntryToMap( parameters, environment, "ase.password" );
		copyEntryToMap( parameters, environment, "assessment.file" );
		String asseDirectory = parameters.get( "asse.dir" );
		String aseDirectory = parameters.get( "ase.dir" );
		String scanWorkspaceDirectory = parameters.get( "scanWorkspace.dir" );
		String targetFolder = parameters.get( "ase.target.dir" );
		System.out.println( "scanWorkspace.dir: " + parameters.get( "scanWorkspace.dir" ) );

		// Translate from the Jenkins job folder to the ASE publishing folder.
		// FIXME: Should verify that the CloudBees Folders plugin is installed.
		// FIXME: Need error handling here too
		ASERestServicesClient aseClient = new ASERestServicesClient( publishParameters.getAseUrl(),
				publishParameters.getAseDomain(), publishParameters.getAseUsername(),
				publishParameters.getAsePassword(), publishParameters.isAseAcceptSsl() );

		int id = aseClient.createFolderStructure( 0, targetFolder.split( "/" ), "", "" );

		// FIXME: No good hack
		if ( id == -99 )
		{
			logger.error( "Something went wrong finding/creating folder structure.  Publish will probably fail." );
		}

		publish( job, notifier, parameters, environment, id );
	}


	public Integer doGetFolderId(String folder, String url, String domain, String username, String password,
			boolean asseptSsl) throws Exception
	{
		Integer id = null;

		logger.info( "doGetFolderId called on: " + folder + ", " + url + ", " + domain + ", " + username + ", "
				+ password + ", " + asseptSsl );
		// Translate from the Jenkins job folder to the ASE publishing folder.
		folder = "ASE/" + folder; // FIXME: Rude
		// FIXME: Need error handling here
		// folder = folder.substring(0, folder.lastIndexOf("/"));
		logger.info( "getting folder ID for: " + folder );

		ASERestServicesClient aseClient = new ASERestServicesClient( url, domain, username, password, asseptSsl );
		id = aseClient.getFolderIdService( folder );
		logger.info( "Folder ID: " + id );

		return id;
	}


	/**
	 * This should be refactored into an ASE Reporting Console plugin so we can
	 * decouple ASSE integration and ASE integration.
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	String publish(Job job, Notifier notifier, Map<String, String> parameters, Map<String, String> environment, int id)
			throws Exception
	{

		logger.info( "Publishing to folder id " + id );
		environment.put( "PUBLISH_FOLDER_ID", Integer.toString( id ) );

		String scriptSourceDirectory = "src/main/resources"; // FIXME: Need to
		// know where
		// SIF is
		// installed

		String scanWorkspaceDirectory = parameters.get( "scanWorkspace.dir" );
		String aseScriptLocation = scanWorkspaceDirectory + "/scripts/cli_script_ASE.txt";
		copyFile( scriptSourceDirectory + "/cli_script_ASE.txt", aseScriptLocation );

		CommandLine cmdLine = new CommandLine( doublequote( parameters.get( "asse.dir" ) + APPSCAN_SOURCE_EXE ) );
		cmdLine.addArgument( "script" );
		cmdLine.addArgument( doublequote( aseScriptLocation ) );

		logger.info( "Invoke AppScan Source command: " + doublequote( cmdLine.toString() ) );

		return executeCommand( cmdLine.toString(), environment );
	}

	class PublishParameters
	{
		private String asseUrl;

		private String asseUsername;

		private String assePassword;

		private boolean asseAcceptSsl;

		private String aseUrl;

		private String aseDomain;

		private String aseUsername;

		private String asePassword;

		private boolean aseAcceptSsl;

		private String assessmentFile;

		private String targetDir;


		public PublishParameters(Map<String, String> parameters)
		{
			// FIXME: At least check for any missing parameters here.
			asseUrl = parameters.get( "asse.url" );
			asseUsername = parameters.get( "asse.username" );
			assePassword = parameters.get( "asse.password" );
			asseAcceptSsl = Boolean.valueOf( parameters.get( "asse.acceptSsl" ) );
			aseUrl = parameters.get( "ase.url" );
			aseUsername = parameters.get( "ase.username" );
			asePassword = parameters.get( "ase.password" );
			aseAcceptSsl = Boolean.valueOf( parameters.get( "ase.acceptSsl" ) );
			assessmentFile = parameters.get( "assessment.file" );
			targetDir = parameters.get( "ase.target.dir" );
		}


		public String getAsseUrl()
		{
			return asseUrl;
		}


		public String getAsseUsername()
		{
			return asseUsername;
		}


		public String getAssePassword()
		{
			return assePassword;
		}


		public boolean isAsseAcceptSsl()
		{
			return asseAcceptSsl;
		}


		public String getAseUrl()
		{
			return aseUrl;
		}


		public String getAseDomain()
		{
			return aseDomain;
		}


		public String getAseUsername()
		{
			return aseUsername;
		}


		public String getAsePassword()
		{
			return asePassword;
		}


		public boolean isAseAcceptSsl()
		{
			return aseAcceptSsl;
		}


		public String getAssessmentFile()
		{
			return assessmentFile;
		}


		public String getTargetDir()
		{
			return targetDir;
		}

	}
}
