package org.sif;

public class Job
{
	private JobStatus status;
	
	public Job()
	{
		status = JobStatus.IN_PROGRESS;
	}
	
	public JobStatus getStatus()
	{
		return status;
	}

	public void setStatus(JobStatus status)
	{
		this.status = status;
	}

}
