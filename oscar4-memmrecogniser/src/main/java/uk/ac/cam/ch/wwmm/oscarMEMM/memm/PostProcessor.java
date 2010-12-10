package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.terms.TermSets;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarrecogniser.manualAnnotations.ManualAnnotations;

/**
 * Handles postprocessing of MEMM results.
 * 
 * @author ptc24
 * 
 */
final class PostProcessor {

	private Map<NamedEntity, Double> entities;
	private Set<NamedEntity> blocked;
	private ITokenSequence tokSeq;

	private static Pattern cjPattern = Pattern.compile("\\S+(ic|al|ous)");
	private static Pattern asePattern = Pattern.compile("\\S+[Aa]ses?");
	private static Pattern rnPattern = Pattern
			.compile(".*(tions?|ing|ed|ates?|ativ(e|e?ly)|"
					+ "ises?|izes?|ly[sz](is|e|ing|able)|lytic(a?ly)?|if(y|ies)|ments?|thes(is|es))");

    private static Pattern oxidationStatePattern = Pattern.compile("\\((o|i{1,4}|i{0,3}[xv]|[xv]i{0,4})\\)", Pattern.CASE_INSENSITIVE);

	private static boolean noPC = false;

	public PostProcessor(ITokenSequence tokSeq, Map<NamedEntity, Double> entities) {
		this.tokSeq = tokSeq;
		this.entities = entities;
	}

	public static int filterEntity(NamedEntity ne) {
		String surf = ne.getSurface();
		NamedEntityType namedEntityType = ne.getType();
		return filterEntity(surf, namedEntityType);
	}

	public static int filterEntity(String surf, NamedEntityType namedEntityType) {
		//GetDataFromModels dataModel = new GetDataFromModels();

		surf = surf.replaceAll("\\s+", " ");
		if (!surf.matches(".*[a-zA-Z].*") && !NamedEntityType.LOCANTPREFIX.isInstance(namedEntityType)) {
			return 1;
		} else if (NamedEntityType.ASE.isInstance(namedEntityType) && !asePattern.matcher(surf).matches()) {
			return 2;
		} else if (NamedEntityType.ADJECTIVE.isInstance(namedEntityType) && !cjPattern.matcher(surf).matches()) {
			return 3;
		} else if (NamedEntityType.REACTION.isInstance(namedEntityType)
				&& !(rnPattern.matcher(surf).matches() || !surf
						.matches(".*[a-zA-Z].*"))) {
			return 4;
		} else if (NamedEntityType.LOCANTPREFIX.isInstance(namedEntityType)
				&& !surf.matches(".+[" + StringTools.hyphens + "]")) {
			return 5;
		} else if ((NamedEntityType.LOCANTPREFIX.isInstance(namedEntityType)
                || NamedEntityType.ADJECTIVE.isInstance(namedEntityType)
                || NamedEntityType.ASE.isInstance(namedEntityType))
				&& surf.matches(".+ .+")) {
			return 6;
		} else if (TermSets.getDefaultInstance().getClosedClass().contains(surf)) {
			return 7;
		} else if (surf.endsWith(",") || surf.endsWith(".")) {
			return 8;
		} else if (surf
				.matches("(\\.|\\,|:|\\d+(\\.\\d+)?|=|at|is|has|with|are|on|of|and|or|were|in|as|was)\\s+.*")
				|| surf
						.matches(".*\\s+(\\.|\\,|:|=|at|is|has|with|are|on|of|and|or|were|in|as|was)")) {
			return 9;
		} else if (oxidationStatePattern.matcher(surf).matches()) {
			return 10;
		} else if (!StringTools.bracketsAreBalanced(surf)
				&& surf.matches(".*\\s.*")) {
			return 11;
			// Fix things for alternate annotation scheme
		}  else if ((namedEntityType.getName().length() < 4) && (!noPC &&
			 ManualAnnotations.getInstance().nonChemicalWords.contains(surf)))
			 {
//		else if ((type.length() < 4)
//				&& (!noPC && dataModel.nonChemicalWords
//						.contains(surf))) {
			return 12;
		} else if (TermSets.getDefaultInstance().getStopWords().contains(surf)) {
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
			Logger.getLogger(PostProcessor.class).debug("Removing: " + ne);
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
