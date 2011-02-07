package uk.ac.cam.ch.wwmm.oscar.ont;

import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author ptc24
 * @author Sam Adams
 * @author dmj30
 */
public class TermsFileReader {

    private static final Pattern definePattern = Pattern.compile("(.*?) = (.*)");
    private static final Pattern lexnamePattern = Pattern.compile("\\[\\S*\\]");



    /**
     * Reads a terms file into a Map
     * @param in
     * @param concatenateTypes
     * @return a Map of terms to space-separated ids
     * @throws IOException
     * @throws DataFormatException 
     */
    //TODO remove boolean argument
    public static Map<String, String> loadTermMap(BufferedReader in, boolean concatenateTypes) throws IOException, DataFormatException {
        HashMap<String, String> defines = new HashMap<String, String>();
		HashMap<String, String> lexicons = new HashMap<String, String>();
		String line = in.readLine();
		String lexname = null;
		while(line != null) {
            if (line.endsWith(">>>")) {
                line = readMultiLine(in, line);
			}
			if(line.length() == 0) {
				// Blank line
			} else if(line.charAt(0) == '#') {
				// Comment
			} else if(lexnamePattern.matcher(line).matches()) {
				lexname = line.substring(1, line.length()-1);
			} else {
				if (lexname == null) {
					throw new DataFormatException("malformed terms file: error parsing line \"" + line + "\"");
				}
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

	public static ListMultimap<String, String> loadConcatenatedTermMap(BufferedReader in) throws DataFormatException, IOException {
		HashMap<String, EquivalentTermSet> equivalentTermSets = new HashMap<String, EquivalentTermSet>(); 
		String line = in.readLine();
		String lexname = null;
		while(line != null) {
            if (line.endsWith(">>>")) {
            	throw new DataFormatException("method does not support multiline mode");
			}
			if(line.length() == 0) {
				// Blank line
			} else if(line.charAt(0) == '#') {
				// Comment
			} else if(lexnamePattern.matcher(line).matches()) {
				lexname = line.substring(1, line.length()-1);
				if("DEFINE".equals(lexname)) {
					throw new DataFormatException("method does not support DEFINE");
				}
			} else {
				if (lexname == null) {
					throw new DataFormatException("malformed terms file: error parsing line \"" + line + "\"");
				}
				String normalisedLine = StringTools.normaliseName(line);
				EquivalentTermSet equivalentSet = equivalentTermSets.get(normalisedLine);
				if (equivalentSet != null) {
					equivalentSet.addNameIfNovel(line);
					equivalentSet.addIdIfNovel(lexname);
				}
				else {
					equivalentSet = new EquivalentTermSet(line, lexname);
					equivalentSet.addNameIfNovel(normalisedLine);
					equivalentTermSets.put(normalisedLine, equivalentSet);
				}
			}
			line = in.readLine();
		}
		
		ArrayListMultimap<String, String> termMap = ArrayListMultimap.create();
		for (EquivalentTermSet termSet : equivalentTermSets.values()) {
			termMap.putAll(termSet.toTermMap());
		}
		return termMap;
	}

	

}
