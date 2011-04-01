package uk.ac.cam.ch.wwmm.oscar.tools;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscar.document.SafTools;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XMLSpanTagger;

/** Converts inline annotation to standoff annotation.
 * 
 * @author ptc24
 *
 */
public final class InlineToSAF {

	private Document safDoc;
	
	/**Generate a SAF document.
	 * 
	 * @param neDoc The inline annotated SciXML document.
	 * @param refDoc The unannotated source SciXML document.
	 * @param name The document name.
	 * @return The SAF document.
	 */
	public static Document extractSAFs(Document neDoc, Document refDoc, String name) {
		InlineToSAF nets = new InlineToSAF(neDoc, refDoc, name);
		return nets.getSAF();
	}
	
	private InlineToSAF(Document neDoc, Document refDoc, String name) {		
		StandoffTable st = new StandoffTable(refDoc.getRootElement());
		new XMLSpanTagger(neDoc.getRootElement(), "n");

		Element saf = new Element("saf");
		safDoc = new Document(saf);
		saf.addAttribute(new Attribute("document", name));
		
		Nodes nes = neDoc.query("//ne");
		for (int i = 0; i < nes.size(); i++) {
			Element e = (Element)nes.get(i);
			String xps = st.getLeftPointAtOffset(Integer.parseInt(e.getAttributeValue("xtspanstart")));
			String xpe = st.getRightPointAtOffset(Integer.parseInt(e.getAttributeValue("xtspanend")));

			Element safElem = SafTools.makeAnnot(xps, xpe, "oscar");
			saf.appendChild(safElem);
			for (int j = 0; j < e.getAttributeCount(); j++) {
				Attribute a = e.getAttribute(j);
				// Eliminate XMLSpanTagger markup
				if(a.getLocalName().startsWith("xt")) continue;
				SafTools.setSlot(safElem, a.getLocalName(), a.getValue());
			}
			SafTools.setSlot(safElem, "surface", e.getValue());
			
			safElem.addAttribute(new Attribute("id", "o" + Integer.toString(i+1)));
		}
	}
	
	private Document getSAF() {		
		return safDoc;
	}
	
	
}
