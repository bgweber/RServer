import java.net.InetAddress;
/**
 * A task is an instance of a schedule that has been selected to run. 
 * 
 * @author beweber
 */ 
public class Task {

	/** name of the task to run */
	private String taskName; 

	/** the path in perforce to the task's base directory, starting with //GAI.. */
	private String perforcePath; 
	
	/** the name of the .r file to run, with no path */ 
	private String rScript;

	/** when the task started running */
	private long startTime; 

	/** the completion time */
	private long endTime;

	/** the owner of the task (email address) */
	private String owner;
	
	/** should the owner be emailed on success? */  
	private boolean emailOnSuccess;  

	/** command line arguments */
	private String parameters = ""; 

	/** is this a shiny app? */
	private boolean shinyApp = false; 

	/** if this is a shiny app, what's the URL */ 
	private String shinyUrl = ""; 

	/** the PID of the process this task represents */ 
	private int PID = 0; 
	
	/** was this process terminated early? */ 
	private boolean aborted = false;  
	
	/** the outcome (success/failure/terminated) */
	private String outcome; 

	/** the path to the output file */ 
	private String rout; 

	/** a job ID for referencing this task */ 
	private String jobId = null;
	
	/** is this a python task? */ 
	private boolean isPython = false;

	/**
	 * Constructs a task based on the passed in schedule. The start time of the task is 
	 * set as the current system time. 
	 * 
	 * @param schedule - The schedule with the task to being executing 
	 */
	public Task(Schedule schedule) throws Exception { 
		this.taskName = schedule.getTaskName();
		this.perforcePath = schedule.getPerforcePath();
		this.rScript = schedule.getrScript();
		this.startTime = System.currentTimeMillis();
		this.owner = schedule.getOwner();
		this.emailOnSuccess = schedule.getEmailOnSuccess();
		this.parameters = schedule.getParameters();
		this.shinyApp = schedule.getShinyApp();
		this.jobId = schedule.getJobId();
		this.isPython = !rScript.toLowerCase().endsWith(".r"); 

		if (shinyApp) {
			shinyUrl = InetAddress.getLocalHost().getCanonicalHostName() + ":" + parameters.split(" ")[0];
		}
	}

	
	/* 
	 * Setters  
	 */ 
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setPID(int pID) {
		PID = pID;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public void setRout(String rout) {
		this.rout = rout;
	}
	

	/* 
	 * Getters 
	 */ 
	public String getTaskName() {
		return taskName;
	}

	public String getPerforcePath() {
		return perforcePath;
	}

	public String getrScript() {
		return rScript;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public String getOwner() {
		return owner;
	}

	public boolean getEmailOnSuccess() {
		return emailOnSuccess;
	}

	public String getParameters() {
		return parameters;
	}

	public boolean getShinyApp() {
		return shinyApp;
	}

	public String getShinyUrl() {
		return shinyUrl;
	}

	public int getPID() {
		return PID;
	}

	public boolean getAborted() {
		return aborted;
	}

	public String getOutcome() {
		return outcome;
	}

	public String getRout() {
		return rout;
	} 
	
	public String getJobId() {
		return jobId; 
	} 	
	
	public boolean getIsPython() { 
		return isPython;
	} 
}
