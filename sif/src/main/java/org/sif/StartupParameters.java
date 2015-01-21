package org.sif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.owasp.esapi.reference.crypto.ReferenceEncryptedProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupParameters
{
	final Logger logger = LoggerFactory.getLogger( StartupParameters.class );

	private String userHomeDirectory;

	private String defaultParametersFile = "default.properties";

	private String globalParametersFile = "global.properties";

	private String connectionParametersFile = "encrypted.properties";

	// private String jobParametersFile;

	private Map<String, String> parameters = new HashMap<String, String>();


	public StartupParameters(String userHomeDirectory)
	{
		this.userHomeDirectory = userHomeDirectory;
	}


	public Map<String, String> getParameters()
	{
		return parameters;
	}


	public void loadParameters(String jobParametersFile)
	{
		try
		{
			loadProperties( new File( userHomeDirectory, defaultParametersFile ) );

			loadProperties( new File( userHomeDirectory, globalParametersFile ) );

			// Local properties override global properties
			loadProperties( new File( jobParametersFile ) );

			loadConnectionProperties( new File( userHomeDirectory, connectionParametersFile ) );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	private void loadProperties(File file) throws FileNotFoundException, IOException
	{
		logger.debug( "Loading properties from file: " + file );
		Properties properties = new Properties();

		properties.load( new FileReader( file ) );
		logger.info( "Loading properties: " + properties );

		// FIXME: Need a better solution than a cast
		parameters.putAll( (Map) properties );
	}


	// public EncryptedProperties loadEncryptedProperties(String propertiesFile,
	// String domainPrefix){
	// EncryptedProperties properties = null;
	// try
	// {
	// InputStream is = new FileInputStream(new File(propertiesFile));
	// properties = loadEncryptedProperties(is, domainPrefix);
	// }
	// catch (FileNotFoundException e)
	// {
	// e.printStackTrace();
	// }
	//
	// return properties;
	// }

	private void copyPropertiesToMap(Properties properties, Map<String, String> map)
	{
		for ( Object keyObject : properties.keySet() )
		{
			String key = (String) keyObject;
			String value = properties.getProperty( key );
			map.put( key, value );
		}
	}


	// /**
	// * FIXME: Refactor this sensor specific behavior. Also, the sensor plugin
	// developer should be
	// * able to define all properties in one place like so:
	// *
	// *
	// name=aseUrl,encrypted=true,required=true,environment=true,description=The
	// URL for AppScan Enterprise,default=https://localhost/ase
	// *
	// * Perhaps XML:
	// *
	// *
	// * @return
	// */
	// public HashMap<String, String> storePropertiesInEnvironment(){
	// HashMap<String,String> environmentVars = new HashMap<String, String>();
	// addPropertyToEnvironment(environmentVars, "ASE_URL", aseUrl);
	// addPropertyToEnvironment(environmentVars, "ASE_UNAME", aseUsername);
	// addPropertyToEnvironment(environmentVars, "ASE_PWORD", asePassword);
	// addPropertyToEnvironment(environmentVars, "ASSE_URL", asseUrl);
	// addPropertyToEnvironment(environmentVars, "ASSE_UNAME", asseUsername);
	// addPropertyToEnvironment(environmentVars, "ASSE_PWORD", assePassword);
	// if(aseDomain != null && !aseDomain.equals(""))
	// addPropertyToEnvironment(environmentVars, "ASE_DOMAIN", aseDomain);
	// addPropertyToEnvironment(environmentVars, "SCAN_CONFIGURATION",
	// scanConfiguration);
	// addPropertyToEnvironment(environmentVars, "APP_NAME",
	// pafFileLocation.getRemote());
	// addPropertyToEnvironment(environmentVars, "ASSESSMENT_FILE",
	// assessmentFile.getRemote());
	// //addPropertyToEnvironment(environmentVars, "ASSE_ARTIFACTS",
	// asseArtifacts.getRemote());
	//
	// return environmentVars;
	// }

	// private void addPropertyToEnvironment(HashMap<String,String> environment,
	// String key, String value) {
	// if (key != null)
	// {
	// if (value == null)
	// throw new IllegalArgumentException("Required property not set: " + key);
	//
	// environment.put(key, value);
	// }
	//
	// }

	/**
	 * FIXME: This works for the Jenkins plugin. We want the property loading
	 * code to be as common as possible between the CLI and any Launcher
	 * plugins.
	 * 
	 * @param propertiesFile
	 */
	private void loadConnectionPropertiesFromClasspath(String propertiesFile)
	{
		Properties properties = new Properties();

		// Load unencrypted properties from our CLASSPATH
		InputStream is = this.getClass().getResourceAsStream( "/" + propertiesFile );

		// FIXME: Handle is==null when properties file is missing
		try
		{
			properties.load( is );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if ( is != null )
			{
				try
				{
					is.close();
				}
				catch (IOException ignoreme)
				{
				}
			}
		}

		copyPropertiesToMap( properties, parameters );

	}


	private void loadConnectionProperties(File file) throws FileNotFoundException, IOException
	{
		logger.debug( "Loading properties from file: " + file );
		loadProperties(file);
//		loadEncryptedProperties(file);
//		copyPropertiesToMap( properties, parameters );

	}


	public Properties loadEncryptedProperties(String propertiesFile)
	{
		Properties properties = null;
		try
		{
			InputStream is = new FileInputStream( new File( propertiesFile ) );
			properties = loadEncryptedProperties( is );
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		return properties;
	}


	public Properties loadEncryptedProperties(InputStream propertiesStream)
	{
		Properties properties = new ReferenceEncryptedProperties();
		try
		{
			properties.load( propertiesStream );
			// this.aseUrl = props.getProperty(domainPrefix+"ase.url");
			// this.aseUsername =
			// props.getProperty(domainPrefix+"ase.username");
			// this.asePassword =
			// props.getProperty(domainPrefix+"ase.password");
			// this.aseDomain = props.getProperty(domainPrefix+"ase.domain");
			// this.asseUsername =
			// props.getProperty(domainPrefix+"asse.username");
			// this.assePassword =
			// props.getProperty(domainPrefix+"asse.password");
			// this.asseUrl = props.getProperty(domainPrefix+"asse.url");
			copyPropertiesToMap( properties, parameters );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if ( propertiesStream != null )
				{
					propertiesStream.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return properties;
	}

	// public HashMap<String, String> storePropertiesInEnvironment()
	// {
	// HashMap<String, String> environmentVars = new HashMap<String, String>();
	// addPropertyToEnvironment( environmentVars, "ASE_URL", aseUrl );
	// addPropertyToEnvironment( environmentVars, "ASE_UNAME", aseUsername );
	// addPropertyToEnvironment( environmentVars, "ASE_PWORD", asePassword );
	// addPropertyToEnvironment( environmentVars, "ASSE_URL", asseUrl );
	// addPropertyToEnvironment( environmentVars, "ASSE_UNAME", asseUsername );
	// addPropertyToEnvironment( environmentVars, "ASSE_PWORD", assePassword );
	// if ( aseDomain != null && !aseDomain.equals( "" ) )
	// addPropertyToEnvironment( environmentVars, "ASE_DOMAIN", aseDomain );
	// addPropertyToEnvironment( environmentVars, "SCAN_CONFIGURATION",
	// scanConfiguration );
	// addPropertyToEnvironment( environmentVars, "APP_NAME",
	// pafFileLocation.getRemote() );
	// addPropertyToEnvironment( environmentVars, "ASSESSMENT_FILE",
	// assessmentFile.getRemote() );
	// // addPropertyToEnvironment(environmentVars, "ASSE_ARTIFACTS",
	// // asseArtifacts.getRemote());
	//
	// return environmentVars;
	// }

	// private void addPropertyToEnvironment(HashMap<String, String>
	// environment, String key, String value)
	// {
	// if ( key != null )
	// {
	// if ( value == null )
	// throw new IllegalArgumentException( "Required property not set: " + key
	// );
	//
	// environment.put( key, value );
	// }
	//
	// }
	//
	// private void copyPropertiesToMap(Properties properties, Map<String,
	// String> map)
	// {
	// for (Object keyObject : properties.keySet())
	// {
	// String key = (String) keyObject;
	// String value = properties.getProperty( key );
	// map.put(key, value);
	// }
	// }

	// public Properties loadEncryptedProperties(InputStream propertiesStream,
	// String domainPrefix)
	// {
	// Properties properties = new ReferenceEncryptedProperties();
	// try
	// {
	// properties.load( propertiesStream );
	// // // FIXME: To be replaced by the parameters field.
	// // this.aseUrl = properties.getProperty( domainPrefix + "ase.url" );
	// // this.aseUsername = properties.getProperty( domainPrefix +
	// "ase.username" );
	// // this.asePassword = properties.getProperty( domainPrefix +
	// "ase.password" );
	// // this.aseDomain = properties.getProperty( domainPrefix + "ase.domain"
	// );
	// // this.asseUsername = properties.getProperty( domainPrefix +
	// "asse.username" );
	// // this.assePassword = properties.getProperty( domainPrefix +
	// "asse.password" );
	// // this.asseUrl = properties.getProperty( domainPrefix + "asse.url" );
	// copyPropertiesToMap(properties, parameters);
	// }
	// catch (EncryptionException e)
	// {
	// e.printStackTrace();
	// }
	// catch (IOException e)
	// {
	// e.printStackTrace();
	// }
	// finally
	// {
	// try
	// {
	// if ( propertiesStream != null )
	// {
	// propertiesStream.close();
	// }
	// }
	// catch (IOException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// return properties;
	// }
	//
	//
	// private void loadAllProperties()
	// {
	// Properties properties = new Properties();
	//
	// // Load unencrypted properties from our CLASSPATH
	// InputStream is = this.getClass().getResourceAsStream(
	// "/encryptedProperties.prop" );
	//
	// // FIXME: Handle is==null when properties file is missing
	// try
	// {
	// properties.load( is );
	// }
	// catch (IOException e)
	// {
	// e.printStackTrace();
	// }
	// finally
	// {
	// if ( is != null )
	// {
	// try
	// {
	// is.close();
	// }
	// catch (IOException ignoreme)
	// {
	// }
	// }
	// }
	// String domainPrefix = "";
	// if ( properties.getProperty( "prefix" ) != null )
	// {
	// // So when we retrieve other properties, we'll get them like
	// // domain.ase.url, rather than domainase.url
	// domainPrefix = properties.getProperty( "prefix" ) + ".";
	// }
	//
	// // Load encrypted properties from our CLASSPATH
	// InputStream eis = this.getClass().getResourceAsStream(
	// "/encryptedProperties.prop" );
	//
	// // load encrypted properties. This does not need to be stored in an
	// // object, but we store it anyway
	// Properties encrProperties = loadEncryptedProperties( eis, domainPrefix );
	//
	// // FIXME: Move this value into parameters
	// this.scanConfiguration = "Low Noise";
	// parameters.put("scanConfiguration", "Low Noise"); // FIXME: Should be
	// configurable
	//
	// // this.assessmentFile = asseAssessments.child("assessment.ozasmt");
	// this.pafFileLocation = new FilePath( new File( this.pafFile ) );
	//
	// try
	// {
	// if ( !this.pafFileLocation.exists() )
	// {
	// try
	// {
	// throw new Exception( "Could not find specified .paf/.sln: " +
	// this.pafFile );
	// }
	// catch (Exception e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// else
	// {
	// // this.copiedPathFile =
	// // this.configDirectory.child("application.paf");
	// // this.pafFileLocation.copyTo(copiedPathFile);
	// }
	// }
	// catch (IOException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// catch (InterruptedException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

}
