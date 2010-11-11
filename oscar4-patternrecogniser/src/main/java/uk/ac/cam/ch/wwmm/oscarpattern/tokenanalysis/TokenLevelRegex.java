package uk.ac.cam.ch.wwmm.oscarpattern.tokenanalysis;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Element;

/** A regular expression used to classify individual Tokens.
 * 
 * @author ptc24
 *
 */

public class TokenLevelRegex {
	
	private String regex;
	private String type;
	private Pattern pattern;
	private String name;
	
	public TokenLevelRegex(Element elem, TLRHolder tlrHolder) {
		type = elem.getAttributeValue("type");
		String idRef = elem.getAttributeValue("idref");
		regex = tlrHolder.getDefText(idRef);
		name = elem.getAttributeValue("name");
        pattern = Pattern.compile(regex, Pattern.COMMENTS);
	}
	
	public boolean matches(String s) {
		Matcher m = pattern.matcher(s);
		return m.matches();
	}
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
}
