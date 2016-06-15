import java.io.File;

public class GitScript {

	public static void main(String[] args) throws Exception {
		update(false); 
	}
	
	public static void update(boolean isLinux) throws Exception {
				
		System.out.println("Running Git!");
		
		if (isLinux) {
			Runtime.getRuntime().exec("sudo rm -r RServer").waitFor(); 	        	 
		}
		else {
			ProcessBuilder pb = new ProcessBuilder("DeleteGit.bat");
			pb.directory(new File(System.getProperty("user.dir")));
			pb.start().waitFor();  
		}

		Runtime.getRuntime().exec("git clone https://github.com/bgweber/RServer").waitFor(); 	        	
	}	
}
