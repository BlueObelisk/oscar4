package uk.ac.cam.ch.wwmm.oscardata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Text;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;

/** A node in RParser's Regex heirarchy.
 *
 * @author  caw47, annexed by ptc24
 */
final class RPNode {
    
    private Pattern pattern = null;
    private String type = null;    
    private String id = null;
    private String value = null;
    private boolean unique = false;
    private List<RPNode> children;
    private int parseGroup;
    private boolean saf;
    private RParser rParser;
    
    /** Creates a new instance of RPNode */
    RPNode(RParser rParser) {
        type = null;
        id = null;
        value = null;
        unique = false;
        pattern = null;
        children = new ArrayList<RPNode>();
        parseGroup = 0;
        saf = false;
        this.rParser = rParser;
    }
    
    
    /** Creates a new instance of RPNode */
    public RPNode(RParser rParser, Element elem) {
    	setRegex(rParser.getNodeRegex(elem.getChildElements("regexp").get(0)));
        parseGroup = Integer.parseInt(elem.getChildElements("regexp").get(0).getAttributeValue("parsegroup"));
        type = elem.getAttributeValue("type");
        id = elem.getAttributeValue("id");
        value = elem.getAttributeValue("value");
        unique = elem.getChildElements("unique").size() > 0;
        saf = elem.getAttribute("saf") != null;
        children = new ArrayList<RPNode>();
        this.rParser = rParser;
	}


	/**
     * Getter for property pattern.
     * @return Value of property pattern.
     */
    Pattern getPattern() {
        return this.pattern;
    }
    
    
    /**
     * Setter for property regex.
     * @param regex New value of preoperty regex.
     */
    void setRegex(String regex) {
        pattern = new Pattern(regex,
                REFlags.IGNORE_CASE | REFlags.IGNORE_SPACES);
    }
    
    
    /**
     * Getter for property type.
     * @return Value of property type.
     */
    String getType() {
        return this.type;
    }
    
    
    /**
     * Getter for property id.
     * @return Value of property id.
     */
    String getId() {
        return this.id;
    }
    
    
    /**
     * Getter for property value.
     * @return Value of property value.
     */
    String getValue() {
        return this.value;
    }
    
    
    /**
     * Getter for property unique.
     * @return Value of property unique.
     */
    boolean isUnique() {
        return this.unique;
    }
    
    
    List<RPNode> getChildren() {
		return children;
	}


	boolean isSaf() {
		return saf;
	}


	RParser getrParser() {
		return rParser;
	}
    
    /**
     * Add a child element.
     * @param elem The XML element corresponding to the &lt;node&gt; to be added
     */
    void addChild(Element elem) {
        RPNode RPchild = new RPNode(rParser, elem);
        Elements childElems = elem.getChildElements("child");
        for (int i = 0; i < childElems.size(); i++) {
        	Element child = childElems.get(i);
        	String childType = child.getAttributeValue("type");
        	String childId = child.getAttributeValue("id");
        	Element referencedNode = rParser.findNode(childType, childId);
        	if(referencedNode==null) {
        		continue;
        	}
        	RPchild.addChild(referencedNode);
        }

        children.add(RPchild);
    }

    List<DataAnnotation> parseXOMText(Text textNode) {
    	return parseXOMText(textNode, new HashSet<RPNode>(), 0);
    }
    
    /**
     * Adds inline annotations to the element containing a nu.xom.Text node
     * according to the OSCAR-data regular expressions
     * 
     * @param textNode the nu.xom.Text to process
     * @param excludedChildren a set of RPNodes with the "unique" property that
     * already been found  
     * @param offset the text offset within the TokenSequence
     * @return 
     */
    List<DataAnnotation> parseXOMText(Text textNode, HashSet<RPNode> excludedChildren, int offset) {
    	List <DataAnnotation> annotations = new ArrayList<DataAnnotation>();
    	String nodeText = textNode.getValue();
        for(ListIterator <RPNode> i=children.listIterator(); i.hasNext();) {
            RPNode child = i.next();
            if(excludedChildren.contains(child)) {
            	continue;
            }
            	
            Pattern pat = child.getPattern();
            Matcher m = pat.matcher(nodeText);
            if(m.find()) {
            	DataAnnotation annotation = new DataAnnotation(m.start() + offset, m.end() + offset, m.group(0));
                String tokText;
                int pg = child.getParseGroup();
                try {
                    tokText = m.group(pg);
                } catch(ArrayIndexOutOfBoundsException e) {
                	pg = 0;
                    tokText = m.group(0);
                }
                
                textNode.setValue(nodeText.substring(0, m.start()));
                Node currentNode = textNode;
                if(m.start(pg) > m.start()) {
                	Text invincibleText = new Text(nodeText.substring(m.start(), m.start(pg)));
                	XOMTools.insertAfter(currentNode, invincibleText);
                	currentNode = invincibleText;                    		
                }
            	Element elem = new Element(child.getType());
            	XOMTools.insertAfter(currentNode, elem);
            	currentNode = elem;
            	if(child.getValue() != null && child.getValue().length() > 0) {
            		elem.addAttribute(new Attribute("type", child.getValue()));
            	}
            	if(child.saf) elem.addAttribute(new Attribute("saf", "yes"));
            	Text childText = new Text(tokText);
            	elem.appendChild(childText);
            	child.parseXOMText(childText);
                if(m.end() > m.end(pg)) {
                	Text invincibleText = 
                		new Text(nodeText.substring(m.end(pg), m.end()));
                	XOMTools.insertAfter(currentNode, invincibleText);
                	currentNode = invincibleText;                    		
                }
            	if(child.isUnique()) {
            		excludedChildren.add(child);
            	}
            	annotations.addAll(parseXOMText(textNode, excludedChildren, offset));//generate and add annotations from text previous to current annotation
            	annotations.add(annotation);//add current annotation
                if(m.end() < nodeText.length()) {
                	Text endText = new Text(nodeText.substring(m.end()));
                	XOMTools.insertAfter(currentNode, endText);
                	annotations.addAll(parseXOMText(endText, excludedChildren, m.end() + offset));//generate and add annotations from text subsequent to current annotation
                	// Do something about uniqueness here...
                }
                annotation.setInternalMarkup(elem);
                break;
            }
        }
        return annotations;
    }
        
    /**
     * Getter for property parseGroup.
     * @return Value of property parseGroup.
     */
    int getParseGroup() {
        return this.parseGroup;
    }
    
    /**
     * Setter for property parseGroup.
     * @param parseGroup New value of property parseGroup.
     */
    void setParseGroup(int parseGroup) {
        this.parseGroup = parseGroup;
    }
    
    
    List <DataAnnotation> annotateData(TokenSequence tokSeq) {
    	Text textNode = new Text(tokSeq.getSurface());
    	//a parent element is needed in parseXOMText()
    	new Element("dummy").appendChild(textNode);
    	return parseXOMText(textNode);
    }
}
