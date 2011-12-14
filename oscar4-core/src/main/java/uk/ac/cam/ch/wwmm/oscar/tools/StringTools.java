package uk.ac.cam.ch.wwmm.oscar.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**Static routines for string manipulation.
 *
 * @author ptc24
 *
 */
public final class StringTools {

    private static final String SPACE = " ";
    //public static String base62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /** Lowercase Greek Unicode characters */
    public static final String lowerGreek = "\u03b1\u03b2\u03b3\u03b4\u03b5\u03b6\u03b7\u03b8\u03b9\u03ba\u03bb\u03bc\u03bd\u03be\u03bf\u03c0\u03c1\u03c2\u03c3\u03c4\u03c5\u03c6\u03c7\u03c8\u03c9";
    /** Quotation marks of various Unicode forms */
    public static final String quoteMarks = "\"'\u2018\u2019\u201A\u201B\u201C\u201D\u201E\u201F";
    /** Hyphens, dashes and the like */
    public static final String hyphens = "-\u2010\u2011\u2012\u2013\u2014\u2015";
    /** A regex fragment for any hyphen or other dash */
    public static final String hyphensRegex = "(?:-|\u2010|\u2011|\u2012|\u2013|\u2014|\u2015)";
    /** Apostrophes, backticks, primess etc */
    public static final String primes = "'`\u2032\u2033\u2034";
    /** The en dash */
    public static final String enDash = "\u2013";
    /** The em dash */
    public static final String emDash = "\u2014";
    /** The soft hyphen */
    public static final String SOFT_HYPHEN = "\u00ad";
    /** Three dots at mid level. Commmonly used for hydrogen bonds. */
    public static final String midElipsis = "\u22ef";
    /** Less than, greater than, equals, and other related characters. */
    public static final String relations = "=<>\u2260\u2261\u2262\u2263\u2264\u2265\u2266\u2267\u2268" +
            "\u2269\u226a\u226b";
    /** Tests for the presence of two adjacent lowercase letters. */
    public static final Pattern twoLowerPattern = Pattern.compile("[a-z][a-z]");
    /** Whitespace characters. */
    public static final String whiteSpace = "\u0020\u0085\u00a0\u1680\u180e\u2000\u2001\u2002\u2003" +
            "\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000";

    private static final Pattern P_OPTIONAL_GROUP = Pattern.compile("\\(([0-9 ]+)\\)\\?");

    /**Finds the first uppercase letter in a name that might sensibly be
     * converted to lowercase.
     */
    public static final Pattern firstLowerCaseable = Pattern.compile("(^|[^a-z])([A-Z][a-z][a-z])");

    public static final Pattern PSCRUB = Pattern.compile("^[\\(\\[]*(.*?)[\\.,;:!\\?\\)\\]]*$");

    private static final Pattern P_WHITESPACE = Pattern.compile("\\s+");

    private static final Pattern P_ALL_WS_DIGIT = Pattern.compile("[0-9 ]+");
    private static final Pattern P_ALL_WS_DIGIT_GROUPS = Pattern.compile("[0-9 ()?]+");

    /**Removes the letter "s" from the end of a string, if present.
     *
     * @param s The string.
     * @return The potentially modified string.
     */
    public static String withoutTerminalS(String s) {
        if (s.endsWith("s")) {
            return s.substring(0, s.length()-1);
        }
        return s;
    }

    /**Converts a list of objects into a string.
     *
     * @param l A list of characters.
     * @return The corresponding string.
     */
    public static String objectListToString(List<?> l, String separator) {
        StringBuffer text = new StringBuffer();
        for (Iterator<?> it = l.iterator(); it.hasNext();) {
            text.append(it.next().toString());
            if (it.hasNext() && separator != null) {
                text.append(separator);
            }
        }
        return text.toString();
    }


    /**Produce repetitions of a string. Eg. HelloWorld * 2 = HelloWorldHelloWorld.
     *
     * @param s The string to multiply.
     * @param n The number of times to multiply it.
     * @return The multiplied string.
     */
    public static String multiplyString(String s, int n) {
        StringBuffer text = new StringBuffer();
        for(int i = 0; i < n; i++) {
            text.append(s);
        }
        return text.toString();
    }

    /**Checks to see if placing an open bracket (normal, square or curly) on the
     * front of the string would cause the string to have balanced brackets.
     *
     * @param s The string to test.
     * @return The result of the test.
     */
    public static boolean isLackingOpenBracket(String s) {
        int bracketLevel = 0;
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == '[' || c == '{') {
                bracketLevel++;
            }
            else if (c == ')' || c == ']' || c == '}') {
                bracketLevel--;
            }
            if (bracketLevel == -1) {
                return true;
            }
        }
        return false;
    }

    /**Checks to see if placing an close bracket (normal, square or curly) on
     * the end of the string would cause the string to have balanced brackets.
     *
     * @param s The string to test.
     * @return The result of the test.
     */
    public static boolean isLackingCloseBracket(String s) {
        int bracketLevel = 0;
        for(int i = s.length()-1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c == '(' || c == '[' || c == '{') {
                bracketLevel--;
            }
            else if (c == ')' || c == ']' || c == '}') {
                bracketLevel++;
            }
            if (bracketLevel == -1) {
                return true;
            }
        }
        return false;
    }

    /**Checks to see whether the brackets in the string are balanced. Note that
     * this does not distinguish between normal, square and curly brackets.
     * Furthermore, "a)(b" does not count as balanced.
     *
     * @param s The string to test.
     * @return The result of the test.
     */
    public static boolean bracketsAreBalanced(String s) {
        int bracketLevel = 0;
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == '[' || c == '{') {
                bracketLevel++;
            }
            else if (c == ')' || c == ']' || c == '}') {
                bracketLevel--;
            }
            if (bracketLevel == -1) {
                return false;
            }
        }
        return bracketLevel == 0;
    }

    /**Checks to see if the string is surrounded by a pair of (normal, square
     * or curly) brackets, and that those brackets match each other.
     *
     * @param s The string to test.
     * @return The result of the test.
     */
    public static boolean isBracketed(String s) {
        if (s == null || s.length() < 3) {
            return false;
        }
        char first = s.charAt(0);
        char last = s.charAt(s.length()-1);
        if ((first == '(' && last == ')') || (first == '[' && last == ']') || (first == '{' && last == '}')) {
            return bracketsAreBalanced(s.substring(1, s.length()-1));
        }
        return false;
    }

    /**Joins a collection of strings into a single string.
     *
     * @param strings The strings to join together.
     * @param separator The separator to use.
     * @return The resulting string.
     */
    public static String collectionToString(Collection<String> strings, String separator) {
        StringBuilder text = new StringBuilder();
        for (Iterator<String> it = strings.iterator(); it.hasNext();) {
            text.append(it.next());
            if (it.hasNext()) {
                text.append(separator);
            }
        }
        return text.toString();
    }

    /**Joins an array of strings into a single string.
     *
     * @param strings The strings to join together.
     * @param separator The separator to use.
     * @return The resulting string.
     */
    public static String arrayToString(String[] strings, String separator) {
        return collectionToString(Arrays.asList(strings), separator);
    }


    /**URLEncodes a long string, adding newlines if necessary.
     *
     * @param s The string to URLEncode.
     * @return The URLEncoded string.
     */
    public static String urlEncodeLongString(String s) {
        StringBuffer sb = new StringBuffer();
        int chunks = s.length() / 50;
        for(int i = 0; i < chunks; i++) {
            sb.append(urlEncodeUTF8NoThrow(s.substring(i*50, (i+1)*50)));
            sb.append("\n");
        }
        sb.append(urlEncodeUTF8NoThrow(s.substring(chunks*50)));
        return sb.toString();
    }

    /**URLEncodes a string for UTF-8. This should not throw an exception as
     * UTF-8 is unlikely to be an unsupported encoding.
     *
     * @param s The string to encode.
     * @return The encoded string.
     */
    public static String urlEncodeUTF8NoThrow(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error("Wot no UTF-8 for URLEncode?");
        }
    }

    /**
     * Replace whitespace with a single space, remove soft hyphens, and convert
     * (whitespace-delimited) tokens to lowercase if two adjacent lowercase
     * characters are detected in the token in question.
     *
     * @param name
     *            The name to convert.
     * @return The normalised name.
     *
     */
    public static String normaliseName(String name) {
        String [] subStrings = splitOnWhitespace(name);
        for (int i = 0; i < subStrings.length; i++) {
            if (twoLowerPattern.matcher(subStrings[i]).find()) {
                subStrings[i] = subStrings[i].toLowerCase();
            }
            subStrings[i] = subStrings[i].replace(SOFT_HYPHEN, "");
        }
        if (subStrings.length == 0) {
            return "";
        }
        if (subStrings.length == 1) {
            return subStrings[0];
        }
        return arrayToString(subStrings, SPACE);
    }

    /**As normalise name, but with a better heuristic for deciding when to
     * convert to lowercase.
     *
     * @param name The name to convert.
     * @return The normalised name.
     */
    public static String normaliseName2(String name) {
        String[] subStrings = splitOnWhitespace(name);
        for (int i = 0; i < subStrings.length; i++) {
            subStrings[i] = subStrings[i].replace(SOFT_HYPHEN, "");
            Matcher m = firstLowerCaseable.matcher(subStrings[i]);
            if(m.find()) {
                subStrings[i] = subStrings[i].substring(0,m.start())
                        + subStrings[i].substring(m.start(),m.end()).toLowerCase()
                        + subStrings[i].substring(m.end());
            }
        }
        if (subStrings.length == 0) {
            return "";
        }
        if (subStrings.length == 1) {
            return subStrings[0];
        }
        return arrayToString(subStrings, SPACE);
    }

    /**Converts a unicode string into ISO-8859-1, converting greek letters
     * to their names, and difficult characters to underscore.
     *
     * @param s The string to convert.
     * @return The converted string.
     */
    public static String unicodeToLatin(String s) {
        boolean hasUnicode = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 128) {
                hasUnicode = true;
                break;
            }
        }
        if (!hasUnicode) {
            return s;
        }
        s = s.replace("\u03b1", "alpha");
        s = s.replace("\u03b2", "beta");
        s = s.replace("\u03b3", "gamma");
        s = s.replace("\u03b4", "delta");
        s = s.replace("\u03b5", "epsilon");
        s = s.replace("\u03b6", "zeta");
        s = s.replace("\u03b7", "eta");
        s = s.replace("\u03b8", "theta");
        s = s.replace("\u03b9", "iota");
        s = s.replace("\u03ba", "kappa");
        s = s.replace("\u03bb", "lambda");
        s = s.replace("\u03bc", "mu");
        s = s.replace("\u03bd", "nu");
        s = s.replace("\u03be", "xi");
        s = s.replace("\u03bf", "omicron");
        s = s.replace("\u03c0", "pi");
        s = s.replace("\u03c1", "rho");
        s = s.replace("\u03c2", "stigma");
        s = s.replace("\u03c3", "sigma");
        s = s.replace("\u03c4", "tau");
        s = s.replace("\u03c5", "upsilon");
        s = s.replace("\u03c6", "phi");
        s = s.replace("\u03c7", "chi");
        s = s.replace("\u03c8", "psi");
        s = s.replace("\u03c9", "omega");

        Charset charset = Charset.forName("ISO-8859-1");
        CharsetDecoder decoder = charset.newDecoder();
        CharsetEncoder encoder = charset.newEncoder();
        try {
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(s));
            CharBuffer cbuf = decoder.decode(bbuf);
            return cbuf.toString();
        } catch (CharacterCodingException e) {
            s = s.replaceAll("[^A-Za-z0-9_+-]", "_");
            try {
                ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(s));
                CharBuffer cbuf = decoder.decode(bbuf);
                return cbuf.toString();
            } catch (CharacterCodingException ee) {
                return null;
            }
        }
    }

    /**Tests to see whether one string could be an acronym of the other.
     *
     * @param potentialAcroymn The potential acronym.
     * @param reference The potential acronym expansion.
     * @return Whether there is a match.
     */
    public static boolean testForAcronym(String potentialAcroymn, String reference) {
        potentialAcroymn = potentialAcroymn.toLowerCase();
        reference = reference.toLowerCase();
        int refpoint = 0;
        for (int i = 0; i < potentialAcroymn.length(); i++) {
            refpoint = reference.indexOf(potentialAcroymn.charAt(i), refpoint);
            if (refpoint == -1) {
                return false;
            }
            refpoint++;
        }
        return true;
    }

    /**Sorts a list of strings, in reverse order of the values that they are
     * mapped to.
     *
     * @param list The list to sort.
     * @param map The mapping.
     */
    public static void sortStringList(List<String> list, final Map<String,? extends Comparable> map) {
        Collections.sort(list, Collections.reverseOrder(new Comparator<String>() {
            @SuppressWarnings("unchecked")
            public int compare(String o1, String o2) {
                return map.get(o1).compareTo(map.get(o2));
            }
        }));
    }

    /**Extracts all of the keys from the map, and returns them sorted in
     * reverse order of the values.
     *
     * @param map The map.
     * @return The sorted list.
     */
    public static List<String> getSortedKeyList(Map<String,? extends Comparable> map) {
        List<String> list = new ArrayList<String>(map.keySet());
        sortStringList(list, map);
        return list;
    }


    /**Takes a space-separated list, and produces all of the possible strings
     * that are subsets (including the whole set and the empty set) of that
     * set.
     *
     * @param ssList The space separated list.
     * @return The possibilities.
     */
    public static List<String> spaceSepListToSubLists(String ssList) {
        List<String> possibilities = new ArrayList<String>();
        possibilities.add("");
        String[] substrings = splitOnWhitespace(ssList);
        for (int i = 0; i < substrings.length; i++) {
            for (String s : new ArrayList<String>(possibilities)) {
                if (s.length() == 0) {
                    possibilities.add(substrings[i]);
                } else {
                    possibilities.add(s + SPACE + substrings[i]);
                }
            }
        }
        return possibilities;
    }

    /** Expands a string consisting of digits, whitespace and regex characters
     * into a finite set of digits/whitespace only strings if possible.
     *
     * A?BC? >>
     *          B
     *          AB
     *          BC
     *          ABC
     *
     * @param regex The regex to expand.
     * @return The strings that the regex can match.
     */
    public static Set<String> expandRegex(String regex) {
        if (regex == null || regex.length() == 0) {
            return Collections.emptySet();
        }
        if (!P_ALL_WS_DIGIT.matcher(regex).matches()) {
            if (P_ALL_WS_DIGIT_GROUPS.matcher(regex).matches()) {
                Matcher m = P_OPTIONAL_GROUP.matcher(regex);
                if (m.find()) {
                    String before = regex.substring(0, m.start());
                    String middle = regex.substring(m.start(1), m.end(1));
                    String after = regex.substring(m.end());
                    Set<String> results = new LinkedHashSet<String>();
                    results.addAll(expandRegex(before + after));
                    results.addAll(expandRegex(before + middle + after));
                    return results;
                }
            }
        }
        return Collections.singleton(regex);
    }

    /**
     * Removes all instances of a specified char from a given string, returning
     * the original string if no instances exist
     *
     * @author dmj30
     */
    public static String removeCharFromString(char c, String string) {
        char[] oldChars = string.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < oldChars.length; i++) {
            char current = oldChars[i];
            if (current != c) {
                builder.append(current);
            }
        }
        if (builder.length() == string.length()) {
            //avoids overhead of allocating new string
            return string;
        }
        return builder.toString();
    }

    public static boolean isHyphen(String s) {
        return hyphens.contains(s);
    }

    public static boolean isMidElipsis(String s) {
        return midElipsis.contains(s);
    }

    public static boolean isQuoteMark(String s) {
        return quoteMarks.contains(s);
    }

    public static String[] splitOnWhitespace(String s) {
        return P_WHITESPACE.split(s);
    }

    public static boolean isLowerCaseWord(String s) {
        if (s.length() == 0) {
            return false;
        }
        for (int i = s.length()-1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < 'a' || c > 'z') {
                return false;
            }
        }
        return true;
    }
    
	/**
	 * Tests if this string ends with the specified suffix ignoring case.
	 * @param str
	 * @param suffix
	 * @return
	 */
	public static boolean endsWithCaseInsensitive(String str, String suffix) {
		if (suffix.length() > str.length()) {
			return false;
		}
		int strOffset = str.length() - suffix.length();
		return str.regionMatches(true, strOffset, suffix, 0, suffix.length());
	}
}
