package uk.ac.cam.ch.wwmm.oscarMEMM.xmltools;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;


/** Places each character in XML in an element of its own, with ID.
 * 
 * @author ptc24
 *
 */
public final class CharacterMarkup {

	/**Places each character in XML in an element of its own, with ID.
	 * 
	 * @param e The element to modify.
	 */
	public static void markupCharacters(Element e) {
		markupCharacters(e, false);
	}
	
	/**Places each character in XML in an element of its own, with ID.
	 * 
	 * @param e The element to modify.
	 * @param ieFix Whether to convert space to \r, to make things work in IE.
	 */
	public static void markupCharacters(Element e, boolean ieFix) {
		Nodes n = e.query("//text()");
		for(int i=0;i<n.size();i++) {
			String s = n.get(i).getValue();
			Element parent = (Element)n.get(i).getParent();
			int index = parent.indexOf(n.get(i));
			for(int j=0;j<s.length();j++) {
				Element c = new Element("char");
				String cStr = s.substring(j,j+1);
				if(ieFix && cStr.equals(" ")) cStr="\r";
				c.appendChild(cStr);
				parent.insertChild(c, index+j);
			}
			n.get(i).detach();
		}
		Nodes cn = e.query("//char");
		for(int i=0;i<cn.size();i++) ((Element)cn.get(i)).addAttribute(new Attribute("index", Integer.toString(i)));
	}
	
}
