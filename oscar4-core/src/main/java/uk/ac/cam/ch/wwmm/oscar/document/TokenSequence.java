package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

import nu.xom.Element;

/**A tokenised representation of a piece of text, as made by the Tokeniser
 * class.
 * 
 * @author ptc24
 *
 */
public final class TokenSequence implements ITokenSequence {

	private String surface;
	private int offset;
	private IProcessingDocument doc;
	private List<Token> tokens;
	private Element elem;
	
	public TokenSequence(String sourceString, int offset, IProcessingDocument doc, List<Token> tokens) 
	{
		this.surface = sourceString;
        this.offset = offset;
		this.doc = doc;
		this.tokens = tokens;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getSourceString()
	 */
	public String getSurface() {
		return surface;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getDoc()
	 */
	public IProcessingDocument getDoc() {
		return doc;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getOffset()
	 */
	public int getOffset() {
		return offset;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getTokens()
	 */
	public List<Token> getTokens() {
		return tokens;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getTokens(int, int)
	 */
	public List<Token> getTokens(int from, int to) {
		return tokens.subList(from, to+1);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getToken(int)
	 */
	public IToken getToken(int i) {
		return tokens.get(i);
	}
	
	public void setElem(Element elem) {
		this.elem = elem;
	}
	
	/**Gets the XML element (or null) containing the named entity information
	 * that was used during tokenisation.
	 * 
	 * @return The XML element.
	 */
	public Element getElem() {
		return elem;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#size()
	 */
	public int size() {
		return tokens.size();
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getTokenStringList()
	 */
	public List<String> getTokenStringList() {
		List<String> tl = new ArrayList<String>();
		for(IToken t : tokens) {
			tl.add(t.getValue());
		}
		return tl;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getSubstring(int, int)
	 */
	public String getSubstring(int startToken, int endToken) {
		if(endToken >= size()) endToken = size()-1;
		int startOffset = tokens.get(startToken).getStart();
		int endOffset = tokens.get(endToken).getEnd();
		return surface.substring(startOffset - offset, endOffset - offset);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getStringAtOffsets2(int, int)
	 */
	public String getStringAtOffsets2(int start, int end) {
		//System.err.println(" Source String text==="+sourceString);
		System.err.println(" Source string length == "+surface.length()+" with a start of "+start+ " and offset="+offset+" end =="+end);
		if(end > surface.length() + offset) end= surface.length();
		if(start < surface.length()) start = surface.length() -offset ;
		return surface.substring(start - offset, end - offset);
	}
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getStringAtOffsets(int, int)
	 */
	public String getStringAtOffsets(int start, int end) {
		return surface.substring(start - offset, end - offset);
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getAfterHyphens()
	 */
	public Set<String> getAfterHyphens() {
		Set<String> afterHyphens = new HashSet<String>();
		for(int i=1;i<tokens.size();i++) {
			if(i < tokens.size()-1
				&& tokens.get(i).getValue().length() == 1
				&& StringTools.isHyphen(tokens.get(i).getValue())
				&& "O".equals(tokens.get(i).getBioTag())
				&& "O".equals(tokens.get(i+1).getBioTag())
				&& !"O".equals(tokens.get(i-1).getBioTag())
				&& tokens.get(i).getStart() == tokens.get(i-1).getEnd()
				&& tokens.get(i).getEnd() == tokens.get(i+1).getStart()
					) {
				afterHyphens.add(tokens.get(i+1).getValue());
			} else if("O".equals(tokens.get(i).getBioTag())
				&& "B-CPR".equals(tokens.get(i-1).getBioTag())
				&& tokens.get(i).getStart() == tokens.get(i-1).getEnd()
			) {
				afterHyphens.add(tokens.get(i).getValue());				
			}
		}
		return afterHyphens;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getNes()
	 */
	public Map<String,List<List<String>>> getNes() {
		Map<String,List<List<String>>> neMap = new HashMap<String,List<List<String>>>();
		String neType = null;
		List<String> neTokens = null;
		for(IToken t : tokens) {
			if(neType == null) {
				if(!"O".equals(t.getBioTag())) {
					neTokens = new ArrayList<String>();
					// Trim of the B- in the BIO tag
					neType = t.getBioTag().substring(2);
					neTokens.add(t.getValue());
					if(!neMap.containsKey(neType)) neMap.put(neType, new ArrayList<List<String>>());
					neMap.get(neType).add(neTokens);
				}
			} else {
				if("O".equals(t.getBioTag())) {
					neType = null;
					neTokens = null;
				} else if(t.getBioTag().startsWith("B-")) {
					neTokens = new ArrayList<String>();
					// Trim of the B- in the BIO tag
					neType = t.getBioTag().substring(2);
					neTokens.add(t.getValue());
					if(!neMap.containsKey(neType)) neMap.put(neType, new ArrayList<List<String>>());
					neMap.get(neType).add(neTokens);					
					// Must be I- something then
				} else {
					neTokens.add(t.getValue());
				}
			}
		}
		return neMap;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getNonNes()
	 */
	public List<String> getNonNes() {
		List<String> nonNes = new ArrayList<String>();
		for(IToken t : tokens) {
			if("O".equals(t.getBioTag())) nonNes.add(t.getValue());
		}
		return nonNes;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#toBIOEW()
	 */
	public void toBIOEW() {
		for(int i=0;i<tokens.size();i++) {
			String tag = tokens.get(i).getBioTag();
			if(!tag.equals("O")) {
				//String prevTag = "OOS";
				String nextTag = "OOS";
				//if(i > 0) prevTag = tokens.get(i-1).getBioTag();
				if(i < tokens.size() - 1) nextTag = tokens.get(i+1).getBioTag();
				if(tag.startsWith("B") && !nextTag.equals("I-" + tag.substring(2))) {
					tokens.get(i).setBioTag("W-" + tag.substring(2));
				} else if(tag.startsWith("I-") && !nextTag.equals("I-" + tag.substring(2))) {
					tokens.get(i).setBioTag("E-" + tag.substring(2));
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#toRichTags()
	 */
	public void toRichTags() {
		for(int i=0;i<tokens.size();i++) {
			String tag = tokens.get(i).getBioTag();
			//String prevTag = "OOS";
			//String nextTag = "OOS";
			String prevTag = "O";
			String nextTag = "O";
			if(i > 0) prevTag = tokens.get(i-1).getBioTag();
			if(i < tokens.size() - 1) nextTag = tokens.get(i+1).getBioTag();
			if(tag.equals("O")) {
				if(prevTag.equals("OOS")) {
					if(nextTag.equals("O")) {
						tokens.get(i).setBioTag("O-B-OOS");						
					} else if(nextTag.equals("OOS")) {
						tokens.get(i).setBioTag("O-W-OOS");
					} else {
						tokens.get(i).setBioTag("O-W-" + nextTag.substring(2));					
					}
				} else if(nextTag.equals("OOS")) {
					if(prevTag.startsWith("O")) {
						tokens.get(i).setBioTag("O-E-OOS");												
					} else {
						tokens.get(i).setBioTag("O-W-OOS");						
					}
				} else if(prevTag.startsWith("O") && nextTag.equals("O")) {
					tokens.get(i).setBioTag("O-I");
				} else if(prevTag.startsWith("O")) {
					tokens.get(i).setBioTag("O-E-" + nextTag.substring(2));
				} else if(nextTag.startsWith("O")) {
					tokens.get(i).setBioTag("O-B-" + prevTag.substring(2));					
				} else {
					tokens.get(i).setBioTag("O-W-" + nextTag.substring(2));					
				}
			} else {				
				if(tag.startsWith("B") && !nextTag.equals("I-" + tag.substring(2))) {
					tokens.get(i).setBioTag("W-" + tag.substring(2));
				} else if(tag.startsWith("I-") && !nextTag.equals("I-" + tag.substring(2))) {
					tokens.get(i).setBioTag("E-" + tag.substring(2));
				}
			}
		}
	}

}
