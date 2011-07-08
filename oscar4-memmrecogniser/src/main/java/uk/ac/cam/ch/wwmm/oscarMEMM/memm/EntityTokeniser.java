package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.types.BioTag;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/**Holds a paragraph, and detects probable sequences of tags.
 * 
 * @author ptc24
 *
 */
final class EntityTokeniser {

	private List<Map<BioType,Map<BioType,Double>>> classifierResults;
	private List<Map<BioType,Double>> alphas;
	private int length;
	private MEMMModel memm;
	private TokenSequence tokSeq;
	
	public EntityTokeniser(MEMMModel memm, TokenSequence tokSeq, List<Map<BioType,Map<BioType,Double>>> classifierResults) {
		this.tokSeq = tokSeq;
		this.classifierResults = classifierResults;
		this.memm = memm;
		length = classifierResults.size();
		alphas = new ArrayList<Map<BioType, Double>>();
		for (int i = 0; i < length; i++) {
			Map<BioType,Double> alphasAtI = new HashMap<BioType,Double>();
			if(i==0) {
				for(BioType tag : memm.getTagSet()) {
					alphasAtI.put(tag, classifierResults.get(0).get(new BioType(BioTag.O)).get(tag));
				}
			} else {
				for(BioType tag : memm.getTagSet()) {
					double alpha = 0.0;
					for(BioType prevTag : memm.getTagSet()) {
						alpha += alphas.get(i-1).get(prevTag) * classifierResults.get(i).get(prevTag).get(tag);
					}
					alphasAtI.put(tag, alpha);
				}
			}
			alphas.add(alphasAtI);
		}

	}
	
	public double probEntityStartsAt(NamedEntityType namedEntityType, int position) {
		return alphas.get(position).get(
			new BioType(BioTag.B, namedEntityType)
		);			
	}

	
	public double probEntityAt(NamedEntityType namedEntityType, int startPosition, int length) {
		if(startPosition+length > this.length) return 0.0;
		// First: everything up to the start tag(inclusive)
		double score = alphas.get(startPosition).get(
			new BioType(BioTag.B, namedEntityType)
		);
		// Second: the body of the entity - the second and subsequent tags
		for (int i = startPosition+1; i < startPosition+length; i++) {
			BioType prevTag;
			if (i == startPosition+1) {
				prevTag = new BioType(BioTag.B, namedEntityType);
			} else {
                prevTag = new BioType(BioTag.I, namedEntityType);
            }
			if(!classifierResults.get(i).get(prevTag).containsKey(
				new BioType(BioTag.I, namedEntityType))) {
				return 0;
			} else {
				score *= classifierResults.get(i).get(prevTag).get(new BioType(BioTag.I, namedEntityType));
			}
		}
		// Third: leave the entity
		int afterPosition = startPosition + length;
		if (afterPosition == this.length) {
			return score;
		} else {
			BioType prevTag;
			if (length == 1) {
                prevTag = new BioType(BioTag.B, namedEntityType);
            } else {
                 prevTag = new BioType(BioTag.I, namedEntityType);
            }
			double afterTotal = 0.0;
			for(BioType tag : classifierResults.get(afterPosition).keySet()) {
				if (tag.getBio() == BioTag.I) {
                    continue;
                }
				afterTotal += classifierResults.get(afterPosition).get(prevTag).get(tag);
			}
			return score * afterTotal;
		}
	}
	
	public List<NamedEntity> getEntities(double threshold) {
		List<NamedEntity> entities = new ArrayList<NamedEntity>();
		for (int i = 0; i < length; i++) {
			for(NamedEntityType namedEntityType : memm.getNamedEntityTypes()) {
				double entitiesProb = probEntityStartsAt(namedEntityType, i);
				if(entitiesProb > threshold) {
					for (int j = 1; j <= length-i; j++) {
						double entityProb = probEntityAt(namedEntityType, i, j);
						if(entityProb > threshold) {
							
							int startOffset = tokSeq.getToken(i).getStart();
							int endOffset = tokSeq.getToken(i+j-1).getEnd();
							String entityStr = tokSeq.getStringAtOffsets(startOffset, endOffset);
							NamedEntityType finalEntityType = namedEntityType;
							if(NamedEntityType.valueOf("NCM").equals(finalEntityType)) namedEntityType = NamedEntityType.COMPOUND;
							if(NamedEntityType.valueOf("NRN").equals(finalEntityType)) namedEntityType = NamedEntityType.REACTION;
							NamedEntity ne = new NamedEntity(tokSeq.getTokens(i,i+j-1), entityStr, namedEntityType);
							ne.setConfidence(entityProb);
							entities.add(ne);
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
