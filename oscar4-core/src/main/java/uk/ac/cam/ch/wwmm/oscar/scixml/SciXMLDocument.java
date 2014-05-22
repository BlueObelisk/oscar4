package uk.ac.cam.ch.wwmm.oscar.scixml;

import java.util.Collection;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

/** A subclass of Document, contains methods for generating SciXML and extensions.
 * 
 * @author ptc24
 *
 */
public class SciXMLDocument extends Document {
	
	/**Makes an new SciXMLDocument from a document. The old document is
	 * effectively destroyed in the process (the root element is replaced
	 * with a dummy element).
	 * 
	 * @param doc The old Document. 
	 * @return The new SciXMLDocument.
	 */
	public static SciXMLDocument makeFromDoc(Document doc) {
		Element dummy = new Element("dummy");
		Element root = doc.getRootElement();
		doc.setRootElement(dummy);
		return new SciXMLDocument(root);
	}
	
	/**Makes a new SciXMLDocument from the given root element.
	 * 
	 * @param arg0 The root element.
	 */
	public SciXMLDocument(Element arg0) {
		super(arg0);
	}



	/**Sets the title of the document, creating a new TITLE element, or 
	 * replacing the contents of an existing element, as appropriate. 
	 * 
	 * @param n Either a text node, or an element whose contents are to be
	 * transplanted into the TITLE element.
	 * @return The title element.
	 */
	public Element setTitle(Node n) {
		if(query("/TITLE").size() == 0) {
			Element title = new Element("TITLE");
			title.appendChild(n);
			getRootElement().appendChild(title);
		return title;
		} else {
			Element title = (Element)query("/TITLE").get(0);
			title.removeChildren();
			title.appendChild(n);			
			return title;
		}
	}


		
	/**Gets the body element of the document, creating one if it doesn't
	 * already exist.
	 * 
	 * @return The body element.
	 */
	public Element getBody() {
		Nodes n = query("/DIV");
		if(n.size() == 0) {
			Element body = new Element("BODY");
			getRootElement().appendChild(body);
			return body;
		} else {
			return (Element)n.get(0);
		}
	}
	
	/** Gets the last DIV in the document,
	 * creating if necessary.
	 * 
	 * @return The DIV Element.
	 */
	public Element getDiv() {
		Nodes n = query("//DIV");
		if(n.size() == 0) {
			Element div = new Element("DIV");
			getBody().appendChild(div);
			return div;
		} else {
			return (Element)n.get(n.size()-1);
		}
	}


	/**Creates a new empty LIST element, configured for bullet points.
	 * 
	 * @return The new LIST element.
	 */
	public Element makeList() {
		Element list = new Element("LIST");
		list.addAttribute(new Attribute("TYPE", "bullet"));
		return list; 
	}
	
	/**Creates a new LIST element, configured for bullet points, containing
	 * the specified items.
	 * 
	 * @param items The strings to be turned into list items.
	 * @return The new LIST element.
	 */
	public Element makeList(Collection<String> items) {
		Element list = makeList();
		for(String item:items) {
			list.appendChild(makeListItem(item));
		}
		return list;
	}
	
	/** Make a new, empty, list item.
	 * 
	 * @return The list item;
	 */
	public Element makeListItem() {
		Element li = new Element("LI");
		return li;
	}
	
	/** Make a new list item, containing a string.
	 * 
	 * @param s The contents of the list item
	 * @return The list item.
	 */
	public Element makeListItem(String s) {
		Element li = makeListItem();
		li.appendChild(s);
		return li;
	}
		
	/**Make a new, empty, link element.
	 * 
	 * @param href The HREF to link to.
	 * @return The new link.
	 */
	public Element makeLink(String href) {
		Element a = new Element("a");
		a.addAttribute(new Attribute("href", href));
		return a;
	}
	
}
