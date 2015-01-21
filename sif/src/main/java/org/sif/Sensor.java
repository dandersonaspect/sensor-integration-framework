package org.sif;

import java.util.Map;


public interface Sensor
{

	public void sense(Job job, Notifier notifier, Map<String, String> parameters) throws Exception;

}
