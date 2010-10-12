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
public final class TokenSequence {

	private String sourceString;
	private int offset;
	private ProcessingDocument doc;
	private List<Token> tokens;
	private Element elem;
	private int indes=0;
	
	public TokenSequence(String sourceString, int offset, ProcessingDocument doc, List<Token> tokens) 
	{
		this.sourceString = sourceString;
        this.offset = offset;
		this.doc = doc;
		this.tokens = tokens;
	}
	
	/**Gets the string that was tokenised to make this TokenSequence.
	 * 
	 * @return The string that was tokenised to make this TokenSequence.
	 */
	public String getSourceString() {
		return sourceString;
	}
	
	/**Gets the ProcessingDocument (or null) that this TokenSequence was made
	 * from.
	 * 
	 * @return The ProcessingDocument (or null) that this TokenSequence was made
	 * from.
	 */
	public ProcessingDocument getDoc() {
		return doc;
	}
	
	/**Gets the start offset of this TokenSequence. If this information was
	 * not supplied during tokenisation, this will be 0.
	 * 
	 * @return The start offset of this TokenSequence.
	 */
	public int getOffset() {
		return offset;
	}
	
	/**Gets the list of tokens that comprise this TokenSequence.
	 * 
	 * @return The list of tokens that comprise this TokenSequence.
	 */
	public List<Token> getTokens() {
		return tokens;
	}
	
	/**Gets a the sublist of tokens that occur between the given indices.
	 * 
	 * @param from The first token in the sublist (inclusive).
	 * @param to The last token in the sublist (inclusive).
	 * @return The sublist of tokens.
	 */
	public List<Token> getTokens(int from, int to) {
		return tokens.subList(from, to+1);
	}
	
	/**Gets a single token.
	 * 
	 * @param i The index of the token to get.
	 * @return The token.
	 */
	public Token getToken(int i) {
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

	/**Gets the number of tokens in the TokenSequence.
	 * 
	 * @return the number of tokens in the TokenSequence.
	 */
	public int size() {
		return tokens.size();
	}

	/**Gets a list of strings corresponding to the tokens.
	 * 
	 * @return The list of strings corresponding to the tokens.
	 */
	public List<String> getTokenStringList() {
		List<String> tl = new ArrayList<String>();
		for(Token t : tokens) {
			tl.add(t.value);
		}
		return tl;
	}
	
	/**Gets a substring of the source string that runs between two tokens 
	 * (inclusive).
	 * 
	 * @param startToken The first token (inclusive).
	 * @param endToken The last token (inclusive).
	 * @return The substring.
	 */
	public String getSubstring(int startToken, int endToken) {
		if(endToken >= size()) endToken = size()-1;
		int startOffset = tokens.get(startToken).start;
		int endOffset = tokens.get(endToken).end;
		return sourceString.substring(startOffset - offset, endOffset - offset);
	}
	
	/**Gets a substring of the source string that runs between two offsets.
	 * 
	 * @param start The start offset.
	 * @param end The end offset.
	 * @return The substring.
	 */
	public String getStringAtOffsets2(int start, int end) {
		//System.err.println(" Source String text==="+sourceString);
		System.err.println(" Source string length == "+sourceString.length()+" with a start of "+start+ " and offset="+offset+" end =="+end);
		if(end > sourceString.length() + offset) end= sourceString.length();
		if(start < sourceString.length()) start = sourceString.length() -offset ;
		return sourceString.substring(start - offset, end - offset);
	}
	public String getStringAtOffsets(int start, int end) {
		return sourceString.substring(start - offset, end - offset);
	}

	/**Gets all of the token values of tokens that are hyphenated with 
	 * named entities. For example, this would get "based" in "acetone-based".
	 * 
	 * @return The token values.
	 */
	public Set<String> getAfterHyphens() {
		Set<String> afterHyphens = new HashSet<String>();
		for(int i=1;i<tokens.size();i++) {
			if(i < tokens.size()-1
				&& tokens.get(i).getValue().length() == 1
				&& StringTools.hyphens.contains(tokens.get(i).getValue())
				&& "O".equals(tokens.get(i).getBioTag())
				&& "O".equals(tokens.get(i+1).getBioTag())
				&& !"O".equals(tokens.get(i-1).getBioTag())
				&& tokens.get(i).start == tokens.get(i-1).end
				&& tokens.get(i).end == tokens.get(i+1).start
					) {
				afterHyphens.add(tokens.get(i+1).getValue());
			} else if("O".equals(tokens.get(i).getBioTag())
				&& "B-CPR".equals(tokens.get(i-1).getBioTag())
				&& tokens.get(i).start == tokens.get(i-1).end
			) {
				afterHyphens.add(tokens.get(i).getValue());				
			}
		}
		return afterHyphens;
	}

	/**Gets all of the named entities in the TokenSequence. This produces a
	 * map, where the keys are the named entity types. The values are a list
	 * of all NEs of the corresponding type, which are represented as lists
	 * of strings.
	 * 
	 * @return The named entities.
	 */
	public Map<String,List<List<String>>> getNes() {
		Map<String,List<List<String>>> neMap = new HashMap<String,List<List<String>>>();
		String neType = null;
		List<String> neTokens = null;
		for(Token t : tokens) {
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
	
	/**Gets the string values of all of the non-NE tokens.
	 * 
	 * @return The string values.
	 */
	public List<String> getNonNes() {
		List<String> nonNes = new ArrayList<String>();
		for(Token t : tokens) {
			if("O".equals(t.getBioTag())) nonNes.add(t.getValue());
		}
		return nonNes;
	}
	
	/**Converts the BIO-coding of the NE information to a BIOEW-coding.
	 * This alters the Token objects that comprise the token sequence.
	 * 
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

	/**Converts the BIO-coding of the NE information to a much richer tagset.
	 * This alters the Token objects that comprise the token sequence.
	 * 
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
