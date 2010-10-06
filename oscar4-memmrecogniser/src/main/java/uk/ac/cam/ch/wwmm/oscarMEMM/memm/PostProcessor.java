package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.chemnamedict.ChemNameDictSingleton;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ExtractTrainingData;
import uk.ac.cam.ch.wwmm.oscarMEMM.terms.TermSets;
import uk.ac.cam.ch.wwmm.oscarMEMM.tokenAnalysis.TokenTypes;

/**
 * Handles postprocessing of MEMM results.
 * 
 * @author ptc24
 * 
 */
final class PostProcessor {

	Map<NamedEntity, Double> entities;
	Set<NamedEntity> blocked;
	TokenSequence tokSeq;

	private static Pattern cjPattern = Pattern.compile("\\S+(ic|al|ous)");
	private static Pattern asePattern = Pattern.compile("\\S+[Aa]ses?");
	private static Pattern rnPattern = Pattern
			.compile(".*(tions?|ing|ed|ates?|ativ(e|e?ly)|"
					+ "ises?|izes?|ly[sz](is|e|ing|able)|lytic(a?ly)?|if(y|ies)|ments?|thes(is|es))");

	private static boolean noPC = false;

	public PostProcessor(TokenSequence tokSeq, Map<NamedEntity, Double> entities) {
		this.tokSeq = tokSeq;
		this.entities = entities;
	}

	public static int filterEntity(NamedEntity ne) {
		String surf = ne.getSurface();
		String type = ne.getType();
		return filterEntity(surf, type);
	}

	public static int filterEntity(String surf, String type) {
		//GetDataFromModels dataModel = new GetDataFromModels();

		surf = surf.replaceAll("\\s+", " ");
		if (!surf.matches(".*[a-zA-Z].*") && !type.equals("CPR")) {
			return 1;
		} else if (type.equals("ASE") && !asePattern.matcher(surf).matches()) {
			return 2;
		} else if (type.equals("CJ") && !cjPattern.matcher(surf).matches()) {
			return 3;
		} else if (type.equals("RN")
				&& !(rnPattern.matcher(surf).matches() || !surf
						.matches(".*[a-zA-Z].*"))) {
			return 4;
		} else if (type.equals("CPR")
				&& !surf.matches(".+[" + StringTools.hyphens + "]")) {
			return 5;
		} else if ((type.equals("CPR") || type.equals("CJ") || type
				.equals("ASE"))
				&& surf.matches(".+ .+")) {
			return 6;
		} else if (TermSets.getClosedClass().contains(surf)) {
			return 7;
		} else if (surf.endsWith(",") || surf.endsWith(".")) {
			return 8;
		} else if (surf
				.matches("(\\.|\\,|:|\\d+(\\.\\d+)?|=|at|is|has|with|are|on|of|and|or|were|in|as|was)\\s+.*")
				|| surf
						.matches(".*\\s+(\\.|\\,|:|=|at|is|has|with|are|on|of|and|or|were|in|as|was)")) {
			return 9;
		} else if (TokenTypes.oxidationStatePattern.matcher(surf).matches()) {
			return 10;
		} else if (!StringTools.bracketsAreBalanced(surf)
				&& surf.matches(".*\\s.*")) {
			return 11;
			// Fix things for alternate annotation scheme
		}  else if((type.length() < 4) && (!noPC &&
			 ExtractTrainingData.getInstance().nonChemicalWords.contains(surf)))
			 {
//		else if ((type.length() < 4)
//				&& (!noPC && dataModel.nonChemicalWords
//						.contains(surf))) {
			return 12;
		} else if (ChemNameDictSingleton.hasStopWord(surf)
				|| TermSets.getStopWords().contains(surf)) {
			return 13;
		}
		return 0;
	}

	public void filterEntities() {
		List<NamedEntity> neList = new ArrayList<NamedEntity>(entities.keySet());

		// Post-processing
		for (NamedEntity ne : neList) {
			if (filterEntity(ne) > 0)
				entities.remove(ne);
		}
	}

	public Set<NamedEntity> getBlocked() {
		if (blocked != null)
			return blocked;
		blocked = new HashSet<NamedEntity>();

		Set<Integer> occupied = new HashSet<Integer>();
		for (NamedEntity ne : getSorted()) {
			boolean isblocked = false;
			for (int i = ne.getTokens().get(0).getId(); i <= ne.getTokens()
					.get(ne.getTokens().size() - 1).getId(); i++) {
				if (occupied.contains(i)) {
					isblocked = true;
				}
			}
			if (isblocked) {
				blocked.add(ne);
				ne.setBlocked(true);
			} else {
				for (int i = ne.getTokens().get(0).getId(); i <= ne.getTokens()
						.get(ne.getTokens().size() - 1).getId(); i++) {
					occupied.add(i);
				}
			}
		}
		return blocked;
	}

	public void removeBlocked() {
		for (NamedEntity ne : getBlocked()) {
			if (OscarProperties.getInstance().verbose)
				System.out.println("Removing: " + ne);
			entities.remove(ne);
		}
	}

	private List<NamedEntity> getSorted() {
		List<NamedEntity> sorted = new ArrayList<NamedEntity>(entities.keySet());
		Collections.sort(sorted, Collections
				.reverseOrder(new Comparator<NamedEntity>() {
					// @SuppressWarnings("unchecked")
					public int compare(NamedEntity o1, NamedEntity o2) {
						return entities.get(o1).compareTo(entities.get(o2));
					}
				}));
		return sorted;
	}

	public Map<NamedEntity, Double> getEntities() {
		return entities;
	}

}
