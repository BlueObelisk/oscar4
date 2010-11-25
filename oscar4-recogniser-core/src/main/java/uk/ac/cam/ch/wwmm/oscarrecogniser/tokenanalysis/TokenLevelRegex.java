package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Element;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/** A regular expression used to classify individual Tokens.
 * 
 * @author ptc24
 *
 */

public class TokenLevelRegex {
	
	private String regex;
	private NamedEntityType type;
	private Pattern pattern;
	private String name;
	
	public TokenLevelRegex(Element elem, TLRHolder tlrHolder) {
		type = NamedEntityType.valueOf(elem.getAttributeValue("type"));
		String idRef = elem.getAttributeValue("idref");
		regex = tlrHolder.getDefText(idRef);
		name = elem.getAttributeValue("name");
        pattern = Pattern.compile(regex, Pattern.COMMENTS);
	}
	
	public boolean matches(String s) {
		Matcher m = pattern.matcher(s);
		return m.matches();
	}
	
	public NamedEntityType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
}
