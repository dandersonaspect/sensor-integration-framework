package org.sif;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.Set;


public class SensorIntegrationFramework
{

	Map<String, Sensor> sensorsMap = new Hashtable<String, Sensor>();

	Map<String, Publisher> publishersMap = new Hashtable<String, Publisher>();
	
	Map<String, Notifier> notifiersMap = new Hashtable<String, Notifier>();
	
	private static final Set<String> SIF_COMMANDS = Collections.unmodifiableSet( new HashSet<String>( Arrays.asList(
			"sense", "import", "report", "publish", "list" ) ) );



	public SensorIntegrationFramework()
	{
		System.out.println("Instantiating SensorIntegrationFramework");
		System.out.println("Runtime classpath: " + System.getProperty( "java.class.path" ));
		// Find all classes on the classpath that implement Sensor, and
		// Publisher.
		// For each, instantiate it and put it in our map so the user can
		// reference it by a key.

		// FIXME: Does this mechanism create two instances of the plugin?
		// Hopefully not.
		ServiceLoader<Sensor> sensorLoader = ServiceLoader.load( Sensor.class );
		for ( Sensor sensor : sensorLoader )
		{
			System.out.println("Loading sensor provider: " + sensor);
			sensorsMap.put( sensor.getClass().getName(), sensor );
		}

		ServiceLoader<Publisher> publisherLoader = ServiceLoader.load( Publisher.class );
		for ( Publisher publisher : publisherLoader )
		{
			System.out.println("Loading publisher provider: " + publisher);
			publishersMap.put( publisher.getClass().getName(), publisher );
		}
	}


	public Sensor getSensor(String scannerClass)
	{
		return sensorsMap.get( scannerClass );
	}


	public Publisher getPublisher(String publisherClass)
	{
		return publishersMap.get( publisherClass );
	}
	
	public Notifier getNotifier(String notifierClass)
	{
		return notifiersMap.get( notifierClass );
	}
	
	
	public boolean containsCommand(String command)
	{
		return SIF_COMMANDS.contains( command );
	}
	
	

}
