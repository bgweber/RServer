package com.ea.rserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.TreeMap;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hyperic.sigar.Sigar;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
/**
 * Main class for running the R Server. 
 */ 
public class RServer {    
	
	public static final String ServerDate= "April 10, 2016";  

	// thread update speeds   
	private static long mainThreadRefresh = 2000; 
	private static long statusThreadRefresh = 5000; 
	private static long gcRefresh = 60000;    
	private static int completedTaskSize = 100;   
	private static int logHistorySize = 20;  
	private static int maxJobFiles = 500;    
	private static int pageRefreshTime = 5000; 
	private static int performanceLogSize = 200;     
	 
	// server paths
	private static String wampWWW = "C:/wamp/www/";
	private static String logDir = wampWWW + "/RServer/logs/"; 
	private static String requestsDir = wampWWW + "/RServer/requests/";
	private static String routDir = wampWWW + "/RServer/Rout/";     
	private static String jobsDir = wampWWW + "/RServer/jobs/";     
	private static String indexPagePath = wampWWW + "/RServer/index.php";  	   
	private static String performanceLogDir = ".";    

	// email config  
	private static boolean enableEmail = false;        
	private static String serverEmail = "RServer@YourDomain.com";       
	private static String serverAdmin = "RserverAdmin@YourDomain.com";  
	private static String smtp = "mail.yourdomain.com";   
	private static String gmailToken = null;  

	// git paths 
	private static String gitTool = "C:/RServerGit/Update.bat";    
	private static String gitBase = "C:/RServerGit/RServer";     
	private static String schedulePath = "/tasks/Schedule.csv"; 	   
	
	// working directory    
	private static String scriptPath = "scripts/";   
	private static String rPath = "C:/Program Files/R/R-3.2.4revised/bin/";   

	/** The last time that git ran */  
	private long lastGitUpdate = 0;  
	
	/** when the server launched */  
	private static long bootTime = System.currentTimeMillis(); 
	 
	/** The list of schedules with a regular frequency */
	private ArrayList<Schedule> regularSchedules = new ArrayList<Schedule>(); 
	
	/** The list of ad hoc schedule to run */
	private ArrayList<Schedule> adhocSchedules = new ArrayList<Schedule>();   
	
	/** Currently active tasks */
	private TreeMap<String, Task> runningTasks = new TreeMap<String, Task>();   
 
	/** Completed tasks (regular and ad hoc) */
	private LinkedList<Task> completed = new LinkedList<Task>();	 

	/** Recent log messages */ 
	private LinkedList<String> logEntries = new LinkedList<String>();	  

	/** last garbage collection run */  
	private static long lastGC = System.currentTimeMillis(); 

	/** for monitoring system usage */
	private Sigar sigar = new Sigar();

	/** store the last CPU reading, sigar.getCpuPerc() doesn't work if called too frequently */
	private double cpuLoad = 0.0; 

	/** have schedules been loaded yet? Only run start-up tasks once */ 
	boolean firstLoad = true; 
 
	// String constants 
	private static final String GitTask = "Git";  

	// host config   
	private static String hostName = null;
	private static String fullHostName = null;

	// record how long updates took
	private long updateHtmlTime = 0;
	private long updatePerfLogTime = 0;
	
	/**
	 * 	Start the server! 
	 */ 
	public static void main(String[] args) {    

		try { 
			hostName = InetAddress.getLocalHost().getHostName();   
			fullHostName = InetAddress.getLocalHost().getCanonicalHostName();    
		}
		catch (Exception e) {  
			e.printStackTrace();	
		}
	
		// search for the R path 
		File rDir = new File("C:/Program Files/R");		
		if (rDir.exists()) {
			File[] files = rDir.listFiles();
			
			if (files.length > 0) {
				File dir = files[files.length - 1];
				rPath = dir.getParent() + "\\" + dir.getName() + "\\bin\\";
			}
		}
		
		// load system properties  
		mainThreadRefresh = Long.parseLong(getProperty("mainThreadRefresh", "" + mainThreadRefresh));  
		statusThreadRefresh = Long.parseLong(getProperty("statusThreadRefresh", "" + statusThreadRefresh));
		gcRefresh = Long.parseLong(getProperty("gcRefresh", "" + gcRefresh));
		completedTaskSize = Integer.parseInt(getProperty("completedTaskSize", "" + completedTaskSize)); 
		logHistorySize = Integer.parseInt(getProperty("logHistorySize", "" + logHistorySize)); 		
		maxJobFiles = Integer.parseInt(getProperty("maxJobFiles", "" + maxJobFiles)); 		
		pageRefreshTime = Integer.parseInt(getProperty("pageRefreshTime", "" + pageRefreshTime)); 		 
		performanceLogSize = Integer.parseInt(getProperty("performanceLogSize", "" + performanceLogSize)); 		
		performanceLogDir = getProperty("performanceLogDir", performanceLogDir); 
		
		// server config 
		enableEmail = Boolean.parseBoolean(getProperty("enableEmail", "" + enableEmail)); 
		gmailToken = getProperty("gmailToken", gmailToken); 
		serverEmail = getProperty("serverEmail", serverEmail); 
		serverAdmin = getProperty("serverAdmin", serverAdmin);
		smtp = getProperty("smtp", smtp); 		
		scriptPath = getProperty("scriptPath", scriptPath);
		rPath = getProperty("rPath", rPath);     
		
		// set up Git directories 
		gitTool = getProperty("gitTool", gitTool);  
		gitBase = getProperty("gitBase", gitBase);   
		schedulePath = gitBase + getProperty("schedulePath", "" + schedulePath);  
		
		// Set up wamp directories 
		wampWWW = getProperty("wampWWW", wampWWW);     
		logDir = wampWWW + "/RServer/logs/";
		requestsDir = wampWWW + "/RServer/requests/";
		routDir = wampWWW + "/RServer/Rout/";     
		jobsDir = wampWWW + "/RServer/jobs/";      
		indexPagePath = wampWWW + "/RServer/index.php";  	    

		// create necessary Wamp directories if not found 
		String[] directories = {  
						  "/RServer", 
						  "/RServer/jobs", 
						  "/RServer/logs",
						  "/RServer/reports",
						  "/RServer/requests",
						  "/RServer/Rout",
						  "/RServer/reports/RServer", 
						};

		for (String dir : directories) {
			File file = new File(wampWWW + dir);
			
			if (!file.exists()) {
				file.mkdir(); 
			}
		}
				
		// set up scripts path 
		if (!new File(scriptPath).exists()) {
			new File(scriptPath).mkdir();
		}
		 
		// delete prior performance log
		 File logFile = new File(performanceLogDir + "/" + hostName + ".csv"); 
		 if (logFile.exists()) {
			 logFile.delete();
		 } 
		
		// clean up orphan processes (Shiny)   
		killShinyProcesses(); 
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				killShinyProcesses();				    
			}  
		}); 	

		// start the server 
		new RServer().run();		
	} 
 
	/**
	 * Kills ALL RTerm process on the system. 
	 * 
	 * Note: this may impact other R task running on the system. 
	 */
	public static void killShinyProcesses() {

		try {
			Process p = Runtime.getRuntime().exec("taskkill /F /IM Rterm.exe"); 
	        p.waitFor();
	    }  
		catch (Exception err) {
	        err.printStackTrace();
	    }		
	}	
	 
	/**
	 * Main server loop. 
	 * 
	 * The server status thread updates the dashboard file. 
	 * 
	 * The main thread checks for scheduels to run and new ad hoc schedules, 
	 * does git updates, and manages creating working directories. 
	 */
	public void run() {  
		Thread.currentThread().setName("Main server thread"); 		 
		log("Using R Directory: " + rPath);

		// force Git to refresh on start up 
		adhocSchedules.add(new Schedule(GitTask, "", "", serverAdmin, false, "", false, null));   
		
		// create the server status thread  
		new Thread() {
			public void run() {
				Thread.currentThread().setName("Server status thread");
				
				while (true) {
					try { 
						long time = System.currentTimeMillis();
						updateServerStatus();  
						updateHtmlTime = System.currentTimeMillis() - time;
						 
						time = System.currentTimeMillis();
						updatePerformaceLog();  
						updatePerfLogTime = System.currentTimeMillis() - time;					
						
						Thread.sleep(statusThreadRefresh);  
					}
					catch (Exception e) {
						log("ERROR: updating server status (index.php): " + e.getMessage());  
					}					
				}				
			}
		}.start();  
		 
		// main server loop 
		while (true) {    
			try {
				checkSchedules();			 	 		
				 
				// GC time?
				if ((System.currentTimeMillis() - lastGC) > gcRefresh) {  
					System.gc(); 
					lastGC = System.currentTimeMillis(); 
				}
				
				Thread.sleep(mainThreadRefresh);
			}  
			catch (Exception e) { 
				log("ERROR: main server thread: " + e.getMessage()); 
				e.printStackTrace(); 
				
				sendEmail(serverAdmin, "R Server Error",
						"Excepton in main server thread: " + e.getMessage());   
			} 
		} 		
	} 

	/** 
	 * Check if any schedules need to run. Launches a new thread for each running schedule. 
	 * Updates Git if an ad-hoc schedule is running, or the git task is scheduled.
	 * 
	 * Method overview
	 * ---------------
	 *  1. Check the requests directory for any ad-hoc task to run
	 *  2. Schedule the ad-hoc tasks to run
	 *  3. Check if any regular schedules should run 
	 *  4. Update git if necessary  
	 *  5. Set up the working directories for the new tasks 
	 *  6. Run each task in a new thread 
	 */
	public void checkSchedules() {
		
		// 1. check for new ad hoc requests (poll the request directory) 
		File requestDir = new File(requestsDir); 
		if (requestDir.exists()) {

			for (File file : requestDir.listFiles()) {	
				try {
					BufferedReader reader = new BufferedReader(new FileReader(file)); 
					String line = reader.readLine();			

					// check if this is a valid file 
					if (line != null) {					
						String[] atts = line.split(",");

						// ad-hoc request (.ad file) 
						if (file.getName().endsWith(".ad")) {					  
							
							if (atts.length < 8) {   
								log("Invalid schedule: " + line);
							}
							else {   
								String task = atts[0].trim(); 
								String path = atts[1].trim();
								String script = atts[2].trim();
								String owner = atts[3].trim(); 
								boolean emailOnSuccess = "true".equalsIgnoreCase(atts[4].trim()); 
								String params = atts[5].trim();
								boolean shiny = "true".equalsIgnoreCase(atts[6].trim());     
								String jobId = atts[7].trim(); 
		
								// do some basic error checking  
								if ((task.length() == 0 || path.length() == 0 || script.length() == 0 || owner.length() == 0)  
										&& !task.equalsIgnoreCase(GitTask)) {  
									
									log("Invalid schedule submitted: " + line);  
									updateJobStatus(jobId, "Invalid", null);  
								} 
								else {  
									Schedule schedule = new Schedule(task, path, script, owner, emailOnSuccess, params, shiny, jobId);
									log("Loaded request for ad-hoc schedule: " + schedule.toString());
									adhocSchedules.add(schedule);  
									
									// update the status of the job 
									updateJobStatus(jobId, "Submitted", null); 
								}
							}
						}
						// kill process request (.kp file) 
						else if (file.getName().endsWith(".kp")) {					
							
							synchronized (RServer.this) {			 
								String task = atts[0].trim(); 								
								Task runningTask = runningTasks.get(task); 
								 
								if (runningTask != null && runningTask.getPID() > 0) {
									log("Killing Process: " + task + " " + runningTask.getPID());
									
									try {  
										runningTask.setAborted(true);   
										Process p = Runtime.getRuntime().exec("taskkill /F /T /PID " + runningTask.getPID());  
								        p.waitFor(); 
								    }  
									catch (Exception err) {
										log("ERROR: failed to run task kill for : " + task + " " + runningTask.getPID()); 
								        err.printStackTrace();
								    }		 								
								} 
								else {
									log("ERROR: unable to terminate process, not found: " + task);
								}						 
							}
						}
					}
					else {
						log("Empty request file found, ignoring: " + file.getName()); 
					}
					
					reader.close();
					file.delete();
				} 
				catch (Exception e) { 
					e.printStackTrace();
					log("ERROR: Exception while reading ad-hoc schedule file: " + file + " " + e.getMessage()); 
				} 
			}  
		}
		else {  
			log("ERROR: directory for ad-hoc schedules not found: " + requestDir);   
		} 
		  		 		
		// 2. schedule the ad-hoc tasks to run (ignore duplicates)  
		ArrayList<Task> newTasks = new ArrayList<Task>();		 
		boolean updateGit = false;
		 
		synchronized (RServer.this) {			 
			
			for (Schedule schedule : adhocSchedules) { 				
				if (runningTasks.containsKey(schedule.getTaskName()) == false) {
					
					try {
						Task task = new Task(schedule); 						
						newTasks.add(task);  
						runningTasks.put(task.getTaskName(), task);

						// always update git for ad-hoc tasks 
						updateGit = true;  
					}
					catch (Exception e) {
						log("ERROR: failed to schedule script: " + e.getMessage());
					}
				}				
				else { 
					log("ERROR: The schedule is already running: " + schedule.getTaskName());
				}
			}
			
			adhocSchedules.clear(); 
		} 		
 
		// 3. check if any regular schedules should run  
 		for (Schedule schedule : regularSchedules) {
			if (System.currentTimeMillis() > schedule.getNextRunTime()) {				
				
				synchronized (RServer.this) {
					if (runningTasks.get(schedule.getTaskName()) == null) {
					
						try {
							Task task = new Task(schedule); 
							newTasks.add(task);    							
							runningTasks.put(task.getTaskName(), task); 
							schedule.setNextRunTime(false);

							// update git? 
							if (GitTask.equalsIgnoreCase(task.getTaskName())) {
								updateGit = true;  
							}
						} 
						catch (Exception e) {
							log("ERROR: failed to schedule script: " + e.getMessage());
						}
					} 
				}				
			}
		}  

 		// 4. update git in the main server thread if necessary 
		if (updateGit) {
			log("Updating Git files");
			
			try {
				lastGitUpdate = System.currentTimeMillis();
				
				Process process = new ProcessBuilder(gitTool).start(); 			
				int result = process.waitFor();
 
				if (result > 0) { 
					log("Git update failed due to system exit value"); 
					
					sendEmail(serverAdmin, "Git failure on R Server",
							"Git update failed due to system exit value"); 
				}  
			} 
			catch (Exception e) {
				log("Git update failed due to Excepton: " + e.getMessage());

				sendEmail(serverAdmin, "Git failure on R Server",
						"Git update failed due to IOExcepton: " + e.getMessage()); 
			}		 			 
			
			regularSchedules = loadSchedules();
			log("Git Update Completed"); 
		} 
 
		// 5. set up the working directories for the new tasks 
		for (Task task : newTasks) {  
			try { 
				if (GitTask.equalsIgnoreCase(task.getTaskName())) {
					continue;  
				}  
				
				File taskDir = new File(scriptPath + task.getTaskName());
				if (taskDir.exists()) {
					deleteDirectory(taskDir); 
				}
	
				if (taskDir.mkdir()) {
					File sourceDir = new File(gitBase + task.getGitPath().replace("//", "/")); 
					
					if (sourceDir.exists()) {
						copyDirectory(sourceDir, taskDir, false); 
					}
					else { 
						log("ERROR: Invalid Git directory for " + task.getTaskName() + ": " + task.getGitPath() + " ");  
					}
					
				}
				else {
					log("ERROR: unable to make working directory for task: " + task.getTaskName() + " ");  
				} 
			}
			catch (Exception e) {
				log("ERROR: Error creating working directory for task: " + task.getTaskName() + ", " + e.getMessage());  
			}
		}
 
		// 6. Run each task in a new thread  
		for (Task task : newTasks) {
			new Thread() { 
				public void run() {
					try {
						Thread.currentThread().setName("Task: " + task.getTaskName()); 
						runTask(task);
					}
					catch (Exception e) {						
						log("ERROR: Running task: " + task.getTaskName() + ": " + e.getMessage());   
					}
				} 
			}.start(); 
		}
	} 
	
	/**
	 * Runs the R script for the given task.  
	 * Copies the Rout file to the server. Sends out an email with the outcome of the script. 
	 * 
	 * Method overview
	 * ---------------
	 *  1. Create a batch file to run the R script 
	 *  2. Run the batch file as a new process and get the process ID
	 *  3. Wait for the process to complete 
	 *  4. Check the outcome of the R Script 
	 *  5. Copy the Rout file to the server 
	 *  6. Email out the results  
	 *  7. Mark the task as completed
	 */
	private void runTask(Task task) {   
		
		// if this is a Git task, update the web directories 
		if (GitTask.equalsIgnoreCase(task.getTaskName())) {

			// copy supporting web files to the base RServer direcotry
			long time = System.currentTimeMillis();
			copyDirectory(new File(gitBase + "/www"), 
					new File(wampWWW + "/RServer"), true);  			    
			log("Updated web directory: " + (System.currentTimeMillis() - time) + " ms"); 			
			
		}
		
		// update the status of the job  
		updateJobStatus(task.getJobId(), "Running", null);
		
		try {
			
			task.setOutcome(GitTask.equalsIgnoreCase(task.getTaskName()) ? "" : "Failure");   
		
			// get the task directory    
			File taskDir = new File(scriptPath + task.getTaskName());
			File sourceDir = new File(gitBase + task.getGitPath().replace("//", "/"));
			
			if (taskDir.exists() && sourceDir.exists()) {
				 
				// 1. Create a batch file to run the R script 
				File batchFile = new File(taskDir.getPath() + "/run.bat");				
				BufferedWriter writer = new BufferedWriter(new FileWriter(batchFile));  
				writer.write("cd \"" + taskDir.getAbsolutePath() + "\"\n"); 				
 
				// Run an R Script 
				log("Running R Script: " + task.getTaskName());
				writer.write("\"" + rPath + "\"R CMD BATCH " 
						+ (task.getParameters().length() > 0 ? "\"--args " + task.getParameters() + "\" " : "")
				        + task.getrScript() + "\n" );					
				
				writer.close();    
				 
				// 2. Run the batch file as a new process and get the process ID
				Process process = new ProcessBuilder(batchFile.getAbsolutePath()).start(); 		
	
				// Use JNA to get the PID 
				try {
					Field field = process.getClass().getDeclaredField("handle");
					field.setAccessible(true);
					long id = field.getLong(process);
					
				    Kernel32 kernel = Kernel32.INSTANCE; 
				    HANDLE handle = new HANDLE(); 
				    handle.setPointer(Pointer.createConstant(id)); 
				    task.setPID(kernel.GetProcessId(handle));					
				} 
				catch (Exception e) {
					log("ERROR: unable to get PID for task: " + e.getMessage());
				}
								

				// 3. Wait for the process to complete 
				int result = process.waitFor(); 

				// 4. Check the outcome of the R Script  
				boolean errorInScipt = false;  
				StringBuffer output = new StringBuffer();
				File resultsFile = new File(taskDir.getPath() + "/" + task.getrScript() + ".Rout");
				
				if (!resultsFile.exists()) { 	// DEAL with how Rout files are named (uppercase R means no .r in File name) 
					resultsFile = new File(taskDir.getPath() + "/" + task.getrScript().split("\\.")[0] + ".Rout"); 
				}
				
				if (resultsFile.exists()) { 
					BufferedReader reader = new BufferedReader(new FileReader(resultsFile));
					String line = reader.readLine();
					
					while (line != null) {
						if (line.startsWith("Execution halted")) {
							errorInScipt = true;
						}
						
						output.append(line + "\n"); 
						line = reader.readLine(); 
					}
					
					reader.close();
				}

				// 5. Copy the Rout file to the server
				Calendar today = Calendar.getInstance();
				String outputDir = "output_" + today.get(Calendar.YEAR) + "-" + 
						(today.get(Calendar.MONTH) + 1) + "-" + today.get(Calendar.DAY_OF_MONTH);
						
				if (new File(routDir + outputDir).exists() == false) {
					new File(routDir + outputDir).mkdir();
				}
				
				task.setRout(outputDir + "/" + task.getTaskName() + "_" + System.currentTimeMillis()%100000 + ".Rout");  		 		
				File outputFile = new File(routDir + task.getRout()); 
				BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile)); 
				outputWriter.write(output.toString()); 
				outputWriter.close();  
				 
				// 6. Email out the results  
				// check if process was cancelled
				if (task.getAborted()) { 
					task.setOutcome ("Terminated");  
					log("R Script was aborted: " + task.getTaskName());   					
					
				}
				// check if process failed  
				else if (result > 0 || !resultsFile.exists()) {

					// Execution error during the script   
					if (errorInScipt) {
						log("ERROR: R Script failed during execution: " + task.getTaskName());   						 
					}
					// unable to run R script 
					else {
						log("ERROR: Unable to run R Script: " + task.getTaskName());  						 
					}

					String url = "http://" +fullHostName + "/RServer/Rout/" + task.getRout();  
					sendEmail(task.getOwner(), "R Script Failed: " + task.getTaskName(), 
							"The R Script failed: " + task.getTaskName() + "\n" +  
							"Link to Rout file: " + url + "\n\n" +  
  					        "Output from R Script:\n" + output.toString()); 					  
				}  
				// check if process compeleted  
				else {  
					task.setOutcome("Success"); 
					log("R Script completed successfully: " + task.getTaskName());   
					
					if (task.getEmailOnSuccess()) {
						String url = "http://" + fullHostName + "/RServer/Rout/" + task.getRout();
						sendEmail(task.getOwner(), "R Script completed: " + task.getTaskName(),
								"The R Script completed successfully: " + task.getrScript() + "\n\n" + 
								"Link to Rout file: " + url); 
					}
				} 				
			} 				 		
		}  
		catch (Exception e) {  
			log("ERROR: running script: " + e.getMessage());  
			
			sendEmail(task.getOwner(), "R Script Failed: " + task.getTaskName(), 
					"The R Script failed to complete: " + task.getTaskName() + "\n\n" +  
						e.getMessage() + "\n" + e); 
			
			sendEmail(serverAdmin, "R Script Failed: " + task.getTaskName(), 
					"The R Script failed to complete: " + task.getTaskName() + "\n\n" +  
						e.getMessage() + "\n" + e); 
		}    
		
		// 7. Mark the task as completed 
		synchronized (RServer.this) {   
			runningTasks.remove(task.getTaskName());    
			task.setEndTime(System.currentTimeMillis());  
			completed.add(0, task);
			
			if (completed.size() > completedTaskSize) {
				completed.remove(completedTaskSize);  
			}
		}		 		
		
		// update the status of the job      
		updateJobStatus(task.getJobId(), task.getOutcome(), task.getRout() != null ? "Rout/" + task.getRout() : ""); 
	}  
	
	/** 
	 * Updates the status of a Job. 
	 *  
	 * @param jobId - the job identifier  
	 * @param status - the new status  
	 * @param rout - a link to the .Rout file  
	 */ 
	private void updateJobStatus(String jobId, String status, String rout) { 
		try { 
			if (jobId == null) {
				return;
			}

			File dir = new File(jobsDir);			
			if (dir.exists()) {
				
				String jobFile = jobsDir + "/" + jobId + ".csv";  
				BufferedWriter writer = new BufferedWriter(new FileWriter(jobFile)); 		 
				writer.write("Status,rout\n");
				writer.write(status + "," + (rout != null ? rout : "") + "\n"); 
				writer.close();			 				 
			}
			else { 
				log("ERROR: unable to update job status: Jobs directory not found");
			}
			
			// check if we need to do some clean up  
			if (dir.exists()) {				
				if (dir.listFiles().length > maxJobFiles) { 
					File[] files = dir.listFiles(); 
					Arrays.sort(files);
					files[0].delete();
				}
			} 
			
		} 
		catch (Exception e) {
			log("ERROR: unable to update job status: " + jobId + " " + status + " :" + e.getMessage());
		}
	}	
	
	/**
	 * Copies all files from the source directory to the target directory. 
	 */
	private void copyDirectory(File sourceDir, File targetDir, boolean overwrite) { 
		try { 
		
			for (File file : sourceDir.listFiles()) {
				if (file.isDirectory()) {
					File newDir = new File(targetDir.getPath() + "/" + file.getName());
	
					if (newDir.mkdir() || overwrite == true) { 
						copyDirectory(file, newDir, overwrite);  
					}
					else {
						log("ERROR: unable to create directory: " + newDir); 
					}
				}
				else {
					File targetFile = new File(targetDir.getPath() + "/" + file.getName());
					
					try { 
						// NOTE: Using file channels, because the source file is read-only and the target needs to be writtable 

						// delete the target file if it already exists 
						if (targetFile.exists()) {
							targetFile.delete();
						}

						// copy the file 						
						FileInputStream inputStream = new FileInputStream(file);
						FileOutputStream outputStream = new FileOutputStream(targetFile);
						FileChannel inputChannel = inputStream.getChannel();
						FileChannel outputChannel = outputStream.getChannel();
						 
				        outputChannel.transferFrom(inputChannel, 0, inputChannel.size()); 
				        inputStream.close();
				        outputStream.close();  
					}
					catch (Exception e) {
						log("ERROR: unable to copy file to script directory: " + targetFile); 
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e) {  
			log("ERROR: copying working directory failed: " + e.getMessage() + " source: " + sourceDir + " target: " + targetDir);    
		} 
	}
	
	/**
	 * Delete a directory (recursive) 
	 */
	private void deleteDirectory(File dir) {
		try { 
		
			for (File file : dir.listFiles()) {
				if (file.isDirectory()) {
					deleteDirectory(file); 
				}
				else {				
					if (file.delete() == false) {
						log("ERROR: unable to delete directory: " + file);
					}
				}
			}
			
			if (dir.delete() == false) {
				log("ERROR: unable to delete directory: " + dir);  
			}
		}
		catch (Exception e) { 
			log("ERROR: deleting directories failed: " + e.getMessage());   
		} 
 	}   
	 
	/**
	 * Loads the schedules from the schedule file in the Git directory.
	 */
	private ArrayList<Schedule> loadSchedules() {
		
		try {		
			log("Updating Schedule: " + schedulePath); 			
			
			File file = new File(schedulePath); 
			if (!file.exists()) {
				log("ERROR: Schedule file not found"); 			
				return new ArrayList<Schedule>(); 
			}
			
			ArrayList<Schedule> schedules = new ArrayList<Schedule>();
			 
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			line = reader.readLine();	// eat the header
			
			while (line != null) { 
				Schedule schedule = Schedule.parseSchedule(line, firstLoad);
				
				if (schedule != null) {
					schedules.add(schedule);
				}
				else if (line.trim().length() > 0) {  
					log("ERROR: Problem loading schedule. Entry: " + line); 
				}

				line = reader.readLine(); 
			}
			 
			firstLoad = false;  
			reader.close(); 
			return schedules; 
		}
		catch (Exception e) {  
			log("ERROR: loading schedules failed: " + e.getMessage());  
			return new ArrayList<Schedule>(); 
		}		
	} 
	
	/**
	 * Sends out an email. 
	 */
	private void sendEmail(String toAddress, String subject, String body) {

		// Is email disabled?  
		if (!enableEmail) {
			log("Email is disabled"); 
			return;
		}
		
		try {
			Properties props = new Properties();
			props.put("mail.smtp.host", smtp); 
			Session session = Session.getDefaultInstance(props);
			
			// Use Gmail? 
			if (gmailToken != null) {
				
			  props.put("mail.smtp.auth", "true");
		      props.put("mail.smtp.starttls.enable", "true"); 
		      props.put("mail.smtp.port", "587");
		      props.put("mail.smtp.host", "smtp.gmail.com");  
				
		      session = Session.getInstance(props,
			      new javax.mail.Authenticator() {
			         protected PasswordAuthentication getPasswordAuthentication() {
			            return new PasswordAuthentication(serverEmail, gmailToken); 
			         }
			      });				
			}
			
	        Message msg = new MimeMessage(session);
	        msg.setFrom(new InternetAddress(serverEmail));
	        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
	        msg.setSubject(subject);
	        msg.setText(body);
	        Transport.send(msg); 			
		}
		catch (Exception e) { 
			log("ERROR: unable to send email: " + subject + ", " + e.getMessage()); 
		}
	}
	
	/** 
	 * Prints the message to the console and appends the message to the daily server log file. 
	 */
	private void log(String message) {   
		try {   
			System.out.println("Log - " + message);    
			
			synchronized(RServer.this) {
				logEntries.add(0, new Date(System.currentTimeMillis()).toString() + ": " + message);
	
				if (logEntries.size() > logHistorySize) {
					logEntries.remove(logHistorySize);
				} 
			}
			
			Calendar today = Calendar.getInstance();
			String errorFile = logDir + "log_" + today.get(Calendar.YEAR) + "-" + 
					(today.get(Calendar.MONTH) + 1) + "-" + today.get(Calendar.DAY_OF_MONTH); 
		
			FileWriter fileWriter = new FileWriter(errorFile, true);
			BufferedWriter writer = new BufferedWriter(fileWriter); 		
			writer.write(new Date(System.currentTimeMillis()).toString() + ": " + message + "\n");
			writer.close();			 
		}
		catch (Exception e) {    
			System.err.println("Failure during logging: " + e.getMessage()); 
			e.printStackTrace();  
		} 
	}
	
	/**  
	 * Generates the index.php file for the server dashboard.   
	 */
	private void updateServerStatus() {  
		try {
			StringBuffer page = new StringBuffer();
			
			// html header
			page.append("<html>\n<head>\n  <title>R Server</title>\n"); 
			page.append("  <script>\n");  
			page.append("  function reload() { \n");     
			page.append("    var isIE = /*@cc_on!@*/false || !!document.documentMode;\n");  
			page.append("    if (!isIE) location.reload();\n");  
			page.append("  }\n");  
			
			page.append("  setTimeout(reload, " + pageRefreshTime + ")\n");  
			page.append("  </script>\n");   
			page.append("  <link rel=\"stylesheet\" type=\"text/css\" href=\"rserver.css\">\n");    			

			// html body 
			page.append("</head>\n<body>\n"); 
			page.append("<div id=\"MainDiv\">\n\n");  

			// header  
			page.append("<div id=\"HeaderDiv\">\n");   
			page.append("  <div id=\"NavHeaderDiv\"></div>\n");       
			
			// links  
			page.append("  <ul id=\"NavBar\">\n");  
			page.append("    <li><a href=\"https://github.com/bgweber/RServer\">EA RServer</a></li>\n");	 		 
			page.append("    <li><a href=\"NewTask.php\">Submit New Task</a>\n");	 		
			page.append("    <li><a href=\"Reports.php\">Reports</a></li>\n");	 		
			page.append("    <li><a href=\"logs\">Server Logs</a></li>\n");	 		 
			page.append("    <li><a href=\"Rout\">Script Results</a></li>\n");	 		
			page.append("    <li><a href=\"reports/RServer/RServerTasks.html\">Task Report</a></li>\n");	 		
			page.append("  </ul>\n\n"); 
			page.append("</div>\n\n");	  		     
 
			// sub-header  
			page.append("  <div id=\"SubHeaderDiv\">\n");    
			page.append("    <h2 id=\"RHeader\">R Server: </h2>\n");  			  
			page.append("<?php if ((time() - " + System.currentTimeMillis()/1000 + ") < 10) { "    
					+ "echo \"<h2 id=\\\"ServerOnline\\\">Online</h2>\"; } else { "      
					+ "echo \"<h2 id=\\\"ServerOffline\\\">Offline</h2>\"; } ?>\n");    
			page.append("  </div>\n\n");	  		 

			// server status 
			page.append("  <div id=\"ContentDiv\">\n");     
			page.append("<h3 id=\"StatsHeader\">Server Statistics</h3>\n");  			 
			
			page.append("<table id=\"StatsTable\">\n");  
			page.append("  <thead>\n");  
			page.append("    <th>Property</th>\n    <th>Value</th>\n");    
			page.append("  </thead>\n"); 
			 
			if (hostName != null) {  
				page.append("    <tr><td>Server Name</td><td>" + hostName + "</td></tr>\n");
			}
  
			page.append("    <tr><td>Server Updated</td><td>" + new Date(System.currentTimeMillis()).toString() + "</td></tr>\n");
 
			Runtime runtime = Runtime.getRuntime();  
			String memory = ((runtime.totalMemory() - runtime.freeMemory())/1024/1024) + " / " + ((runtime.totalMemory())/1024/1024) + " MB";
			
			try {
				 double totalMem = sigar.getMem().getTotal()/1024/1024/1024.0;
				 double usedMem = (sigar.getMem().getTotal() - sigar.getMem().getFree())/1024/1024/1024.0;
				 cpuLoad = 1 - sigar.getCpuPerc().getIdle();
				 
				 totalMem = ((int)(totalMem*10))/10.0;
				 usedMem = ((int)(usedMem*10))/10.0;
				
				page.append("    <tr><td>Server CPU Usage</td><td>" + (((int)(cpuLoad*1000))/10.0) + "%</td></tr>\n");
				page.append("    <tr><td>Server Memory</td><td>" + usedMem + " / " + totalMem + " GB</td></tr>\n");
				
			}
			catch (Exception e) {
				log("ERROR: unable to update system stats: " + e.getMessage()); 
			}
			
			page.append("    <tr><td>Application Memory</td><td>" + memory + "</td></tr>\n");  
			page.append("    <tr><td>Thread Count</td><td>" + Thread.activeCount() + "</td></tr>\n");

			page.append("    <tr><td>Git Updated</td><td>" + (lastGitUpdate > 0 ?  
					new Date(lastGitUpdate).toString() : "No history") + "</td></tr>\n");  
			page.append("    <tr><td>Server Launched</td><td>" + new Date(bootTime).toString() + "</td></tr>\n");
			page.append("    <tr><td>Server Version</td><td>" + ServerDate + "</td></tr>\n");    
			page.append("</table>\n\n");   
			
			// currently running tasks 
			page.append("<h3 id=\"RunningHeader\">Running Tasks</h3>\n"); 			 
			page.append("<table id=\"RunningTable\">\n");  
			page.append("  <thead>\n");  
			page.append("    <th>Task Name</th>\n    <th>Log File</th>\n    <th>Start Time</th>\n    <th>Duration</th>\n    " 
					+ "<th>Runtime Parameters</th>\n    <th>Owner</th>\n    <th>End Process</th>\n");   
			page.append("  </thead>\n"); 
			 			
			synchronized (RServer.this) { 
				for (Task task : runningTasks.values()) { 

					long startTime = task.getStartTime();
					long duration = (System.currentTimeMillis() - startTime)/1000;
					
					page.append("  <tr>\n");
					page.append("    <td>" + (task.getShinyApp() ? "<a href=\"" + task.getShinyUrl() + "\">" : "")
							+ task.getTaskName() + (task.getShinyApp() ? "</a>" : "") + "</td>");
					
					if (task.getrScript().toLowerCase().contains(".r")) {   
						String link = "<a href=\"Rout.php?task=" + task.getTaskName() + "&log=" + 
								(task.getrScript().contains(".r") ? task.getrScript() + ".Rout" : 
									task.getrScript().split("\\.")[0] + ".Rout")
								+ "\">" + task.getrScript() + "</a>"; 
						page.append("\n    <td>" + link + "</td>");  				  
					}
					else {
						page.append("\n    <td> </td>");  				   						
					} 
					
					page.append("\n    <td>" + new Date(startTime).toString() + "</td>");  				   
					page.append("\n    <td>" + (task.getShinyApp() ? "" : (duration/60 + " m " + duration%60 + " s")) + "</td>");  				 
					page.append("\n    <td>" + task.getParameters() + "</td>");  				  
					page.append("\n    <td>" + task.getOwner() + "</td>");  				  
					
					if (GitTask.equalsIgnoreCase(task.getTaskName())) { 
						page.append("\n    <td> </td>");  				  
					}
					else {  
						page.append("\n    <td><form onsubmit=\"return confirm('Are you sure?')\""    
								+ " action=\"KillTask.php\" method=\"post\">"    
					 			+ "\n      <input type=\"hidden\" name=\"name\" value=\"" + task.getTaskName() + "\" />"  
					 			+ "\n      <button"
					 			+ " >Terminate</button>\n    </form></td>");     					 		    
					}					    
					
					page.append("\n  </tr>\n"); 				 
				}				
			}			 

			page.append("</table>\n\n"); 
 			 
			// Recent log history   
			page.append("<h3 id=\"LogHeader\">Recent Log Entries</h3>\n");  
			
			synchronized(RServer.this) { 
				page.append("<ul id=\"LogList\">\n"); 
				
				for (String log : logEntries) {
					if (log.contains("ERROR:")) {
						page.append("  <li class=\"FontLogError\">" + log + "</li>\n");    
					}
					else { 
						page.append("  <li>" + log + "</li>\n"); 
					}
				}
				
				page.append("</ul>\n\n"); 
			}
						
			// schedules 
			page.append("<h3 id=\"SchedulesHeader\">Scheduled Tasks</h3>\n");    
			page.append("<table id=\"ScheduleTable\">\n");  
			page.append("  <thead>\n");  
			page.append("    <th>Task Name</th>\n    <th>R Script</th>\n    <th>Runtime Parameters</th>\n    <th>Frequency</th>\n    " 
					+ "<th>Next Run Time</th>\n    <th>Owner</th>\n    <th>Run Now</th>\n");
			page.append("  </thead>\n");  
			
			
			page.append("<ul id=\"LogList\">\n"); 			
			if (schedulePath.split("//").length > 1) {
				page.append("  <li>Schedule File: //" + schedulePath.split("//")[1] + "</li>\n");  
			}
			else {
				page.append("  <li>Schedule File: " + schedulePath + "</li>\n");  
			} 			
			page.append("</ul>\n"); 

			for (Schedule schedule : regularSchedules) {				 

				page.append("  <tr>\n");
				page.append("    <td>" + schedule.getTaskName().replace("_", " ") + "</td>\n    " 
						+ "<td>" + schedule.getrScript().replace("_", " ") + "</td>\n    "   
						+ "<td>" + schedule.getParameters() + "</td>\n    "    
						+ "<td>" +  (schedule.getFrequency() == Schedule.Frequency.Now ? "Startup" : schedule.getFrequency()) + "</td>\n    <td>" 
						+ (schedule.getFrequency() == Schedule.Frequency.Now || schedule.getFrequency() == Schedule.Frequency.Never ?  " " : new Date(schedule.getNextRunTime()).toString()) 
						+ "</td>\n    <td>" +schedule.getOwner() + "</td>\n    "    
	         			+ "<td><form onsubmit=\"return confirm('Are you sure?')\"" 
	         			+ " action=\"SubmitTask.php\" method=\"post\">"   
	         			+ "\n      <input type=\"hidden\" name=\"name\" value=\"" + schedule.getTaskName() + "\" />" 
	         			+ "\n      <input type=\"hidden\" name=\"path\" value=\"" + schedule.getGitPath() + "\" />"
	         			+ "\n      <input type=\"hidden\" name=\"rscript\" value=\"" + schedule.getrScript() + "\" />"
	         			+ "\n      <input type=\"hidden\" name=\"email\" value=\"" + schedule.getOwner() + "\" />"  
	         			+ "\n      <input type=\"hidden\" name=\"sendemail\" value=\"" + schedule.getEmailOnSuccess() + "\" />"  
	         			+ "\n      <input type=\"hidden\" name=\"params\" value=\"" + schedule.getParameters() + "\" />" 
	         			+ "\n      <input type=\"hidden\" name=\"shiny\" value=\"" + schedule.getShinyApp() + "\" />"   
	         			+ "\n      <button"  
	         			+ ">Start</button>\n    </form></td>\n");     
				page.append("  </tr>\n");  
			}   		 
			
			page.append("</table>\n\n");    
			
			// completed tasks 
			page.append("<h3 id=\"CompletedHeader\">Completed Tasks</h3>\n");    
			page.append("<table id=\"CompletedTable\">\n");  
			page.append("  <thead>\n");   
			page.append("    <th>Task Name</th>\n    <th>Completion Time</th>\n    <th>Duration</th>\n    "
					+ "<th>Runtime Parameters</th>\n    <th>Outcome</th><th>.Rout log</th>\n    <th>Owner</th>\n"); 
			page.append("  </thead>\n");   

			synchronized (RServer.this) { 
				for (Task task : completed) { 
					long duration = (task.getEndTime() - task.getStartTime())/1000; 
					

					
					page.append("  <tr>\n");
					page.append("    <td>" + task.getTaskName().replace("_", " ") + "</td>\n    <td>");
					page.append("    " + new Date(task.getEndTime()).toString() + "</td>\n    <td>");
					page.append("    " + (duration/60 + " m " + duration%60 + " s ") + "</td>\n    <td>");   
					page.append("    " + task.getParameters() + "</td>\n    <td>"); 
					page.append("    " + (task.getOutcome().equals("Success") ? "<font class=\"FontTaskSuccess\">" :  "<font class=\"FontTaskFailure\">")); 
					page.append("    " + task.getOutcome() + "</font></td>\n    <td>"); 

					if (task.getRout() != null) {
						page.append("<a href=\"Rout.php?path=" + task.getRout() + "&log=" + task.getrScript() + ".Rout\">" + task.getrScript().replace("_", " ") + "</a></td>\n"); 
					}
					else {  
						page.append(" </td>\n"); 
					} 
					
					page.append("    " + "<td>" + task.getOwner() + "</td>\n"); 
					page.append("  </tr>\n"); 				 
				}				 
			}			 
 
			page.append("</table>\n\n");     
			page.append("</div>\n\n");	  		 

			page.append("<div id=\"FooterDiv\">\n");	  		 
			page.append("  <a href=\"https://github.com/bgweber/RServer\">RServer on GitHub</a>\n");	  		   
			page.append("</div>\n\n"); 

			// html footer   
			page.append("</div>\n</body>\n</html>\n"); 	  	  
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(indexPagePath));
			writer.write(page.toString());
			writer.close();
		}  
		catch (Exception e) {
			log("ERROR: failure updating server status: " + e.getMessage()); 
			e.printStackTrace();
		}
	}

	/**
	 * Logs system performance to a file. 
	 */
	private void updatePerformaceLog() {  
		try { 
			 Runtime runtime = Runtime.getRuntime();  
			 double appUsedMem = (runtime.totalMemory() - runtime.freeMemory())/1024/1024;
			 double appTotalMem = (runtime.totalMemory())/1024/1024;
			 
			 double totalMem = sigar.getMem().getTotal()/1024/1024/1024.0;
			 double usedMem = (sigar.getMem().getTotal() - sigar.getMem().getFree())/1024/1024/1024.0;
			 int threads = Thread.activeCount();
			 long currentTime = System.currentTimeMillis();			 
			 String logEntry = hostName + "," + bootTime + "," + currentTime + "," + cpuLoad + "," + threads + "," 
					 		+ appUsedMem + "," + appTotalMem + "," + usedMem + "," + totalMem + ","
					 		+ updateHtmlTime + "," + updatePerfLogTime;

			 File logFile = new File(performanceLogDir + "/" + hostName + ".csv");
			 ArrayList<String> logData = new ArrayList<>();
			 		 
			 if (logFile.exists()) {
				 BufferedReader reader = new BufferedReader(new FileReader(logFile));
				 String line = reader.readLine();			 
				 line = reader.readLine();			// skip the header 
				 
				 while (line != null) {
					 logData.add(line);					 
					 line = reader.readLine();
				 }
				 
				 reader.close();
			 }
			 
			 while (logData.size() >= performanceLogSize) {
				 logData.remove(0);
			 }

			 BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			 writer.write("ServerName, BootTime, UpdateTime, CpuLoad, Threads, AppMemUsage(MB), AppMemTotal(MB), UsedMem(GB), TotalMeb(GB), HtmlUpdateTime, LogUpdateTime\n"); 
			 
			 for (String log : logData) { 
				 writer.write(log + "\n");
			 }
			 
			 writer.write(logEntry + "\n");  
			 writer.close(); 
		}
		catch (Exception e) {
			log("ERROR: unable to update performace Log: " + e.getMessage()); 
		} 
	} 		
	
	/**  
	 * Utility for loading system properties.  
	 */ 
	public static String getProperty(String key, String value) {  
		return System.getProperty(key) != null ? System.getProperty(key) : value; 
	}	 
}
