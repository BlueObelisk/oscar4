package uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**Converts a set of named entities into a set of features for the rescorer.
 * 
 * @author ptc24
 *
 */
/*
 * @dmj30
 * There is another class called FeatureExtractor in the
 * oscarMEMM.memm package with apparently different functionality
 */
//TODO deal with name duplication
final class FeatureExtractor {

	List<NamedEntity> entities;
	Map<String,Double> averageScores;
	Map<String,Double> maxScores;
	Map<String,List<String>> abbrevFeatures;
		
	Pattern allCaps = Pattern.compile("[A-Z]+");
	
	public FeatureExtractor(List<NamedEntity> entities) {
		this.entities = entities;

		abbrevFeatures = new HashMap<String,List<String>>();
		
		Map<String,List<Double>> scoresForEntity = new HashMap<String,List<Double>>();
		for(NamedEntity ne : entities) {
			if(!ne.isBlocked());
			String surf = ne.getSurface();
			if(!scoresForEntity.containsKey(surf)) scoresForEntity.put(surf, new ArrayList<Double>());
			scoresForEntity.get(surf).add(ne.getConfidence());
		}
		averageScores = new HashMap<String,Double>();
		maxScores = new HashMap<String,Double>();
		for(String surf : scoresForEntity.keySet()) {
			DescriptiveStatistics descStats = new DescriptiveStatistics();
			for(double score : scoresForEntity.get(surf)) {
				descStats.addValue(score);
			}
			maxScores.put(surf, descStats.getMax());
			averageScores.put(surf, descStats.getMean());
		}
		
		Map<Token,NamedEntity> neByLastToken = new HashMap<Token,NamedEntity>();
		for(NamedEntity ne : entities) {
			if(!ne.isBlocked()) {
				neByLastToken.put(ne.getLastToken(), ne);
			}
		}

		for(NamedEntity ne : entities) {

			Token prev = ne.getFirstToken().getNAfter(-1);
			Token next = ne.getLastToken().getNAfter(1);

			if(prev != null && next != null && prev.getSurface().equals("(") && next.getSurface().equals(")")) {
				Token prev2 = ne.getFirstToken().getNAfter(-2);
				if(prev2 != null) {
					String surf = ne.getSurface();
					if(surf.matches(".*[A-Z]s") || prev2.getSurface().endsWith("s")) surf = surf.substring(0, surf.length()-1);
					List<String> featuresForAbbrev;
					if(abbrevFeatures.containsKey(surf)) {
						featuresForAbbrev = abbrevFeatures.get(surf);
					} else {
						featuresForAbbrev = new ArrayList<String>();
						abbrevFeatures.put(ne.getSurface(), featuresForAbbrev);
					}
					if(neByLastToken.containsKey(prev2)) {
						NamedEntity maybeAbbrev = neByLastToken.get(prev2);
						String abbrMode = "abbr1:";
						if(StringTools.testForAcronym(surf, maybeAbbrev.getSurface())) {
							abbrMode = "abbr2:";
						}
						if(surf.matches(".*\\s.*")) abbrMode += "wws:";
						for(double lthresh = -5.0;lthresh < 5.05;lthresh += 0.5) {
							double thresh = ProbabilityConvertor.logitToProb(lthresh);
							if(maybeAbbrev.getConfidence() > thresh) {
								featuresForAbbrev.add(abbrMode + "abbr>" + thresh);
							} else {
								featuresForAbbrev.add(abbrMode + "abbr<" + thresh);
							}							
						}
					} else {
						int tokID = ne.getFirstToken().getIndex();
						TokenSequence tokSeq = ne.getFirstToken().getTokenSequence();
						int length = surf.length();
						boolean isAcro = false;
						if(allCaps.matcher(surf).matches()) {
							if(length <= (tokID - 1)) {
								isAcro = true;
								for (int i = 0; i < length; i++) {
									if(!tokSeq.getToken(tokID - length - 1 + i).getSurface().toUpperCase().startsWith(surf.substring(i,i+1))) isAcro = false;
								}
								if(isAcro) {
									featuresForAbbrev.add("allUpperAbbrev");
								}
							}
						}
						if(!isAcro) {
							featuresForAbbrev.add("seenInBrackets");								
						}
					}
				}
			}
		}
	}
	
	public List<String> getFeatures(NamedEntity ne, Set<String> chemNameDictNames) {
		double conf = ne.getConfidence();
		double confLog = Math.log(conf) - Math.log(1 - conf);

		List<String> features = new ArrayList<String>();
		
		TokenSequence t = ne.getTokens().get(0).getTokenSequence();
		int entityLength = ne.getTokens().size();
		int startID = ne.getTokens().get(0).getIndex();
		int endID = startID + entityLength - 1;
		String surf = ne.getSurface();
		
		if(entityLength > 0 && chemNameDictNames.contains(surf)) {
			features.add("LongInCND");
		}
		
		//int filterCode = PostProcessor.filterEntity(surf, ne.getType(null));
		//features.add("filter=" + filterCode);
		
		/*if(startID > 0) {
			features.add("prev=" + t.getToken(startID - 1).getValue());
		}
		for(Token token : ne.getTokens()) {
			features.add("inside=" + token.getValue());
		}
		if(endID + 1 < t.size()) {
			features.add("next=" + t.getToken(endID + 1).getValue());
		}*/
		String surfForAbbrev = surf;
		if(abbrevFeatures.containsKey(surfForAbbrev)) {
			features.addAll(abbrevFeatures.get(surfForAbbrev));
		} else {
			features.add("noabbrev");
		}
		if(ne.isBlocked()) {
			//features.add("blocked");
		} else {
			//features.add("unblocked");
			if(averageScores.containsKey(surf)) {
				double avg = averageScores.get(surf);
				if(avg > conf) features.add("avg+");
				if(avg > conf+0.05) features.add("avg++");
				if(avg > conf+0.1) features.add("avg+++");
				if(avg < conf) features.add("avg-");
				if(avg < conf-0.05) features.add("avg--");
				if(avg < conf-0.1) features.add("avg---");
				double max = maxScores.get(surf);
				if(max - conf > 0.05) features.add("0.05below");
				if(max - conf > 0.1) features.add("0.1below");
				if(max - conf > 0.15) features.add("0.15below");
			}
		}
		
		for (double i = 0; i < Math.min(confLog,15.0); i+=0.05) {
			features.add("conf+");
		}
		for (double i = 0; i > Math.max(confLog,-15.0); i-=0.05) {
			features.add("conf-");
		}
		
		return features;
	}
	
}
