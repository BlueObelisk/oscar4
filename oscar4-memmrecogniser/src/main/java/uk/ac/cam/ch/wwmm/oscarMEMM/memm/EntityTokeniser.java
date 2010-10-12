package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;

/**Holds a paragraph, and detects probable sequences of tags.
 * 
 * @author ptc24
 *
 */
final class EntityTokeniser {

	List<Map<String,Map<String,Double>>> classifierResults;
	List<Map<String,Double>> alphas;
	int length;
	MEMM memm;
	TokenSequence tokSeq;
	
	public EntityTokeniser(MEMM memm, TokenSequence tokSeq, List<Map<String,Map<String,Double>>> classifierResults) {
		this.tokSeq = tokSeq;
		this.classifierResults = classifierResults;
		this.memm = memm;
		length = classifierResults.size();
		alphas = new ArrayList<Map<String, Double>>();
		for(int i=0;i<length;i++) {
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
	
	public double probEntityStartsAt(String entityType, int position) {
		return alphas.get(position).get("B-"+entityType);			
	}

	
	public double probEntityAt(String entityType, int startPosition, int length) {
		if(startPosition+length > this.length) return 0.0;
		// First: everything up to the start tag(inclusive)
		double score = alphas.get(startPosition).get("B-"+entityType);
		// Second: the body of the entity - the second and subsequent tags
		for(int i=startPosition+1;i<startPosition+length;i++) {
			String prevTag = "I-" + entityType;
			if(i == startPosition+1) {
				prevTag = "B-" + entityType;
			}
			if(!classifierResults.get(i).get(prevTag).containsKey("I-" + entityType)) {
				return 0;
			} else {
				score *= classifierResults.get(i).get(prevTag).get("I-" + entityType);				
			}
		}
		// Third: leave the entity
		int afterPosition = startPosition + length;
		if(afterPosition == this.length) {
			return score;
		} else {
			String prevTag = "I-" + entityType;
			if(length == 1) prevTag = "B-" + entityType;
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
		for(int i=0;i<length;i++) {
			for(String entityType : memm.getEntityTypes()) {
				double entitiesProb = probEntityStartsAt(entityType, i);
				if(entitiesProb > threshold) {
					for(int j=1;j<=length-i;j++) {
						double entityProb = probEntityAt(entityType, i, j);
						if(entityProb > threshold) {
							
							int startOffset = tokSeq.getToken(i).getStart();
							int endOffset = tokSeq.getToken(i+j-1).getEnd();
							//System.out.println(tokSeq.getToken(i).getValue()+" with a start of"+tokSeq.getToken(i).getStart()+" with an end of "+tokSeq.getToken(i).getEnd());
							String entityStr = tokSeq.getStringAtOffsets(startOffset, endOffset);
    						//System.err.println("Entity str " +entityStr);
							String finalEntityType = entityType;
							if(finalEntityType.equals("NCM")) entityType = "CM";
							if(finalEntityType.equals("NRN")) entityType = "RN";
							NamedEntity ne = new NamedEntity(tokSeq.getTokens(i,i+j-1), entityStr, entityType);
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
