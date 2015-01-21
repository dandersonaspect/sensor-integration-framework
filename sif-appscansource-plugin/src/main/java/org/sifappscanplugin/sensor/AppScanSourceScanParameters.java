package org.sifappscanplugin.sensor;

import org.sif.SensorParameters;

public class AppScanSourceScanParameters implements SensorParameters
{
	String pafFile;
	String scanConfiguration;	// Optional?

	
	public AppScanSourceScanParameters(String pafFile, String scanConfiguration)
	{
		super();
		this.pafFile = pafFile;
		this.scanConfiguration = scanConfiguration;
	}
	
	public String getPafFile()
	{
		return pafFile;
	}
	
	public String getScanConfiguration()
	{
		return scanConfiguration;
	}

}
