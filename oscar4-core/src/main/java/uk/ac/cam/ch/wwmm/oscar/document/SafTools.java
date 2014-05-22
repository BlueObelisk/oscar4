package uk.ac.cam.ch.wwmm.oscar.document;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

/** Generic routines for handling standoff annotations in SAF format.
 * 
 * @author ptc24
 *
 */
public final class SafTools {
	
	/**Sets the value of a slot in an annotation.
	 * 
	 * @param annot The annot element.
	 * @param slotName The slot name.
	 * @param slotVal The slot value.
	 */
	public static void setSlot(Element annot, String slotName, String slotVal) {
		//        Nodes n = annot.query("slot[@name=\"" + slotName + "\"]");
        //		for (int i = 0; i < n.size(); i++) n.get(i).detach();
        removeSlot(annot, slotName);

		Element slot = new Element("slot");
		slot.addAttribute(new Attribute("name", slotName));
		slot.appendChild(slotVal);
		annot.appendChild(slot);
	}
	
	/**Gets the value of a slot, or null.
	 * 
	 * @param annot The annot element that (potentially) contains the slot.
	 * @param slotName The slot name.
	 * @return The slot value, or null.
	 */
	public static String getSlotValue(Element annot, String slotName) {
		Elements els = annot.getChildElements("slot");
        for (int i = 0; i < els.size(); i++) {
            Element e = els.get(i);
            String name = e.getAttributeValue("name");
            if (slotName.equals(name)) {
                return e.getValue();
            }
        }
        return null;
	}
	
	/**Deletes a slot (if it exists) from an annot.
	 * 
	 * @param annot The annot element that (potentially) contains the slot.
	 * @param slotName The slot name.
	 */
	public static void removeSlot(Element annot, String slotName) {
        Elements els = annot.getChildElements("slot");
        for (int i = els.size()-1; i >= 0; i--) {
            Element e = els.get(i);
            if (slotName.equals(e.getAttributeValue("name"))) {
                e.detach();
            }
        }
	}

	/**Makes an empty annot element.
	 * 
	 * @param start The start XPoint.
	 * @param end The end XPoint.
	 * @param annotType The annot type.
	 * @return The annot element.
	 */
	public static Element makeAnnot(String start, String end, String annotType) {
		Element safElem = new Element("annot");
		safElem.addAttribute(new Attribute("from", start));
		safElem.addAttribute(new Attribute("to", end));
		safElem.addAttribute(new Attribute("type", annotType));
		return safElem;
	}
    
}
