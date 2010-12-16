package uk.ac.cam.ch.wwmm.oscar.obo;

import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sam Adams
 */
public class TermsFileReader {

    private static final Pattern definePattern = Pattern.compile("(.*?) = (.*)");




    /**
     * Reads a terms file into a Map
     * @param in
     * @param concatenateTypes
     * @return
     * @throws IOException
     */
    public static Map<String, String> loadTermMap(BufferedReader in, boolean concatenateTypes) throws IOException {
        HashMap<String, String> defines = new HashMap<String, String>();
		HashMap<String, String> lexicons = new HashMap<String, String>();
		String line = in.readLine();
		String lexname = "";
		while(line != null) {
            if (line.endsWith(">>>")) {
                line = readMultiLine(in, line);
			}
			if(line.length() == 0) {
				// Blank line
			} else if(line.charAt(0) == '#') {
				// Comment
			} else if(line.matches("\\[\\S*\\]")) {
				lexname = line.substring(1, line.length()-1);
			} else {
				for(String d : defines.keySet()) {
					line = line.replace(d, defines.get(d));
				}
				if("DEFINE".equals(lexname)) {
					Matcher m = definePattern.matcher(line);
					if(m.matches()) {
						defines.put(m.group(1), m.group(2));
					}
				} else if(concatenateTypes && lexicons.get(line) != null) {
					String newLexName = StringTools.normaliseName(lexname);
					lexicons.put(line, newLexName += " " + lexicons.get(line));
				} else {
					lexicons.put(line, StringTools.normaliseName(lexname));
					lexicons.put(StringTools.normaliseName(line), StringTools.normaliseName(lexname));
				}
				//if(line.matches(".*[a-z][a-z].*")) lexicons.put(line.toLowerCase(), lexname);
			}
			line = in.readLine();
		}
		return lexicons;
	}

    private static String readMultiLine(BufferedReader in, String line) throws IOException {
        StringBuilder s = new StringBuilder(line);
        do {
            s.setLength(s.length()-3);
            line = in.readLine();
            if (line == null) {
                throw new EOFException();
            }
            s.append(line);
        } while (line.endsWith(">>>"));
        line = s.toString();
        return line;
    }

}
