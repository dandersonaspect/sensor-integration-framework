package org.sif;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * This class provides accessors for all implementors of the SIF extension interfaces.
 * 
 * @see org.sif.Sensor
 * @see org.sif.Publisher
 * @see org.sif.Notifier
 */
public class SensorIntegrationFramework
{

	Map<String, Sensor> sensorsMap = new Hashtable<String, Sensor>();

	Map<String, Publisher> publishersMap = new Hashtable<String, Publisher>();

	Map<String, Notifier> notifiersMap = new Hashtable<String, Notifier>();

	private static final Set<String> SIF_COMMANDS = Collections.unmodifiableSet( new HashSet<String>( Arrays.asList(
			"sense", "import", "report", "publish", "list" ) ) );


	public SensorIntegrationFramework()
	{
		// Find all classes on the classpath that implement Sensor, and
		// Publisher.
		// For each, instantiate it and put it in our map so the user can
		// reference it by a key.

		// FIXME: I believe this mechanism create two instances of the plugin if
		// the plugin
		// implements two of the interfaces we are loading.
		ServiceLoader<Sensor> sensorLoader = ServiceLoader.load( Sensor.class );
		for ( Sensor sensor : sensorLoader )
		{
			System.out.println( "Loading sensor provider: " + sensor );
			sensorsMap.put( sensor.getClass().getName(), sensor );
		}

		ServiceLoader<Publisher> publisherLoader = ServiceLoader.load( Publisher.class );
		for ( Publisher publisher : publisherLoader )
		{
			System.out.println( "Loading publisher provider: " + publisher );
			publishersMap.put( publisher.getClass().getName(), publisher );
		}

		ServiceLoader<Notifier> notifierLoader = ServiceLoader.load( Notifier.class );
		for ( Notifier notifier : notifierLoader )
		{
			System.out.println( "Loading notifier provider: " + notifier );
			notifiersMap.put( notifier.getClass().getName(), notifier );
		}

	}


	/**
	 * Retrieve an instance of the given class that implements Sensor.
	 * 
	 * @param scannerClass
	 * @return
	 */
	public Sensor getSensor(String scannerClass)
	{
		return sensorsMap.get( scannerClass );
	}


	/**
	 * Retrieve an instance of the given class that implements Publisher.
	 * 
	 * @param publisherClass
	 * @return
	 */
	public Publisher getPublisher(String publisherClass)
	{
		return publishersMap.get( publisherClass );
	}


	/**
	 * Retrieve an instance of the given class that implements Notifier.
	 * 
	 * @param notifierClass
	 * @return
	 */
	public Notifier getNotifier(String notifierClass)
	{
		return notifiersMap.get( notifierClass );
	}


	public boolean containsCommand(String command)
	{
		return SIF_COMMANDS.contains( command );
	}

}
