package org.sifappscanplugin.sensor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sif.core.io.FileSearch;

public class ScanResult
{
	boolean status = false;
	
	private List<String> errors = new ArrayList<String>();
	
	private AssessmentSummary statistics = null;
	
	public ScanResult(boolean status, File assessmentFile, File reportFile)
	{
		this.status = status;
		gatherErrorMessages(assessmentFile);
		if (reportFile != null && reportFile.exists())
		{
//			try
//			{
				statistics = getAssessmentSummary();
//			}
//			catch (IOException e)
//			{
//				e.printStackTrace();
//			}
		}
	}
	
    private void gatherErrorMessages(File assessmentFile)
    {
    	try
		{
    		errors.addAll(FileSearch.find(assessmentFile, "<.*>(.*compute limit of.*)<.*>"));
    		errors.addAll(FileSearch.find(assessmentFile, "<.*>(.*Skipping .* due to error:.*)<.*>"));
    		errors.addAll(FileSearch.find(assessmentFile, "<.*>(.*Aborting .* due to error:.*)<.*>"));
    		errors.addAll(FileSearch.find(assessmentFile, "<.*>(.*IPVA time limit exceeded.*)<.*>"));
    		errors.addAll(FileSearch.find(assessmentFile, "<.*>(.*succeeded or up-to-date,\\s[1-9]\\sfailed.*)<.*>"));		// From .NET compiler
    		errors.addAll(FileSearch.find(assessmentFile, "<.*>(.*Parsing error at.*due to: cannot access include file.*)<.*>"));		// From PHP compiler
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args)
    {
    	ScanResult result = new ScanResult(true, 
    			new File("/Users/david/Downloads/assessment.ozasmt"), 
//    			new File("/Users/david/dev/appscan-workspace/ASSE/assessments/assessment.ozasmt"), 
    			new File("/Users/david/dev/appscan-workspace/ASSE/reports/report.html"));
    	System.out.println("Results: " + result.toString());
    }
	
	public void add(String errorMessage)
	{
		errors.add(errorMessage);
	}
	
	public void addAll(List<String> errorMessages)
	{
		errors.addAll(errorMessages);
	}
	
	public boolean getStatus()
	{
		return status;
	}
	
	public AssessmentSummary getAssessmentSummary()
	{
		return statistics;
	}
	
	public int size()
	{
		return errors.size();
	}

	public String getStatistics()
	{
		return statistics != null ? statistics.getCSV() : "";
	}
	
	public String getErrors()
	{
		StringBuffer buffer = new StringBuffer();
		for (String error : errors)
		{
			buffer.append(error);
			buffer.append(System.getProperty("line.separator"));
		}
		
		return buffer.toString();
	}
	
	public String toString()
	{
		return "Statistics:" + System.getProperty("line.separator") + 
				getStatistics() + System.getProperty("line.separator") + 
				"Errors: " + System.getProperty("line.separator") +
				getErrors();
	}
}

class AssessmentSummary
{
	
	private String name;
	
	private List<CategoryStatistics> statistics;
	
	private CategoryStatistics totalStatistics;
	
	
	
	public AssessmentSummary(String name,
			List<CategoryStatistics> statistics,
			CategoryStatistics totalStatistics)
	{
		super();
		this.name = name;
		this.statistics = statistics;
		this.totalStatistics = totalStatistics;
	}
	
	public static AssessmentSummary getAssessmentSummary()
	{
        // FIXME: Get per vulnerability type statistics that we can include in an email notification and/or on a dashboard
		return null;
	}

	
	public void setName(String name) {
		this.name = name;
	}

	public void setStatistics(List<CategoryStatistics> statistics)
	{
		this.statistics = statistics;
	}

	public void setTotalStatistics(CategoryStatistics totalStatistics)
	{
		this.totalStatistics = totalStatistics;
	}

	final static String NL = System.getProperty("line.separator");
	
	public String toString()
	{
		return getCSVHeader() + NL + getCSV();
	}
	
	public String getCSV()
	{
		StringBuffer s = new StringBuffer();
		
//		s.append(name + "," + 
//		totalStatistics.getVulnerabilityType() + "," + 
//		totalStatistics.getTotal() + "," + 
//		totalStatistics.getHighSeverityHighConfidence() + "," +
//		totalStatistics.getMediumSeverityHighConfidence() + "," +
//		totalStatistics.getLowSeverityHighConfidence() + "," +
//		totalStatistics.getHighSeverityMediumConfidence() + "," +
//		totalStatistics.getMediumSeverityMediumConfidence() + "," + 
//		totalStatistics.getLowSeverityMediumConfidence() + "," +
//		totalStatistics.getLowConfidence() + NL);

		for (CategoryStatistics statistic : statistics)
		{
			s.append(name + "," + 
					statistic.getVulnerabilityType() + "," + 
					statistic.getTotal() + "," + 
					statistic.getHighSeverityHighConfidence() + "," +
					statistic.getMediumSeverityHighConfidence() + "," +
					statistic.getLowSeverityHighConfidence() + "," +
					statistic.getHighSeverityMediumConfidence() + "," +
					statistic.getMediumSeverityMediumConfidence() + "," + 
					statistic.getLowSeverityMediumConfidence() + "," +
					statistic.getLowConfidence() + NL);
		}
		
		return s.toString();
	}
	
	public String getCSVHeader()
	{
		return "Application" + "," + 
				"Vulnerability Type" + "," + 
				"Total" + "," + 
				"High severity/High confidence" + "," +
				"Medium severity/High confidence" + "," +
				"Low severity/High confidence" + "," +
				"High severity/Medium confidence" + "," +
				"Medium severity/Medium confidence" + "," +
				"Low severity/Medium confidence" + "," +
				"Low confidence" + NL;
	}
	
	private static String notEmpty(String s)
	{
		return (s == null || s.length() == 0) ? "0" : s;
	}
	
	private static int parseIntNotEmpty(String s)
	{
		return Integer.parseInt(notEmpty(FileSearch.findFirst(s, ">([0-9]+)<")));
	}
}

class CategoryStatistics
{
	private String vulnerabilityType;
	
	private int total;
	private int highSeverityHighConfidence;
	private int mediumSeverityHighConfidence;
	private int lowSeverityHighConfidence;
	private int highSeverityMediumConfidence;
	private int mediumSeverityMediumConfidence;
	private int lowSeverityMediumConfidence;
	private int lowConfidence;
	
	public CategoryStatistics(String vulnerabilityType, int highSeverityHighConfidence,
			int mediumSeverityHighConfidence, int lowSeverityHighConfidence,
			int highSeverityMediumConfidence,
			int mediumSeverityMediumConfidence,
			int lowSeverityMediumConfidence, int lowConfidence) {
		super();
		this.vulnerabilityType = vulnerabilityType;
		this.highSeverityHighConfidence = highSeverityHighConfidence;
		this.mediumSeverityHighConfidence = mediumSeverityHighConfidence;
		this.lowSeverityHighConfidence = lowSeverityHighConfidence;
		this.highSeverityMediumConfidence = highSeverityMediumConfidence;
		this.mediumSeverityMediumConfidence = mediumSeverityMediumConfidence;
		this.lowSeverityMediumConfidence = lowSeverityMediumConfidence;
		this.lowConfidence = lowConfidence;
	}

	public String getVulnerabilityType() {
		return vulnerabilityType;
	}

	public int getHighSeverityHighConfidence() {
		return highSeverityHighConfidence;
	}

	public int getMediumSeverityHighConfidence() {
		return mediumSeverityHighConfidence;
	}

	public int getLowSeverityHighConfidence() {
		return lowSeverityHighConfidence;
	}

	public int getHighSeverityMediumConfidence() {
		return highSeverityMediumConfidence;
	}

	public int getMediumSeverityMediumConfidence() {
		return mediumSeverityMediumConfidence;
	}

	public int getLowSeverityMediumConfidence() {
		return lowSeverityMediumConfidence;
	}

	public int getLowConfidence() {
		return lowConfidence;
	}
	
	public int getTotal()
	{
		return getHighSeverityHighConfidence() + getMediumSeverityHighConfidence() + getLowSeverityHighConfidence() +
				getHighSeverityMediumConfidence() + getMediumSeverityMediumConfidence() + getLowSeverityMediumConfidence() +
				getLowConfidence();
	}

}
