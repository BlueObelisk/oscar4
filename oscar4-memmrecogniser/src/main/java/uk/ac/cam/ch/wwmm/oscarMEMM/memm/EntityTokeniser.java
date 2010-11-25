package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/**Holds a paragraph, and detects probable sequences of tags.
 * 
 * @author ptc24
 *
 */
final class EntityTokeniser {

	private List<Map<String,Map<String,Double>>> classifierResults;
	private List<Map<String,Double>> alphas;
	private int length;
	private MEMM memm;
	private ITokenSequence tokSeq;
	
	public EntityTokeniser(MEMM memm, ITokenSequence tokSeq, List<Map<String,Map<String,Double>>> classifierResults) {
		this.tokSeq = tokSeq;
		this.classifierResults = classifierResults;
		this.memm = memm;
		length = classifierResults.size();
		alphas = new ArrayList<Map<String, Double>>();
		for (int i = 0; i < length; i++) {
			Map<String,Double> alphasAtI = new HashMap<String,Double>();
			if(i==0) {
				for(String tag : memm.getTagSet()) {
					alphasAtI.put(tag, classifierResults.get(0).get("O").get(tag));
				}
			} else {
				for(String tag : memm.getTagSet()) {
					double alpha = 0.0;
					for(String prevTag : memm.getTagSet()) {
						alpha += alphas.get(i-1).get(prevTag) * classifierResults.get(i).get(prevTag).get(tag);
					}
					alphasAtI.put(tag, alpha);
				}
			}
			alphas.add(alphasAtI);
		}

	}
	
	public double probEntityStartsAt(NamedEntityType namedEntityType, int position) {
		return alphas.get(position).get("B-"+namedEntityType.getName());			
	}

	
	public double probEntityAt(NamedEntityType namedEntityType, int startPosition, int length) {
		if(startPosition+length > this.length) return 0.0;
		// First: everything up to the start tag(inclusive)
		double score = alphas.get(startPosition).get("B-"+ namedEntityType.getName());
		// Second: the body of the entity - the second and subsequent tags
		for (int i = startPosition+1; i < startPosition+length; i++) {
			String prevTag = "I-" + namedEntityType.getName();
			if(i == startPosition+1) {
				prevTag = "B-" + namedEntityType.getName();
			}
			if(!classifierResults.get(i).get(prevTag).containsKey("I-" + namedEntityType.getName())) {
				return 0;
			} else {
				score *= classifierResults.get(i).get(prevTag).get("I-" + namedEntityType.getName());
			}
		}
		// Third: leave the entity
		int afterPosition = startPosition + length;
		if(afterPosition == this.length) {
			return score;
		} else {
			String prevTag = "I-" + namedEntityType.getName();
			if(length == 1) prevTag = "B-" + namedEntityType.getName();
			double afterTotal = 0.0;
			for(String tag : classifierResults.get(afterPosition).keySet()) {
				if(tag.startsWith("I-")) continue;
				//if(tag.equals("I-" + entityType)) continue;
				afterTotal += classifierResults.get(afterPosition).get(prevTag).get(tag);
			}
			return score * afterTotal;
		}
	}
	
	public Map<NamedEntity,Double> getEntities(double threshold) {
		Map<NamedEntity,Double> entities = new HashMap<NamedEntity,Double>();
		for (int i = 0; i < length; i++) {
			for(NamedEntityType namedEntityType : memm.getNamedEntityTypes()) {
				double entitiesProb = probEntityStartsAt(namedEntityType, i);
				if(entitiesProb > threshold) {
					for (int j = 1; j <= length-i; j++) {
						double entityProb = probEntityAt(namedEntityType, i, j);
						if(entityProb > threshold) {
							
							int startOffset = tokSeq.getToken(i).getStart();
							int endOffset = tokSeq.getToken(i+j-1).getEnd();
							//System.out.println(tokSeq.getToken(i).getValue()+" with a start of"+tokSeq.getToken(i).getStart()+" with an end of "+tokSeq.getToken(i).getEnd());
							String entityStr = tokSeq.getStringAtOffsets(startOffset, endOffset);
    						//System.err.println("Entity str " +entityStr);
							NamedEntityType finalEntityType = namedEntityType;
							if(NamedEntityType.valueOf("NCM").equals(finalEntityType)) namedEntityType = NamedEntityType.COMPOUND;
							if(NamedEntityType.valueOf("NRN").equals(finalEntityType)) namedEntityType = NamedEntityType.REACTION;
							NamedEntity ne = new NamedEntity(tokSeq.getTokens(i,i+j-1), entityStr, namedEntityType);
							ne.setConfidence(entityProb);
							entities.put(ne, entityProb);
						}
						//breaks loops when falls below a certain amount
						entitiesProb -= entityProb;
						if(entitiesProb < threshold) break;
					}
				}
			}
		}
		return entities;
	}
	
}
