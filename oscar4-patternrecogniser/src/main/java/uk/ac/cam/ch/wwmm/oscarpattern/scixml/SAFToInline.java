package uk.ac.cam.ch.wwmm.oscarpattern.scixml;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscarpattern.saf.SafTools;
import uk.ac.cam.ch.wwmm.oscarpattern.xmltools.StandoffTable;
import uk.ac.cam.ch.wwmm.oscarpattern.xmltools.XMLInserter;
import uk.ac.cam.ch.wwmm.oscarpattern.xmltools.XMLSpanTagger;
import uk.ac.cam.ch.wwmm.oscarpattern.xmltools.XOMTools;
/** Converts standoff annotation to inline annotation.
 * 
 * @author ptc24
 *
 */
public final class SAFToInline {

	/**Produces an inline annotated document from a SAF and a SciXML.
	 * 
	 * @param safDoc The SAF document.
	 * @param plainDoc The SciXML Document.
	 * @param brittle Whether to throw an exception if not all of the
	 * annotations can be inlined (true) or to carry on regardless (false).
	 * @return The inline annotated SciXML.
	 * @throws Exception
	 */
	public static Document safToInline(Document safDoc, Document plainDoc, boolean brittle) throws Exception {
		return safToInline(safDoc, plainDoc, plainDoc, brittle);
	}

	/**Produces an inline annotated document from a SAF and a SciXML.
	 * 
	 * @param safDoc The SAF document.
	 * @param plainDoc The SciXML Document that the SAF annotations stand off
	 * from.
	 * @param ontoDoc A SciXML document, possibly already bearing some form
	 * of inline annotation.
	 * @param brittle Whether to throw an exception if not all of the
	 * annotations can be inlined (true) or to carry on regardless (false).
	 * @return The inline annotated SciXML.
	 * @throws Exception
	 */
	public static Document safToInline(Document safDoc, Document plainDoc, Document ontoDoc, boolean brittle) throws Exception {
		Document resultsDoc = new Document((Element)XOMTools.safeCopy(ontoDoc.getRootElement()));
		plainDoc = new Document((Element)XOMTools.safeCopy(plainDoc.getRootElement()));
		
		StandoffTable st = new StandoffTable(plainDoc.getRootElement());
		XMLInserter xi = new XMLInserter(resultsDoc.getRootElement(), "a", "c");
		new XMLSpanTagger(plainDoc.getRootElement(), "b");
		
		Nodes annots = safDoc.query("//annot");
		for(int i=0;i<annots.size();i++) {
			//if(true) continue;
			Element annot = (Element)annots.get(i);
			
			String blocked = SafTools.getSlotValue(annot, "blocked");
			if("true".equals(blocked)) continue;
			
			Element ne = new Element("ne");
			
			String type = SafTools.getSlotValue(annot, "type");
			if(null == type) continue;
			if("dataSection".equals(type)) ne.setLocalName("datasection");
			
			if(annot.getAttribute("id") != null) ne.addAttribute(new Attribute("id", annot.getAttributeValue("id")));
			
			Elements slots = annot.getChildElements("slot");
			for(int j=0;j<slots.size();j++) {
				String slotName = slots.get(j).getAttributeValue("name");
				//if(slotName.equals("surface")) continue;
				ne.addAttribute(new Attribute(slotName, slots.get(j).getValue()));
			}
			int startOffset; int endOffset;
			try {
				startOffset = st.getOffsetAtXPoint(annot.getAttributeValue("from"));
				endOffset = st.getOffsetAtXPoint(annot.getAttributeValue("to"));
			} catch (Exception e) {
				if(brittle)	{
					e.printStackTrace();
					System.err.println(annot.getAttributeValue("from"));
					System.err.println(annot.getAttributeValue("to"));
					throw e;
					}
				else {
					System.err.println("Warning: could not inline a named entity: ");
					System.err.println("from: " + annot.getAttributeValue("from"));
					System.err.println("to: " + annot.getAttributeValue("to"));
					System.err.println(annot.query("slot[@name='surface']").get(0).getValue());
					continue;
				}
			}
			if(startOffset < 0 || endOffset < 0 || startOffset > endOffset) {
				if(brittle) {
				throw new Exception ("Bad offsets: " + annot.getAttributeValue("from") + " " + Integer.toString(startOffset) + " " +
						annot.getAttributeValue("to") + " " + Integer.toString(endOffset));
				} else {
					System.err.println("Warning: could not inline a named entity: ");
					System.err.println("from: " + annot.getAttributeValue("from"));
					System.err.println("to: " + annot.getAttributeValue("to"));
					System.err.println(annot.query("slot[@name='surface']").get(0).getValue());
					continue;					
				}
			}
			try {
				xi.insertElement(ne, startOffset, endOffset);
			} catch (Exception e) {
				if(brittle) {
				System.err.println(ne.toXML());
				System.err.println(annot.getAttributeValue("from"));
				System.err.println(annot.getAttributeValue("to"));
				System.err.println(startOffset);
				System.err.println(endOffset);
				throw e;
				} else {
					System.err.println("Warning: could not inline a named entity: ");
					System.err.println("from: " + annot.getAttributeValue("from"));
					System.err.println("to: " + annot.getAttributeValue("to"));
					Nodes n = annot.query("slot[@name='surface']");
					if(n.size() > 0) System.err.println(n.get(0).getValue());
					continue;					
				}
			}
		}
		xi.deTagDocument();
		
		Nodes n = safDoc.query("//cmlPile");
		if(n.size() > 0) {
			resultsDoc.getRootElement().appendChild(n.get(0).copy());
		}
		
		return resultsDoc;
	}
	
}
