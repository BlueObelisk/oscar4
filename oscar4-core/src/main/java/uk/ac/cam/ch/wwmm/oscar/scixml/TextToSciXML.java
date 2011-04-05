package uk.ac.cam.ch.wwmm.oscar.scixml;

import java.util.regex.Pattern;

import nu.xom.Element;

/**
 * Converts plain text into (minimal) SciXML. Rudimentary.
 * 
 * @author ptc24
 */
public final class TextToSciXML {
	
	private static Pattern headerRe = Pattern.compile("(abstract|introduction|results|" + 
			"(results\\s+and\\s+)?discussion|conclusions?|" +
			"experimental(\\s+(section|procedures?))?|" +
			"supporting\\s+information(\\s+available)?|" +
			"acknowledge?ments?|references):?\\s*", Pattern.CASE_INSENSITIVE);
	
	/**Builds a new SciXML document from the given text string.
	 * 
	 * @param s The string.
	 * @return The resulting SciXML document.
	 */
	public static SciXMLDocument textToSciXML(String s) {
		return textToSciXML(s, XMLStrings.getDefaultInstance());
	}
			
	
	/**Builds a new SciXML document from the given text string.
	 * 
	 * @param s The string.
	 * @param the {@link XMLStrings} for the output document's schema
	 * @return The resulting SciXML document.
	 */
	public static SciXMLDocument textToSciXML(String s, XMLStrings xmlStrings) {
		Element paper = new Element(xmlStrings.PAPER);
		SciXMLDocument doc = new SciXMLDocument(paper);
		Element body = new Element(xmlStrings.BODY);
		paper.appendChild(body);
		Element div = new Element(xmlStrings.DIV);
		body.appendChild(div);
		Element header = new Element(xmlStrings.HEADER);
		div.appendChild(header);
		
		String[] paragraphs = s.split("\n\\s*");
		for (int i = 0; i < paragraphs.length; i++){
			String para = paragraphs[i];
			if(headerRe.matcher(para.toLowerCase()).matches()) {
				div = new Element("DIV");
				body.appendChild(div);
				header = new Element("HEADER");
				header.appendChild(para);
				div.appendChild(header);
			} else {
				Element p = new Element("P");
				p.appendChild(paragraphs[i]);
				div.appendChild(p);
			}
		}
		return doc;
	}
	
}
