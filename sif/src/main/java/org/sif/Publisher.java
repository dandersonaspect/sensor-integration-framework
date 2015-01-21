package org.sif;

import java.util.Map;


public interface Publisher
{

	public void publish(Job job, Notifier notifier, Map<String, String> parameters) throws Exception;

}
