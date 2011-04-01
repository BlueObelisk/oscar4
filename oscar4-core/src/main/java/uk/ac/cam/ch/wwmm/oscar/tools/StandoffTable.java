package uk.ac.cam.ch.wwmm.oscar.tools;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

/** Lists the element that contains the Text node at each character offset.
 * 
 * @author ptc24
 *
 */
public final class StandoffTable {

	private List<Text> textTable;
	private List<Integer> offsetTable;
		
	private Element rootElem;
	
	/**Generates a StandoffTable.
	 * 
	 * @param docElem The root element of the document to analyse.
	 */
	public StandoffTable(Element docElem) {
		rootElem = docElem;

		int l = docElem.getValue().length();
		textTable = new ArrayList<Text>(l);
		offsetTable = new ArrayList<Integer>(l);
		populateTable(docElem);
		assert(textTable.size() == l);
		assert(offsetTable.size() == l);
	}
	
	/**Gets the innermost Element at a particular character offset.
	 * 
	 * @param offset The character offset.
	 * @return The Element.
	 */
	public Element getElemAtOffset(int offset) {
		return (Element)textTable.get(offset).getParent();
	}
	

	/**Converts an offset into an XPoint suitable for the left (start) of an
	 * annotation.
	 * 
	 * @param offset The offset.
	 * @return The XPoint.
	 */
	public String getLeftPointAtOffset(int offset) {
		return getPathToNode(textTable.get(offset)) + "." + offsetTable.get(offset).toString();
	}
	
	/**Converts an offset into an XPoint suitable for the right (end) of an
	 * annotation.
	 * 
	 * @param offset The offset.
	 * @return The XPoint.
	 */
	public String getRightPointAtOffset(int offset) {
		return getPathToNode(textTable.get(offset-1)) + "." + Integer.toString(offsetTable.get(offset-1) + 1);
	}
	
	private String getPathToNode(Node n) {
		if(n == rootElem) return "/1";
		return getPathToNode(n.getParent()) + "/" + Integer.toString(n.getParent().indexOf(n)+1);
	}
		
	/**Converts an XPoint into a character offset. For this to work the
	 * XMLSpanTagger must have been run on the XML.
	 * 
	 * @param xPoint The XPoint.
	 * @return The offset.
	 */
	public int getOffsetAtXPoint(String xPoint) {
		String [] sa = xPoint.split("\\.");
		int textOffset = Integer.parseInt(sa[1]);
		String [] nodeNumbers = sa[0].substring(1).split("/");
		Node n = rootElem;
		for (int i = 1; i < nodeNumbers.length; i++) {
			n = n.getChild(Integer.parseInt(nodeNumbers[i]) - 1);
		}
		if(!(n instanceof Text)) {
			throw new IllegalArgumentException("Bad xpoint: " + xPoint);
		}
		int index = n.getParent().indexOf(n);
		if(index == 0) {
			return textOffset + Integer.parseInt(((Element)n.getParent()).getAttributeValue("xtspanstart"));
		} else {
			return textOffset + Integer.parseInt(((Element)XOMTools.getPreviousSibling(n)).getAttributeValue("xtspanend"));			
		}
	}
		
    private void populateTable(Element elem) {
		for (int i = 0; i < elem.getChildCount(); i++) {
			Node n = elem.getChild(i);
			if(n instanceof Text) {
				int l = n.getValue().length();
				for (int j = 0; j < l; j++) {
					textTable.add((Text) n);
					offsetTable.add(j);
				}
			} else if (n instanceof Element) {
				populateTable((Element)n);
			}
		}
	}	
}
