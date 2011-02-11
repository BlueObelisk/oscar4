package uk.ac.cam.ch.wwmm.oscar.terms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**
 * A data class to hold several lists of words used in the name recogniser.
 *
 * @author ptc24
 * @author sea36
 *
 */
public final class TermSets {

	private static final Logger LOG = LoggerFactory.getLogger(TermSets.class);
    private static final ResourceGetter rg = new ResourceGetter(TermSets.class.getClassLoader(), "uk/ac/cam/ch/wwmm/oscar/terms/");

    private static TermSets defaultInstance;

    private final Set<String> stopWords;
    private final Set<String> usrDictWords;
    private final Set<String> closedClass;
    private final Set<String> chemAses;
    private final Set<String> nonChemAses;
    private final Set<String> noSplitPrefixes;
    private final Set<String> splitSuffixes;
    private final Set<String> elements;
    private final Set<String> ligands;
    private final Set<String> reactWords;
    private final Set<String> abbreviations;
    private final Pattern endingInElementNamePattern;

    public static TermSets getDefaultInstance() {
        if (defaultInstance == null) {
            return loadTermSets();
        }
        return defaultInstance;
    }

    private static synchronized TermSets loadTermSets() {
        if (defaultInstance == null) {
            defaultInstance = new TermSets();
        }
        return defaultInstance;
    }


    /**
     * Gets the term set from stopwords.txt.
     *
     * @return The term set.
     */
    public Set<String> getStopWords() {
        return stopWords;
    }

    /**
     * Gets the term set from usrDictWords.txt.
     *
     * @return The term set.
     */
    public Set<String> getUsrDictWords() {
        return usrDictWords;
    }

    /**
     * Gets the term set from closedClass.txt.
     *
     * @return The term set.
     */
    public Set<String> getClosedClass() {
        return closedClass;
    }

    /**
     * Gets the term set from noSplitPrefixes.txt.
     *
     * @return The term set.
     */
    public Set<String> getNoSplitPrefixes() {
        return noSplitPrefixes;
    }

    public Set<String> getSplitSuffixes() {
        return splitSuffixes;
    }

    /**
     * Gets the term set from chemAses.txt.
     *
     * @return The term set.
     */
    public Set<String> getChemAses() {
        return chemAses;
    }

    /**
     * Gets the term set from nonChemAses.txt.
     *
     * @return The term set.
     */
    public Set<String> getNonChemAses() {
        return nonChemAses;
    }

    /**
     * Gets the term set from elements.txt.
     *
     * @return The term set.
     */
    public Set<String> getElements() {
        return elements;
    }

    /**
     * Gets the term set from ligands.txt.
     *
     * @return The term set.
     */
    public Set<String> getLigands() {
        return ligands;
    }

    /**
     * Gets the term set from reactWords.txt.
     *
     * @return The term set.
     */
    public Set<String> getReactWords() {
        return reactWords;
    }
    
    /**
     * Gets the term set from abbreviations.txt
     * 
     * @return The term set
     */
    public Set<String> getAbbreviations() {
    	return abbreviations;
    }

    /**
     * Gets a regular expression that detects whether a word is ending in
     * an element name. For example "trizinc" or "dialuminium".
     *
     * @return The compiled regular expression.
     */
    public Pattern getEndingInElementNamePattern() {
        return endingInElementNamePattern;
    }


    private TermSets() {
        LOG.debug("Initialising term sets... ");

        try {
	        stopWords = loadTerms("stopwords.txt");
	        usrDictWords = loadTerms("usrDictWords.txt", false);
	        noSplitPrefixes = loadTerms("noSplitPrefixes.txt");
	        splitSuffixes = loadTerms("splitSuffixes.txt");
	        closedClass = loadTerms("closedClass.txt");
	        chemAses = loadTerms("chemAses.txt");
	        nonChemAses = loadTerms("nonChemAses.txt");
	        elements = loadTerms("elements.txt");
	        ligands = loadTerms("ligands.txt");
	        reactWords = loadTerms("reactWords.txt");
	        abbreviations = loadTerms("abbreviations.txt");
        }
        catch (IOException e) {
        	throw new OscarInitialisationException("failed to load TermSets", e);
        }

        endingInElementNamePattern = initEndingInElementNamePattern();

        LOG.debug("term sets initialised");
    }

    private Pattern initEndingInElementNamePattern() {
        // Pattern matches full (lowercase) names, but not element symbols
        Pattern namePattern = Pattern.compile("[a-z]+");
        StringBuffer sb = new StringBuffer();
        sb.append(".+(");
        for(Iterator<String> it = elements.iterator(); it.hasNext();) {
            String s = it.next();
            if (namePattern.matcher(s).matches()) {
                sb.append(s);
                if (it.hasNext()) {
                    sb.append('|');
                }
            }
        }
        sb.append(')');
        return Pattern.compile(sb.toString());
    }


    private static Set<String> loadTerms(String filename) throws IOException {
        return loadTerms(filename, true);
    }

    private static Set<String> loadTerms(String filename, boolean normalise) throws IOException {
        HashSet<String> dict = new HashSet<String>();
        InputStream is = rg.getStream(filename);
        try {
        	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        	while (reader.ready()) {
                String line = StringTools.normaliseName(reader.readLine());
                if (line.length() > 0 && line.charAt(0) != '#') {
                    dict.add(line);
                }
            }
        }
        finally {
    		IOUtils.closeQuietly(is);
        }
        
        return Collections.unmodifiableSet(dict);
    }

}
