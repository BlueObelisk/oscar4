package uk.ac.cam.ch.wwmm.oscar.scixml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.scixml.SciXMLDocument;
import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;

import nu.xom.Document;
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
		Element paper = new Element(XMLStrings.getInstance().PAPER);
		SciXMLDocument doc = new SciXMLDocument(paper);
		Element body = new Element(XMLStrings.getInstance().BODY);
		paper.appendChild(body);
		Element div = new Element(XMLStrings.getInstance().DIV);
		body.appendChild(div);
		Element header = new Element(XMLStrings.getInstance().HEADER);
		div.appendChild(header);
		
		String[] paragraphs = s.split("\n\\s*");
		for(int i=0;i<paragraphs.length;i++){
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
	
	/**Builds a new SciXML document from the given text string, using special
	 * rules to interpret the format of abstracts in the BioIE corpus.
	 * 
	 * @param s The string.
	 * @return The resulting SciXML document.
	 */
	public static Document bioIEAbstractToXMLDoc(String s) {
		Element paper = new Element(XMLStrings.getInstance().PAPER);
		Document doc = new Document(paper);
		
		Pattern p = Pattern.compile("\\W\n\\W\n\\W*");
		Matcher m = p.matcher(s);
		int pos = 0;
		int num = 0;
		while(m.find()) {
			if(pos < m.start()) {
				num++;
				if(num == 2) {
					Element title = new Element(XMLStrings.getInstance().TITLE);
					title.appendChild(s.substring(pos, m.start()));
					paper.appendChild(title);
				} else if((num == 4 || num == 5) && m.start() - pos > 200) {
					Element abstrct = new Element(XMLStrings.getInstance().ABSTRACT);
					abstrct.appendChild(s.substring(pos, m.start()));
					paper.appendChild(abstrct);
				} else {
					paper.appendChild(s.substring(pos, m.start()));
				}
			}
			paper.appendChild(m.group());
			pos = m.end();			
		}
		if(pos < s.length()) {
			paper.appendChild(s.substring(pos));
		}

		return doc;
	}
	
}
