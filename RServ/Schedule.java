// // Copyright (C) 2016 Electronic Arts Inc.  All rights reserved.
import java.util.Calendar;
import java.util.Date;
/**
 * Represents a schedule for running a task at a specified frequency.  
 * A schedule is a prototype for running a task, and does not store the output of status
 * of a specific instance of a task.
 * 
 * The main logic in this class is to determine when a schedule should run based on the current
 * time and the time specified in the configuration file. 
 *  
 * @author beweber 
 */
public class Schedule {

	/** the types of frequencies to run support */ 
	public enum Frequency { Hourly, Daily, Weekly, Monthly, Now, Never } 

	/** name of the task to run */
	private String taskName; 

	/** the path in perforce to the task's base directory, starting with //GAI.. */
	private String perforcePath; 
	
	/** the name of the .r file to run, with no path */ 
	private String rScript;
	
	/** how often to run the schedule */
	private Frequency frequency;
	
	/** when to run the task */
	private String time; 
	
	/** the owner of the task (email address) */
	private String owner;
	
	/** should the owner be emailed on success? */  
	private boolean emailOnSuccess = false;  
	
	/** the next time to run the task */
	private long nextRunTime = 0; 
	
	/** command line arguments */
	private String parameters = ""; 

	/** is this a shiny app */
	private boolean shinyApp = false; 

	/** a job ID for referencing tasks created from this schedule (ad-hoc schedules only) */ 
	private String jobId = null;
	
	 /**
	  * Creates a schedule from an entry in the schedule file.  
	  */
	public static Schedule parseSchedule(String csvLine, boolean firstLoad) {
		String[] atts = csvLine.split(","); 
		
		if (atts.length <= 6) {
			return null;
		} 

		Schedule schedule = new Schedule();
		schedule.taskName = atts[0].trim().replaceAll(" ", "_"); 
		schedule.perforcePath = atts[1].trim(); 
		schedule.rScript = atts[2].trim();
		schedule.time = atts[4].trim(); 
		schedule.owner = atts[5].trim();
		
		if (atts.length > 6) {
			schedule.emailOnSuccess = "true".equalsIgnoreCase(atts[6].trim());
		}

		if (atts.length > 7) {
			schedule.parameters = atts[7].trim();
		}

		if (atts.length > 8) {
			schedule.shinyApp = "true".equalsIgnoreCase(atts[8].trim());
		} 

		// check if the frequency is valid 
		try {	
			schedule.frequency = Frequency.valueOf(atts[3].trim()); 
		}
		catch (Exception e) {
			return null;
		} 
		
		// check if the schedule is valid   
		if (schedule.setNextRunTime(firstLoad)) {
			return schedule;			
		}
		else {
			return null;
		}
	}

	/**
	 * Default constructor. 
	 */
	private Schedule() {
	}

	/**
	 * Constructor for ad-hoc schedules. 
	 */
	public Schedule(String taskName, String perforcePath, String rScript, String owner, boolean emailOnSuccess, String params, boolean shinyApp, String jobId) {
		this();
		this.taskName = taskName.replaceAll(" ", "_");
		this.perforcePath = perforcePath;
		this.rScript = rScript;
		this.owner = owner; 
		this.emailOnSuccess = emailOnSuccess; 
		this.nextRunTime = System.currentTimeMillis();
		this.frequency = Frequency.Now;
		this.parameters = params;  
		this.shinyApp = shinyApp; 
		this.jobId= jobId;  
	}
	
	/**
	 * Sets the next time to run the schedule based on the frequency and time. 
	 * 
	 * If the schedule is invalid, false is returned.  
	 */
	public boolean setNextRunTime(boolean firstLoad) {
		try {
			
			// right now  
			if (frequency == Frequency.Now) { 
				
				if (firstLoad == true) {
					nextRunTime = System.currentTimeMillis();
				} 
				else {
					nextRunTime = Long.MAX_VALUE;
				}  
			}
			// hourly
			else if (frequency == Frequency.Hourly) { 
				
				String[] atts = time.split(":");
				int minute = Integer.parseInt(atts[1]);

				Calendar today = Calendar.getInstance();
				today.set(Calendar.MINUTE, minute); 
				today.set(Calendar.SECOND, 0);
				
				long scheduledTime = today.getTimeInMillis();
				if (scheduledTime > System.currentTimeMillis()) {
					nextRunTime = scheduledTime;
				} 
				else {
					nextRunTime = scheduledTime + 1L*60L*60L*1000L;
				}				
			}
			// daily 
			else if (frequency == Frequency.Daily) {
				
				String[] atts = time.split(":");
				int hour = Integer.parseInt(atts[0]);
				int minute = Integer.parseInt(atts[1]);

				Calendar today = Calendar.getInstance();
				today.set(Calendar.AM_PM, 0);
				today.set(Calendar.HOUR, hour);
				today.set(Calendar.MINUTE, minute);
				today.set(Calendar.SECOND, 0);
				
				long scheduledTime = today.getTimeInMillis();
				if (scheduledTime > System.currentTimeMillis()) {
					nextRunTime = scheduledTime;
				} 
				else {
					nextRunTime = scheduledTime + 24L*60L*60L*1000L;
				}				
			} 
			// weekly
			else if (frequency == Frequency.Weekly) {	 
				String day = time.split(" ")[0];  
				int weekday = 0; 
				
				switch (day.toLowerCase()) {
				case "sun":
					weekday = 1;
					break;
				case "mon":
					weekday = 2;
					break;
				case "tue":
					weekday = 3;
					break;
				case "wed":
					weekday = 4;
					break;
				case "thu":
					weekday = 5;
					break;
				case "fri":
					weekday = 6;
					break;
				case "sat":
					weekday = 7;
					break;					
				default:
					return false; 
				}

				String[] atts = time.split(" ")[1].split(":");
				int hour = Integer.parseInt(atts[0]);
				int minute = Integer.parseInt(atts[1]);

				Calendar today = Calendar.getInstance();
				today.set(Calendar.AM_PM, 0);
				today.set(Calendar.HOUR, hour);
				today.set(Calendar.MINUTE, minute);
				today.set(Calendar.SECOND, 0); 

				for (int i=0; i<= 7; i++) { 
					if (today.get(Calendar.DAY_OF_WEEK) == weekday) {

						long scheduledTime = today.getTimeInMillis();
						if (scheduledTime > System.currentTimeMillis()) {
							nextRunTime = scheduledTime; 
						} 
						else { 
							nextRunTime = scheduledTime + 7L*24L*60L*60L*1000L;  
						}				
						
						return true; 
					}
					else {
						today.add(Calendar.DAY_OF_WEEK, 1);
					}
				}
				
				return false; 
			}			
			// monthly 
			else if (frequency == Frequency.Monthly) {	 
				int day = Integer.parseInt(time.split(" ")[0]);  
 
				String[] atts = time.split(" ")[1].split(":");
				int hour = Integer.parseInt(atts[0]);
				int minute = Integer.parseInt(atts[1]);

				Calendar today = Calendar.getInstance();
				today.set(Calendar.AM_PM, 0);
				today.set(Calendar.DAY_OF_MONTH, day);
				today.set(Calendar.HOUR, hour);
				today.set(Calendar.MINUTE, minute);
				today.set(Calendar.SECOND, 0);
				
				long scheduledTime = today.getTimeInMillis();
				if (scheduledTime > System.currentTimeMillis()) {
					nextRunTime = scheduledTime;
				} 
				else {
					today.set(Calendar.MONTH, today.get(Calendar.MONTH) + 1);
					nextRunTime = today.getTimeInMillis(); 
				} 
			} 
			// Never
			else if (frequency == Frequency.Never) {	 
				nextRunTime = Long.MAX_VALUE;
			}

			return true;
		}
		catch (Exception e) { 
			return false; 
		} 		
	}

	/**
	 * Returns a summary of the schedule. 
	 */
	public String toString() { 
		return taskName + " " + rScript + " " + frequency + " " + time + " " + owner + " " 
				+ emailOnSuccess + " " + new Date(nextRunTime).toString(); 
	} 	 
	
	/** 
	 * Unit test.  
	 */ 
	public static void main(String[] args) { 
		System.out.println(parseSchedule("EmailTest,//GAI/datascience/tasks/daily/EmailTest,sendmail.r,Now,, beweber@ea.com, true,,false", true)); 
		System.out.println(parseSchedule("EmailTest,//GAI/datascience/tasks/daily/EmailTest,sendmail.r,Now,, beweber@ea.com, true,,false", false)); 
		System.out.println(parseSchedule("EmailTest,//GAI/datascience/tasks/daily/EmailTest,sendmail.r,Hourly,19:35, beweber@ea.com, true,,false", true)); 
		System.out.println(parseSchedule("EmailTest,//GAI/datascience/tasks/daily/EmailTest,sendmail.r,Daily,11:00, beweber@ea.com, true,,false", true)); 
		System.out.println(parseSchedule("EmailTest,//GAI/datascience/tasks/daily/EmailTest,sendmail.r,Weekly,Sun 18:00, beweber@ea.com, false,,false", true)); 
		System.out.println(parseSchedule("EmailTest,//GAI/datascience/tasks/daily/EmailTest,sendmail.r,Monthly,24 11:30, beweber@ea.com, true,,false", true)); 
		System.out.println(parseSchedule("EmailTest,//GAI/datascience/tasks/daily/EmailTest,sendmail.r,Never,, beweber@ea.com, true,,false", true)); 

		System.out.println(parseSchedule("PvZ2_Model, //GAI/popcap/,tasks/berfu/PvZ2_Model/pvz2_model_data.R,Monthly,27 11:45,bduraksen@popcap.com,TRUE,,", true)); 

//		PvZ2_Model, //GAI/popcap/,tasks/berfu/PvZ2_Model/pvz2_model_data.R,Monthly,27 11:45,bduraksen@popcap.com,TRUE,,
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

	public Frequency getFrequency() {
		return frequency;
	}

	public String getTime() {
		return time;
	}

	public String getOwner() {
		return owner;
	}

	public boolean getEmailOnSuccess() {
		return emailOnSuccess;
	}

	public long getNextRunTime() {
		return nextRunTime;
	}

	public String getParameters() {
		return parameters;
	}

	public boolean getShinyApp() {
		return shinyApp;
	}
	
	public String getJobId() {
		return jobId; 
	}
}
