package uk.ac.cam.ch.wwmm.oscarpattern.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**Generic IO routines not to do with files.
 * 
 * @author ptc24
 *
 */
public class IOTools {
	
	/**Gets a yes or no from STDIN.
	 * 
	 * @return Whether the answer was "yes".
	 * @throws Exception
	 */
	public static boolean askYN() throws Exception {
		BufferedReader stdinReader = new BufferedReader(new InputStreamReader(System.in));
		
		boolean done = false;
		String yn = null;
		while(!done) {
			yn = stdinReader.readLine().toLowerCase();
			if(yn != null && yn.length() >= 1 && (yn.startsWith("y") || yn.startsWith("n"))) {
				done = true;
			}
			if(!done) {
				System.out.println("yes (y) or no (n)?");
			}
		}
		
		return yn.startsWith("y");
	}

}
