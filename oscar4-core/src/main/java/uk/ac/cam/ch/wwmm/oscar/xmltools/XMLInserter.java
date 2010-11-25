package uk.ac.cam.ch.wwmm.oscar.xmltools;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Text;


/** Allows XML elements to be inserted into a document, keeping the Text content of
 * the document the same. This is essentially an XML-well-formedness safe version of
 * adding tags into a document. 
 * 
 * @author ptc24
 *
 */
public final class XMLInserter extends XMLSpanTagger {

	private String extraTagPrefix;
	private int extraTagNo;
	
	/**Generates an XMLInserter for an XML document.
	 * 
	 * @param rootElem The root element of the document to insert elements into.
	 * @param tagPrefix A prefix to put on the element IDs when setting up the
	 * inserter.
	 * @param extraTagPrefix A prefix to put on the element IDs when new
	 * elements are inserted.
	 */
	public XMLInserter(Element rootElem, String tagPrefix, String extraTagPrefix) {
		super(rootElem, tagPrefix);
		this.extraTagPrefix = extraTagPrefix;
		extraTagNo = 0;
	}

	/**Analyses a document, and tries to transfer elements with a given prefix
	 * into the inserter's document.
	 * 
	 * @param source The document to analyse.
	 * @param sourceDocPrefix The prefix of the elements to transfer.
	 */
	public void incorporateElementsFromRetaggedDocument(Element source, String sourceDocPrefix) {
		Elements elems = source.getChildElements();
		for (int i = 0; i < elems.size(); i++) {
			recursivelyIncorporateElements(elems.get(i), sourceDocPrefix);
		}		
	}
	
	private void recursivelyIncorporateElements(Element source, String sourceDocPrefix) {
		Attribute a = source.getAttribute("xtid");
		if(a != null && a.getValue().startsWith(sourceDocPrefix)) {
			try {
				insertElement(source);
			} catch (Exception e) {
				// TODO something useful here.
			}
		}
		Elements elems = source.getChildElements();
		for (int i = 0; i < elems.size(); i++) {
			recursivelyIncorporateElements(elems.get(i), sourceDocPrefix);
		}
	}
	
	/**Inserts an element into the inserter's XML document.
	 * 
	 * @param insertion The element to insert.
	 * @param start The start XPoint to insert into.
	 * @param end The end XPoint to insert into.
	 * @return The inserted element (a fresh copy), or null if the process fails.
	 * @throws Exception
	 */
	public Element insertElement(Element insertion, String start, String end) throws Exception {
		Element newElem = XOMTools.shallowCopy(insertion);
		newElem.addAttribute(new Attribute("xtid", extraTagPrefix + Integer.toString(extraTagNo)));
		extraTagNo++;
		newElem.addAttribute(new Attribute("xtspanstart", start));
		newElem.addAttribute(new Attribute("xtspanend", end));
		return insertElement(newElem);
	}
	
	/**Inserts an element into the inserter's XML document.
	 * 
	 * @param insertion The element to insert.
	 * @param start The start character offset to insert into.
	 * @param end The end character offset to insert into.
	 * @return The inserted element (a fresh copy), or null if the process fails.
	 * @throws Exception
	 */	
	public Element insertElement(Element insertion, int start, int end) throws Exception {
		return insertElement(insertion, Integer.toString(start), Integer.toString(end));
	}

	/**Inserts an element bearing poisitional information into the inserter's
	 * XML document. The element must have xtspanstart and xtspanend attributes
	 * containing the XPoints that the element is to be inserted into.
	 * 
	 * @param insertion The element to insert.
	 * @return The inserted element (a fresh copy), or null if the process fails.
	 * @throws Exception
	 */
	public Element insertTaggedElement(Element insertion) throws Exception {
		/* Assumes element is pre-tagged with xtspanstart and xtspanend */
		Element newElem = XOMTools.shallowCopy(insertion);
		newElem.addAttribute(new Attribute("xtid", extraTagPrefix + Integer.toString(extraTagNo)));
		extraTagNo++;
		return insertElement(newElem);
	}
	
	/**Inserts an element from a context into the baseline document.
	 * 
	 * @param insertion The element to insert.
	 * @return          The inserted element (a fresh copy), or null if a failure.
	 */
	private Element insertElement(Element insertion) throws Exception {
		// TODO this is huge. Refactor, please.
		// TODO something sensible with the return value
		/* You *have* tagged up the insertion element, haven't you? */
		if(insertion.getAttribute("xtid") == null) throw new Exception("Insert element must have been tagged up");
		/* First, find the parent element */
		Element currentParent = rootElem;
		Element newParent = currentParent;
		int start = Integer.parseInt(insertion.getAttributeValue("xtspanstart"));
		int end = Integer.parseInt(insertion.getAttributeValue("xtspanend"));
		boolean inParent = false;
		while(!inParent) {
			currentParent = newParent;
			Elements children = currentParent.getChildElements();
			for (int i = 0; i < children.size(); i++) {
				Element child = children.get(i);
				int elemStart = Integer.parseInt(child.getAttributeValue("xtspanstart"));
				int elemEnd = Integer.parseInt(child.getAttributeValue("xtspanend"));
				if(elemEnd < start) continue;
				if(elemStart <= start && elemEnd >= end) {
					newParent = child;
					break;
				}
			}
			if(newParent == currentParent) inParent = true;
		}
		/* OK, we now have our parent element */
		/* Now find the nodes enclosed by the new element */
		Node firstInnerNode = null;
		Node lastInnerNode = null;
		int parentStart = Integer.parseInt(currentParent.getAttributeValue("xtspanstart"));
		int parentEnd = Integer.parseInt(currentParent.getAttributeValue("xtspanend"));
		Elements children = currentParent.getChildElements();
		/* Parent node contains only text, maybe? */
		if(children.size() == 0) {
			int startOffset = start - parentStart;
			int endOffset = end - parentStart;
			if(start == parentStart && end == parentEnd) {
				firstInnerNode = currentParent.getChild(0);
				lastInnerNode = currentParent.getChild(0);
			} else if(start == parentStart) {
				firstInnerNode = currentParent.getChild(0);
				lastInnerNode = firstInnerNode;
				String txt = ((Text)firstInnerNode).getValue();
				((Text)firstInnerNode).setValue(txt.substring(0, endOffset));
				Text textNode = new Text(txt.substring(endOffset));
				currentParent.appendChild(textNode);
			} else if(end == parentEnd) {
				firstInnerNode = currentParent.getChild(0);
				lastInnerNode = firstInnerNode;
				String txt = ((Text)firstInnerNode).getValue();
				((Text)firstInnerNode).setValue(txt.substring(startOffset));
				Text textNode = new Text(txt.substring(0, startOffset));
				currentParent.insertChild(textNode, 0);				
			} else {
				firstInnerNode = currentParent.getChild(0);
				lastInnerNode = firstInnerNode;
				String txt = ((Text)firstInnerNode).getValue();
				((Text)firstInnerNode).setValue(txt.substring(startOffset, endOffset));
				Text textNode = new Text(txt.substring(0, startOffset));
				currentParent.insertChild(textNode, 0);				
				textNode = new Text(txt.substring(endOffset));
				currentParent.appendChild(textNode);				
			}
		} else {
			/* Parent node contains other Elements */
			int charPos = parentStart;
			/* Look through the slots in front of the element */
			for (int i = 0; i < children.size(); i++) {
				Element child = children.get(i);
				/* elemStart is also where the Text _ends_ */
				int elemStart = Integer.parseInt(child.getAttributeValue("xtspanstart"));
				/* We'll need this later on, for the charPos at the next point of the loop */
				int elemEnd = Integer.parseInt(child.getAttributeValue("xtspanend"));
				if(charPos <= start && start <= elemStart && charPos <= end && end <= elemStart) {
					/* The putative tag starts and ends here. */
					int startOffset = start - charPos;
					int endOffset = end - charPos;
					if(start == charPos && end == elemStart) {
						/* Tag swallows the Text whole */
						firstInnerNode = XOMTools.getPreviousSibling(child);
						lastInnerNode = XOMTools.getPreviousSibling(child);
					} else if(start == charPos) {
						/* Tag starts at the start of the Text */
						firstInnerNode = XOMTools.getPreviousSibling(child);
						lastInnerNode = firstInnerNode;
						String txt = ((Text)firstInnerNode).getValue();
						((Text)firstInnerNode).setValue(txt.substring(0, endOffset));
						Text textNode = new Text(txt.substring(endOffset));
						XOMTools.insertAfter(firstInnerNode, textNode);
					} else if(end == elemStart) {
						/* Tag starts at the start of the Text */						
						firstInnerNode = XOMTools.getPreviousSibling(child);
						lastInnerNode = firstInnerNode;
						String txt = ((Text)firstInnerNode).getValue();
						((Text)firstInnerNode).setValue(txt.substring(startOffset));
						Text textNode = new Text(txt.substring(0, startOffset));
						XOMTools.insertBefore(firstInnerNode, textNode);				
					} else {
						/* Tag is within the Text */
						firstInnerNode = XOMTools.getPreviousSibling(child);
						lastInnerNode = firstInnerNode;
						String txt = ((Text)firstInnerNode).getValue();
						((Text)firstInnerNode).setValue(txt.substring(startOffset, endOffset));
						Text textNode = new Text(txt.substring(0, startOffset));
						XOMTools.insertBefore(firstInnerNode, textNode);				
						textNode = new Text(txt.substring(endOffset));
						XOMTools.insertAfter(firstInnerNode, textNode);
					}
				} else if(charPos <= start && start <= elemStart) {
					/* The start's in there somewhere */
					if(charPos == elemStart) {
						/* A zero-length gap - convenient */
						firstInnerNode = child;
					} else if(start == elemStart) {
						/* the insertion starts at the same place as the inner element. */
						firstInnerNode = child;
					} else if(start == charPos) {
						/* the insertion starts at the same place as the previous element /
						 * the parent element. There's a Text node inbetween that needs
						 * eating.
						 */
						firstInnerNode = XOMTools.getPreviousSibling(child);
						if(!(firstInnerNode instanceof Text)) throw new Exception("Ptc24 is a fool!");
					} else {
						/* Text surgery required */
						Text textNode = (Text)(XOMTools.getPreviousSibling(child));
						int offset = start - charPos;
						String txt = textNode.getValue();
						textNode.setValue(txt.substring(0, offset));
						firstInnerNode = new Text(txt.substring(offset));
						XOMTools.insertAfter(textNode, firstInnerNode);
					}
				} else if(charPos <= end && end <= elemStart) {
					/* The end's in there somewhere */
					if(charPos == elemStart) {
						/* A zero-length gap - convenient */
						lastInnerNode = child;
					} else if(end == charPos) {
						/* the insertion ends at the end of the last inner (previous) element. */
						lastInnerNode = children.get(i-1);
					} else if(end == elemStart) {
						/* the insertion ends at the same place as the start of 
						 * this element. There's a Text node inbetween that needs
						 * eating.
						 */
						lastInnerNode = XOMTools.getPreviousSibling(child);
						if(!(lastInnerNode instanceof Text)) throw new Exception("Ptc24 is a fool!");
					} else {
						/* Text surgery required */
						lastInnerNode = (XOMTools.getPreviousSibling(child));
						int offset = end - charPos;
						String txt = lastInnerNode.getValue();
						((Text)lastInnerNode).setValue(txt.substring(0, offset));
						Text textNode = new Text(txt.substring(offset));
						XOMTools.insertAfter(lastInnerNode, textNode);
					}
				}
				if(firstInnerNode != null && lastInnerNode != null) break;
				charPos = elemEnd;
			}
			if(firstInnerNode == null) {
				int startOffset = start - charPos;
				int endOffset = end - charPos;
				if(start == charPos && end == parentEnd) {
					firstInnerNode = currentParent.getChild(currentParent.getChildCount()-1);
					lastInnerNode = currentParent.getChild(currentParent.getChildCount()-1);
				} else if(start == charPos) {
					firstInnerNode = currentParent.getChild(currentParent.getChildCount()-1);
					lastInnerNode = firstInnerNode;
					String txt = ((Text)firstInnerNode).getValue();
					((Text)firstInnerNode).setValue(txt.substring(0, endOffset));
					Text textNode = new Text(txt.substring(endOffset));
					currentParent.appendChild(textNode);
				} else if(end == parentEnd) {
					firstInnerNode = currentParent.getChild(currentParent.getChildCount()-1);
					lastInnerNode = firstInnerNode;
					String txt = ((Text)firstInnerNode).getValue();
					((Text)firstInnerNode).setValue(txt.substring(startOffset));
					Text textNode = new Text(txt.substring(0, startOffset));
					XOMTools.insertBefore(firstInnerNode, textNode);				
				} else {
					firstInnerNode = currentParent.getChild(currentParent.getChildCount()-1);
					lastInnerNode = firstInnerNode;
					String txt = ((Text)firstInnerNode).getValue();
					((Text)firstInnerNode).setValue(txt.substring(startOffset, endOffset));
					Text textNode = new Text(txt.substring(0, startOffset));
					XOMTools.insertBefore(firstInnerNode, textNode);				
					textNode = new Text(txt.substring(endOffset));
					currentParent.appendChild(textNode);				
				}				
			}
			if(lastInnerNode == null) {
				Element lastChild = children.get(children.size()-1);
				charPos = Integer.parseInt(lastChild.getAttributeValue("xtspanend"));
				if(charPos == parentEnd) {
					/* A zero-length gap - convenient */
					lastInnerNode = lastChild;
				} else if(end == charPos) {
					/* the insertion ends at the end of the last inner (previous) element. */
					lastInnerNode = lastChild;
				} else if(end == parentEnd) {
					/* the insertion ends at the same place as the start of 
					 * this element. There's a Text node inbetween that needs
					 * eating.
					 */
					lastInnerNode = currentParent.getChild(currentParent.getChildCount()-1);
					if(!(lastInnerNode instanceof Text)) throw new Exception("Ptc24 is a fool!");
				} else {
					/* Text surgery required */
					lastInnerNode = currentParent.getChild(currentParent.getChildCount()-1);
					int offset = end - charPos;
					String txt = lastInnerNode.getValue();
					((Text)lastInnerNode).setValue(txt.substring(0, offset));
					Text textNode = new Text(txt.substring(offset));
					XOMTools.insertAfter(lastInnerNode, textNode);
				}
			}
		}
		
		Element thisInsertion = XOMTools.shallowCopy(insertion);
		XOMTools.insertBefore(firstInnerNode, thisInsertion);
		int i = currentParent.indexOf(firstInnerNode);
		while(currentParent.getChild(i) != lastInnerNode) {
			Node n = currentParent.getChild(i);
			n.detach();
			thisInsertion.appendChild(n);
		}
		lastInnerNode.detach();
		thisInsertion.appendChild(lastInnerNode);
		
		return thisInsertion;
	}
	
	

	
	
}
