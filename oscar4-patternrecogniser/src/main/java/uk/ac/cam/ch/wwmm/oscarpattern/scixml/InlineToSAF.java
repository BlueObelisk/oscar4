package uk.ac.cam.ch.wwmm.oscarpattern.scixml;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscar.document.SafTools;
import uk.ac.cam.ch.wwmm.oscar.tools.IStandoffTable;
import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XMLSpanTagger;

/** Converts inline annotation to standoff annotation.
 * 
 * @author ptc24
 *
 */
/*
 * Duplicated in oscarMEMM.models, should be moved with ExtractTrainingData
 */

public final class InlineToSAF {

	private Document safDoc;
	
	/**Generate a SAF document.
	 * 
	 * @param neDoc The inline annotated SciXML document.
	 * @param refDoc The unannotated source SciXML document.
	 * @param name The document name.
	 * @return The SAF document.
	 * @throws Exception
	 */
	public static Document extractSAFs(Document neDoc, Document refDoc, String name) throws Exception {
		InlineToSAF nets = new InlineToSAF(neDoc, refDoc, name);
		return nets.getSAF();
	}
	
	private InlineToSAF(Document neDoc, Document refDoc, String name) throws Exception {		
		IStandoffTable st = new StandoffTable(refDoc.getRootElement());
		new XMLSpanTagger(neDoc.getRootElement(), "n");

		Element saf = new Element("saf");
		safDoc = new Document(saf);
		saf.addAttribute(new Attribute("document", name));
		
		Nodes nes = neDoc.query("//ne");
		for(int i=0;i<nes.size();i++) {
			Element e = (Element)nes.get(i);
			String xps = st.getLeftPointAtOffset(Integer.parseInt(e.getAttributeValue("xtspanstart")));
			String xpe = st.getRightPointAtOffset(Integer.parseInt(e.getAttributeValue("xtspanend")));

			//Element safElem = new Element("annot");
			Element safElem = SafTools.makeAnnot(xps, xpe, "oscar");
			saf.appendChild(safElem);
			//safElem.addAttribute(new Attribute("from", xps));
			//safElem.addAttribute(new Attribute("to", xpe));
			//safElem.addAttribute(new Attribute("type", "oscar"));
			for(int j=0;j<e.getAttributeCount();j++) {
				Attribute a = e.getAttribute(j);
				// Eliminate XMLSpanTagger markup
				if(a.getLocalName().startsWith("xt")) continue;
				SafTools.setSlot(safElem, a.getLocalName(), a.getValue());
				//Element slot = new Element("slot");
				//slot.addAttribute(new Attribute("name", a.getLocalName()));
				//slot.appendChild(a.getValue());
				//safElem.appendChild(slot);
			}
			SafTools.setSlot(safElem, "surface", e.getValue());
			/*Element slot = new Element("slot");
			slot.appendChild(e.getValue());
			slot.addAttribute(new Attribute("name", "surface"));
			safElem.appendChild(slot);*/

			safElem.addAttribute(new Attribute("id", "o" + Integer.toString(i+1)));
		}
	}
	
	private Document getSAF() {		
		return safDoc;
	}
	
	
}
