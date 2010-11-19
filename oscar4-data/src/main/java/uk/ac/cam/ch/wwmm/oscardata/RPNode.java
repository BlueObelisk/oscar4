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
    RPNode(String regex,
                int parseGroup,
                String type,
                String ID,
                String value,
                boolean unique,
                boolean saf,
                RParser rParser
                ) {
        this.setRegex(regex);
        this.parseGroup = parseGroup;
        this.type = type;
        this.id = ID;
        this.value = value;
        this.unique = unique;
        this.saf = saf;
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
    
    
    /**
     * Add a child element.
     * @param elem The XML element corresponding to the &lt;node&gt; to be added
     */
    void addChild(Element elem) {
        // get type, id and value attributes
        String type = elem.getAttributeValue("type");
        String id = elem.getAttributeValue("id");
        String value =  elem.getAttributeValue("value");
        boolean safFlag = false;
        if(elem.getAttribute("saf") != null) safFlag = true;

        // examine node children for unique and regex
        String regex = null;
        boolean unique = false;
        if(elem.getChildElements("unique").size() > 0) {
        	unique = true;
        }
        regex = rParser.getNodeText(elem.getChildElements("regexp").get(0));
        int pg = Integer.parseInt(elem.getChildElements("regexp").get(0).getAttributeValue("parsegroup"));
 
        RPNode RPchild = new RPNode(regex, pg, type, id, value, unique, safFlag, rParser);
        Elements childElems = elem.getChildElements("child");
        for(int i=0;i<childElems.size();i++) {
        	Element child = childElems.get(i);
        	String childType = child.getAttributeValue("type");
        	String childId = child.getAttributeValue("id");
        	// find the <node> the <child> refers to
        	Element node2 = (Element)rParser.findNode(childType, childId);
        	if(node2==null)
        		continue;
        	// node2 is the <node> referred to by <child>
        	// get value attribute, if exists, else null
        	RPchild.addChild(node2);
        }

        children.add(RPchild);
    }

    void parseXOMText(Text textNode) {
    	parseXOMText(textNode, new HashSet<RPNode>());
    }
    
    void parseXOMText(Text textNode, HashSet<RPNode> excludedChildren) {
    	boolean foundSomethingFlag = true;
    	while(foundSomethingFlag) {
    		foundSomethingFlag = false;
        	String txt = textNode.getValue();
            for(ListIterator i=children.listIterator();i.hasNext() && !foundSomethingFlag;) {
                RPNode child = (RPNode)i.next();
                if(excludedChildren.contains(child)) {
                	continue;
                }
                	
                Pattern pat = child.getPattern();
                Matcher m = pat.matcher(txt);
                if(m.find()) {
                	
                    String tokText;
                    int pg = child.getParseGroup();
                    try {
                        tokText = m.group(pg);
                    } catch(ArrayIndexOutOfBoundsException e) {
                    	pg = 0;
                        tokText = m.group(0);
                    }
                    
                    textNode.setValue(txt.substring(0, m.start()));
                    Node currentNode = textNode;
                    if(m.start(pg) > m.start()) {
                    	Text invincibleText = 
                    		new Text(txt.substring(m.start(), m.start(pg)));
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
                    		new Text(txt.substring(m.end(pg), m.end()));
                    	XOMTools.insertAfter(currentNode, invincibleText);
                    	currentNode = invincibleText;                    		
                    }
                	if(child.isUnique()) {
                		excludedChildren.add(child);
                	}
                    if(m.end() < txt.length()) {
                    	Text endText = new Text(txt.substring(m.end()));
                    	XOMTools.insertAfter(currentNode, endText);
                    	parseXOMText(endText, excludedChildren);
                    	// Do something about uniqueness here...
                    }
                    foundSomethingFlag = true;
                }
            }    		
    	}
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
    
}
