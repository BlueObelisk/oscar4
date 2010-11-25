package uk.ac.cam.ch.wwmm.oscar.xmltools;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Text;

/**
 * Marks up each Element in a document with the character offsets of where it starts 
 * and finishes.
 * 
 * @author ptc24
 */
public class XMLSpanTagger {

	protected Element rootElem;
	int tagNo;
	int charOffset;
	String tagPrefix;
	
	/**Puts xtspanstart, xtspanend and xtid tags on a document.
	 * 
	 * @param docElem The root element of the document.
	 * @param tagPrefix A prefix for the xtid values.
	 */
	public static void tagUpDocument(Element docElem, String tagPrefix) {
		new XMLSpanTagger(docElem, tagPrefix); // The constructor does all the hard work.
	}
	
	/**Puts xtspanstart, xtspanend and xtid tags on a document.
	 * 
	 * @param rootElem The root element of the document.
	 * @param tagPrefix A prefix for the xtid values.
	 */
	public XMLSpanTagger(Element rootElem, String tagPrefix) {
		this.rootElem = rootElem;
		this.tagPrefix = tagPrefix;
		tagNo = 0;
		tagUpDocument();
	}
	
	private void tagUpDocument() {
		charOffset = 0;
		tagUpElement(rootElem);
	}
	
	private void tagUpElement(Element e) {
		boolean flag = false;
		if(e.getAttribute("xtid") == null) {
			e.addAttribute(new Attribute("xtid", tagPrefix + Integer.toString(tagNo)));
			tagNo++;
			e.addAttribute(new Attribute("xtspanstart", Integer.toString(charOffset)));
			flag = true;
		}
		for (int i = 0; i < e.getChildCount(); i++) {
			if(e.getChild(i) instanceof Element) {
				tagUpElement((Element) e.getChild(i));
			} else if(e.getChild(i) instanceof Text) {
				charOffset += ((Text)e.getChild(i)).getValue().length();
			}
		}
		if(flag) {
			e.addAttribute(new Attribute("xtspanend", Integer.toString(charOffset)));			
		}
	}
	
	/**Removes the xtspanstart, xtspanend and xtid attributes from elements in
	 * the document.
	 * 
	 */
	public void deTagDocument() {
		deTagElement(rootElem);
	}
	
	/**Removes the xtspanstart, xtspanend and xtid attributes from this element
	 * and all descendant elements.
	 * 
	 * @param e The element to clean.
	 */
	public static void deTagElement(Element e) {
		Attribute a = e.getAttribute("xtid");
		if(a != null) {e.removeAttribute(a); a.detach();}
		a = e.getAttribute("xtspanstart");
		if(a != null) {e.removeAttribute(a); a.detach();}
		a = e.getAttribute("xtspanend");
		if(a != null) {e.removeAttribute(a); a.detach();}
		for (int i = 0; i < e.getChildElements().size(); i++) {
			deTagElement(e.getChildElements().get(i));
		}
	}
		
}
