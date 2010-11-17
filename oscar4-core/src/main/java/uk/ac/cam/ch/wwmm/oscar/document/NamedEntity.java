package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityTypes;

import nu.xom.Element;

/**A named entity, as found by one of the various routines in this package.
 * May be one token or multi-token.
 * 
 * @author ptc24
 *
 */
public final class NamedEntity extends ResolvableStandoff {

	private int startOffset;
	private int endOffset;
	private String surface;
	private String type;
	private Token endToken;
	private List<Token> tokens;
	private Set<String> ontIds;
	private Set<String> custTypes;
	private String leftPunct;
	private String rightPunct;
	
	private double confidence;
	private double pseudoConfidence;
	private boolean deprioritiseOnt;
	private boolean blocked;
	
	/**Creates a new NamedEntity.
	 * 
	 * @param tokens The tokens that consititue the named entity.
	 * @param surface The text string of the named entity.
	 * @param type The type of the named entity.
	 */
	public NamedEntity(List<Token> tokens, String surface, String type) {
		confidence = Double.NaN;
		pseudoConfidence = Double.NaN;
		deprioritiseOnt = false;
		blocked = false;
		this.tokens = tokens;
		endToken = tokens.get(tokens.size()-1);
		startOffset = tokens.get(0).start;
		endOffset = endToken.end;
		this.surface = surface;
		this.type = type;
		ontIds = null;
		custTypes = null;
		addPunctuation();
	}
	
	/**Creates a named entity, corresponding to a prefix of a single token,
	 * of type CPR.
	 * 
	 * @param t The token that contains the prefix.
	 * @param prefix The string of the prefix.
	 * @return The named entity corresponding to the prefix.
	 */
	public static NamedEntity forPrefix(Token t, String prefix) {
		NamedEntity ne = new NamedEntity(); 
	    ne.tokens = new ArrayList<Token>();
		ne.tokens.add(t);
		ne.endToken = t;
		ne.startOffset = t.start;
		ne.endOffset = ne.startOffset + prefix.length();
		ne.surface = prefix;
		ne.type = NamedEntityTypes.LOCANTPREFIX;
		ne.ontIds = null;
		ne.custTypes = null;
		ne.addPunctuation();
		return ne;
	}
	
	private NamedEntity() {
		confidence = Double.NaN;
		pseudoConfidence = Double.NaN;
		deprioritiseOnt = false;
		blocked = false;
	}

	/**Gets the type of the named entity.
	 * 
	 * @return The type of the named entity.
	 */
	public String getType() {
		return type;
	}
		
	/**Sets the type of the named entity.
	 * 
	 * @param type The type of the named entity.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**Gets the start offset of the named entity.
	 * 
	 * @return The start offset of the named entity.
	 */
	public int getStart() {
		return startOffset;
	}
	
	/**Gets the end offset of the named entity.
	 * 
	 * @return The end offset of the named entity.
	 */
	public int getEnd() {
		return endOffset;
	}
	
	/**Gets the text string of to the named entity.
	 * 
	 * @return The text string of the named entity.
	 */
	public String getSurface() {
		return surface;
	}
	
	/**Gets the ontology identifiers (if any) for the named entity.
	 * 
	 * @return The ontology identifiers (if any) for the named entity.
	 */
	public Set<String> getOntIds() {
		return ontIds;
	}
	
	/**Gets the custom entity types (if any) for the named entity.
	 * 
	 * @return The custom entity types (if any) for the named entity.
	 */
	public Set<String> getCustTypes() {
		return custTypes;
	}
	
	/**Adds ontology identifiers to the named entity.
	 * 
	 * @param newOntIds The ontology identifiers to add.
	 */
	public void addOntIds(Set<String> newOntIds) {
		if(newOntIds == null) return;
		if(newOntIds.size() == 0) return;
		if(ontIds == null) {
			ontIds = new HashSet<String>();
		}
		ontIds.addAll(newOntIds);
	}
	
	/**Sets the ontology identifiers for the named entity.
	 * 
	 * @param ontIds The ontology identifiers for the named entity.
	 */
	public void setOntIds(Set<String> ontIds) {
		this.ontIds = ontIds;
	}

	/**Adds custom entity types to the named entity.
	 * 
	 * @param newCustTypes The custom entity types to add.
	 */
	public void addCustTypes(Set<String> newCustTypes) {
		if(newCustTypes == null) return;
		if(newCustTypes.size() == 0) return;
		if(custTypes == null) {
			custTypes = new HashSet<String>();
		}
		custTypes.addAll(newCustTypes);
	}
	
	/**Sets the custom entity types for the named entity.
	 * 
	 * @param custTypes The custom entity types for the named entity.
	 */
	public void setCustTypes(Set<String> custTypes) {
		this.custTypes = custTypes;
	}
	

	/**Analyses the context of the named entity, recording the presence of
	 * nearby punctuation to be included as attributes of the named entity.
	 * 
	 */
	public void addPunctuation() {
		leftPunct = "";
		int tmpLeftOffset = startOffset;
		Token prevToken = tokens.get(0).getNAfter(-1);
		while(prevToken != null) {
			if(prevToken.end != tmpLeftOffset) break;
			if(prevToken.value.length() != 1) break;
			if(!("()[]{}.,;:?!'\""+StringTools.quoteMarks).contains(prevToken.value)) break;
			leftPunct = prevToken.value + leftPunct;
			tmpLeftOffset = prevToken.start;
			prevToken = prevToken.getNAfter(-1);
		}
		rightPunct = "";
		int tmpRightOffset = endOffset;
		Token nextToken = tokens.get(tokens.size()-1).getNAfter(1);
		while(nextToken != null) {
			if(nextToken.start != tmpRightOffset) break;
			if(nextToken.value.length() != 1) break;
			if(!("()[]{}.,;:?!'\""+StringTools.quoteMarks+StringTools.hyphens).contains(nextToken.value)) break;
			rightPunct = rightPunct + nextToken.value;
			tmpRightOffset = nextToken.end;
			nextToken = nextToken.getNAfter(1);
		}
		
	}
	
	/**Compares the confidence score of the named entity 
	 * with that of another named entity, for use when deciding which of two
	 * overlapping named entities to discard. Psuedo confidences are not used
	 * 
	 */
	@Override
	public int compareCalculatedConfidenceTo(ResolvableStandoff other) {
		if(other instanceof NamedEntity) {
			NamedEntity otherNe = (NamedEntity)other;
			
			double myConf = confidence;
			if(Double.isNaN(myConf)) {
				return 0;
			}

			double otherConf = otherNe.getConfidence();
			if(Double.isNaN(otherConf)) {
				return 0;
			}
			
			return Double.compare(myConf, otherConf);
		} else {
			return 0;
		}
	}
	
	/**Compares the confidence/pseudo confidence score of the named entity 
	 * with that of another named entity, for use when deciding which of two
	 * overlapping named entities to discard.
	 * 
	 */
	@Override
	public int comparePseudoOrCalculatedConfidenceTo(ResolvableStandoff other) {
		if(other instanceof NamedEntity) {
			NamedEntity otherNe = (NamedEntity)other;
			
			double myConf = confidence;
			if(Double.isNaN(myConf)) myConf = pseudoConfidence;
			if(Double.isNaN(myConf)) {
				return 0;
			}

			double otherConf = otherNe.getConfidence();
			if(Double.isNaN(otherConf)) otherConf = otherNe.pseudoConfidence;
			if(Double.isNaN(otherConf)) {
				return 0;
			}
			
			return Double.compare(myConf, otherConf);
		} else {
			return 0;
		}
	}
	
	/**Compares the type of the named entity with that of another named entity
	 * that has the same string, for use when deciding which of two overlapping
	 * named entities to discard.
	 * 
	 */
	@Override
	public int compareTypeTo(ResolvableStandoff other) {
		if(other instanceof NamedEntity) {
			NamedEntity otherNe = (NamedEntity)other;
			return NamedEntityTypes.getPriority(type).compareTo(NamedEntityTypes.getPriority(otherNe.type));
		} else {
			return 0;
		}
	}
	
	
	/**Compares the end offset of the named entity with that of another named
	 * entity, for use when deciding which of two overlapping named entities to
	 * discard.
	 * 
	 */	
	@Override
	public int compareEnd(ResolvableStandoff other) {	
		if(other instanceof NamedEntity) {
			NamedEntity otherNe = (NamedEntity)other;			
			return new Integer(endOffset).compareTo(otherNe.endOffset);
		} else {
			return 0;
		}
	}
	
	/**Compares the end offset of the named entity with the start offset
	 * of another named entity, to see whether the two might overlap.
	 * 
	 */
	@Override
	public int compareEndToStart(ResolvableStandoff other) {
		if(other instanceof NamedEntity) {
			NamedEntity otherNe = (NamedEntity)other;			
			return new Integer(endOffset).compareTo(otherNe.startOffset);
		} else {
			return 0;
		}
	}
	
	/**Compares the start offset of the named entity with that of another named
	 * entity, for use when deciding which of two overlapping named entities to
	 * discard.
	 * 
	 */	
	@Override
	public int compareStart(ResolvableStandoff other) {
		if(other instanceof NamedEntity) {
			NamedEntity otherNe = (NamedEntity)other;			
			return new Integer(startOffset).compareTo(otherNe.startOffset);
		} else {
			return 0;
		}
	}
	
	/**Compares the start offset of the named entity with the end offset
	 * of another named entity, to see whether the two might overlap.
	 * 
	 */
	@Override
	public int compareStartToEnd(ResolvableStandoff other) {
		if(other instanceof NamedEntity) {
			NamedEntity otherNe = (NamedEntity)other;			
			return new Integer(startOffset).compareTo(otherNe.endOffset);
		} else {
			return 0;
		}
	}
	
	/**A string representation of the NE, for debugging and related purposes.
	 * 
	 */
	@Override
	public String toString() {
		return("[NE:" + type + ":" + startOffset + ":" + endOffset + ":" + surface + "]");
	}
	
	/**Gets the list of tokens that constitute the named entity.
	 * 
	 * @return The list of tokens that consititute the named entity.
	 */
	public List<Token> getTokens() {
		return tokens;
	}
	
	/**Gets the confidence score (from 1.0 to 0.0) for the named entity.
	 * 
	 * @return The confidence score (from 1.0 to 0.0) for the named entity.
	 */
	public double getConfidence() {
		return confidence;
	}
	
	/**Sets the confidence score (from 1.0 to 0.0) for the named entity.
	 * 
	 * @param confidence The confidence score (from 1.0 to 0.0) for the
	 * named entity.
	 */
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	/**Sets a "pseudo-confidence" score, to allow this token to be compared
	 * to other named entities for the purposes of working out which one to
	 * discard if they overlap.
	 * 
	 * @param pseudoConfidence The pseudo-confidence score (from 1.0 to 0.0) 
	 * for the named entity.
	 */
	public void setPseudoConfidence(double pseudoConfidence) {
		this.pseudoConfidence = pseudoConfidence;
	}
	
	/**Sets whether this named entity, if an ontology term, should take be deprioritised
	 * over other named entities when working out which entity to discard when
	 * resolving overlaps. Default is false.
	 * 
	 * @param deprioritiseOnt
	 */
	public void setDeprioritiseOnt(boolean deprioritiseOnt) {
		this.deprioritiseOnt = deprioritiseOnt;
	}
	
	/**Gets whether this entity is known to be "blocked" by overlapping with
	 * a conflicting higher-confidence entity.
	 * 
	 * @return Whether this entity is known to be "blocked".
	 */
	public boolean isBlocked() {
		return blocked;
	}
	
	/**Sets whether this entity is known to be "blocked" by overlapping with
	 * a conflicting higher-confidence entity.
	 * 
	 * @param blocked Whether this entity is known to be "blocked".
	 */
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}
	
	/**Gets the first token of the named entity.
	 * 
	 * @return The first token of the named entity.
	 */
	public Token getFirstToken() {
		return tokens.get(0);
	}
	
	/**Gets the last token of the named entity.
	 * 
	 * @return The last token of the named entity.
	 */
	public Token getLastToken() {
		return tokens.get(tokens.size() -1);
	}
	
	/**
	 * If deprioritiseOnt is true will sort such that a non ontological term is prioritised
	 * @param other
	 * @return int indicating sort order
	 */
	public int comparePropertiesSpecifiedPrioritisation(ResolvableStandoff other) {
		if(other instanceof NamedEntity) {
			NamedEntity otherNe = (NamedEntity)other;
			if (deprioritiseOnt){				if(type.equals(NamedEntityTypes.ONTOLOGY) && !otherNe.type.equals(NamedEntityTypes.ONTOLOGY)) {
					return -1;
				} else if(!type.equals(NamedEntityTypes.ONTOLOGY) && otherNe.type.equals(NamedEntityTypes.ONTOLOGY)) {
					return 1;
				}
			}
		}
		return 0;
	}

}
