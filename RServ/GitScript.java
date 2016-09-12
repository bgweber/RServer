// Copyright (C) 2016 Electronic Arts Inc.  All rights reserved.
import java.io.File;

public class GitScript {

	public static void main(String[] args) throws Exception {
		update(false, "https://github.com/bgweber/RServer"); 
	}
	
	public static void update(boolean isLinux, String depot) throws Exception {
		
		if (isLinux) {
			Runtime.getRuntime().exec("sudo rm -r RServer").waitFor(); 	        	 
		}
		else {
			ProcessBuilder pb = new ProcessBuilder("DeleteGit.bat");
			pb.directory(new File(System.getProperty("user.dir")));
			pb.start().waitFor();  
		}
 
		Runtime.getRuntime().exec("git clone " + depot).waitFor(); 	        	
	}	
}
