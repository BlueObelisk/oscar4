package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/**A named entity, as found by one of the various routines in this package.
 * May be one token or multi-token.
 * 
 * @author ptc24
 *
 */
public final class NamedEntity implements Annotation, Comparable<NamedEntity> {

	private int startOffset;
	private int endOffset;
	private String surface;
	private NamedEntityType type;
	private Token endToken;
	private List<Token> tokens;
	private Set<String> ontIds = new HashSet<String>();
	private Set<String> custTypes = new HashSet<String>();
	private String leftPunct;
	private String rightPunct;
	
	private double confidence;
	private double pseudoConfidence;
	private boolean deprioritiseOnt;
	private boolean blocked;
	
	/**Creates a new NamedEntity.
	 * 
	 * @param tokens The tokens that constitute the named entity.
	 * @param surface The text string of the named entity.
	 * @param type The type of the named entity.
	 */
	public NamedEntity(List<Token> tokens, String surface, NamedEntityType type) {
		confidence = Double.NaN;
		pseudoConfidence = Double.NaN;
		deprioritiseOnt = false;
		blocked = false;
		this.tokens = tokens;
		endToken = tokens.get(tokens.size()-1);
		startOffset = tokens.get(0).getStart();
		endOffset = endToken.getEnd();
		this.surface = surface;
		this.type = type;
		addPunctuation();
	}

    public NamedEntity(String surface, int start, int end, NamedEntityType type) {
        this.surface = surface;
        this.startOffset = start;
        this.endOffset = end;
        this.type = type;
        this.confidence = Double.NaN;
        this.pseudoConfidence = Double.NaN;
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
		ne.startOffset = t.getStart();
		ne.endOffset = ne.startOffset + prefix.length();
		ne.surface = prefix;
		ne.type = NamedEntityType.LOCANTPREFIX;
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
	public NamedEntityType getType() {
		return type;
	}
		
	/**Sets the type of the named entity.
	 * 
	 * @param type The type of the named entity.
	 */
	public void setType(NamedEntityType type) {
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
	
	/**Gets the ontology identifiers for the named entity. Will be null if
	 * the named entity has no ontology ids, thought the named entity need
	 * not be of type ONT to have ontology ids.
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
		//TODO why might we be passing null in?
		if (newOntIds == null) {
            return;
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
		//TODO why might we be passing null in?
		if(newCustTypes == null) {
			return;
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
			if(prevToken.getEnd() != tmpLeftOffset) break;
            String v = prevToken.getSurface();
			if(v.length() != 1) break;
			if(!("()[]{}.,;:?!'\"".contains(v) || StringTools.isQuoteMark(v))) break;
			leftPunct = v + leftPunct;
			tmpLeftOffset = prevToken.getStart();
			prevToken = prevToken.getNAfter(-1);
		}
		rightPunct = "";
		int tmpRightOffset = endOffset;
		Token nextToken = tokens.get(tokens.size()-1).getNAfter(1);
		while(nextToken != null) {
			if(nextToken.getStart() != tmpRightOffset) break;
            String v = nextToken.getSurface();
            if(v.length() != 1) break;
			if(!("()[]{}.,;:?!'\"".contains(v) || StringTools.isQuoteMark(v) || StringTools.isHyphen(v))) break;
			rightPunct = rightPunct + v;
			tmpRightOffset = nextToken.getEnd();
			nextToken = nextToken.getNAfter(1);
		}
		
	}
	
	/**Compares the confidence score of the named entity 
	 * with that of another named entity, for use when deciding which of two
	 * overlapping named entities to discard. Psuedo confidences are not used
	 * 
	 */
	public int compareCalculatedConfidenceTo(NamedEntity otherNe) {
        double myConf = confidence;
        if(Double.isNaN(myConf)) {
            return 0;
        }

        double otherConf = otherNe.getConfidence();
        if(Double.isNaN(otherConf)) {
            return 0;
        }

        return Double.compare(myConf, otherConf);
	}
	
	/**Compares the confidence/pseudo confidence score of the named entity 
	 * with that of another named entity, for use when deciding which of two
	 * overlapping named entities to discard.
	 * 
	 */
	public int comparePseudoOrCalculatedConfidenceTo(NamedEntity otherNe) {
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
	}
	
	/**Compares the type of the named entity with that of another named entity
	 * that has the same string, for use when deciding which of two overlapping
	 * named entities to discard.
	 * 
	 */
	public int compareTypeTo(NamedEntity otherNe) {
        return Integer.valueOf(getType().getPriority()).compareTo(otherNe.getType().getPriority());
	}
	
	
	/**Compares the end offset of the named entity with that of another named
	 * entity, for use when deciding which of two overlapping named entities to
	 * discard.
	 * 
	 */	
	public int compareEnd(NamedEntity otherNe) {
        return new Integer(endOffset).compareTo(otherNe.endOffset);
	}
	
	/**Compares the end offset of the named entity with the start offset
	 * of another named entity, to see whether the two might overlap.
	 * 
	 */
	public int compareEndToStart(NamedEntity otherNe) {
        return new Integer(endOffset).compareTo(otherNe.startOffset);
	}
	
	/**Compares the start offset of the named entity with that of another named
	 * entity, for use when deciding which of two overlapping named entities to
	 * discard.
	 * 
	 */	
	public int compareStart(NamedEntity otherNe) {
        return new Integer(startOffset).compareTo(otherNe.startOffset);
	}
	
	/**Compares the start offset of the named entity with the end offset
	 * of another named entity, to see whether the two might overlap.
	 * 
	 */
	public int compareStartToEnd(NamedEntity other) {
        return new Integer(startOffset).compareTo(other.endOffset);
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
	 * @return The list of tokens that constitute the named entity.
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
	
	/**
	 * Gets the pseudoConfidence score (from 1.0 to 0.0, or NaN) for the named entity.
	 */
	public double getPseudoConfidence() {
		return pseudoConfidence;
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
	
	public boolean getDeprioritiseOnt() {
		return deprioritiseOnt;
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
	 * @param otherNe
	 * @return int indicating sort order
	 */
	public int comparePropertiesSpecifiedPrioritisation(NamedEntity otherNe) {
        if (deprioritiseOnt) {
            if (NamedEntityType.ONTOLOGY.equals(type) && !NamedEntityType.ONTOLOGY.equals(otherNe.type)) {
                return -1;
            } else if (!NamedEntityType.ONTOLOGY.equals(type) && NamedEntityType.ONTOLOGY.equals(otherNe.type)) {
                return 1;
            }
        }
		return 0;
	}

    public int compareTo(NamedEntity other) {
        int startComparison = compareStart(other);
        int endComparison = compareEnd(other);
        if (startComparison < 0) {
            return -1;
        } else if (startComparison > 0) {
            return 1;
        } else if (endComparison < 0) {
            return -1;
        } else if (endComparison > 0) {
            return 1;
        } else {
            return 0;
        }
	}

	public boolean conflictsWith(NamedEntity other) {
		// If this starts after (or at) the end of the other, no conflict
		if (compareStartToEnd(other) >= 0) {
            return false;
        }
		// If this ends before (or at) the start of the other, no conflict
		if (compareEndToStart(other) <= 0) {
            return false;
        }
		// Therefore, there must be a conflict
		return true;
	}


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NamedEntity) {
            NamedEntity that = (NamedEntity) o;
            return getStart() == that.getStart()
                    && getEnd() == that.getEnd()
                    && getSurface().equals(that.getSurface())
                    && getType().equals(that.getType())
                    && isBlocked() == that.isBlocked()
                    && compareConf(getConfidence(), that.getConfidence())
                    && compareConf(getPseudoConfidence(), that.getPseudoConfidence())
                    && getCustTypes().containsAll(that.getCustTypes()) && that.getCustTypes().containsAll(getCustTypes())
                    && getOntIds().containsAll(that.getOntIds()) && that.getOntIds().containsAll(getOntIds())
            ;
        }
        return false;
    }

    /**
     * Compares two confidence doubles, return true if they are
     * (floating-point) equal or both are NaN 
     * @return
     */
    private boolean compareConf(double a, double b) {
		if (Double.isNaN(a) && Double.isNaN(b)) {
			return true;
		}
    	return Math.abs(a - b) < 0.0001;
	}

	@Override
    public int hashCode() {
        int result = startOffset;
        result = 31 * result + endOffset;
        result = 31 * result + (surface != null ? surface.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
