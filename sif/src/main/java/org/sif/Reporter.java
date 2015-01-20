package org.sif;

import java.util.Map;


public interface Reporter
{

	public void report(Job job, Notifier notifier, Map<String, String> parameters) throws Exception;

}
