package org.sif;


public interface Notifier
{

	public void notify(String s) throws Exception;

	public void notifySuccess(String s) throws Exception;

	public void notifyFailure(String s) throws Exception;
	
	public void notifyFailure(String s, Exception e) throws Exception;
}
