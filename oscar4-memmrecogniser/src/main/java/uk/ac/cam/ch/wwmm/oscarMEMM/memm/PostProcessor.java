package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.terms.TermSets;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;

/**
 * Handles postprocessing of MEMM results.
 * 
 * @author ptc24
 * 
 */
final class PostProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(PostProcessor.class);
	
	private List<NamedEntity> entities;
	private Set<NamedEntity> blocked;
	private TokenSequence tokSeq;
	private ExtractedTrainingData annotations;

	private static Pattern cjPattern = Pattern.compile("\\S+(ic|al|ous)");
	private static Pattern asePattern = Pattern.compile("\\S+[Aa]ses?");
	private static Pattern rnPattern = Pattern
			.compile(".*(tions?|ing|ed|ates?|ativ(e|e?ly)|"
					+ "ises?|izes?|ly[sz](is|e|ing|able)|lytic(a?ly)?|if(y|ies)|ments?|thes(is|es))");

    private static Pattern oxidationStatePattern = Pattern.compile("\\((o|i{1,4}|i{0,3}[xv]|[xv]i{0,4})\\)", Pattern.CASE_INSENSITIVE);

	private static boolean noPC = false;

	public PostProcessor(TokenSequence tokSeq, List<NamedEntity> entities, ExtractedTrainingData annotations) {
		this.tokSeq = tokSeq;
		this.entities = entities;
		this.annotations = annotations;
	}

	public int filterEntity(NamedEntity ne) {
		String surf = ne.getSurface();
		NamedEntityType namedEntityType = ne.getType();
		return filterEntity(surf, namedEntityType);
	}

	public int filterEntity(String surf, NamedEntityType namedEntityType) {
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
			 annotations.getNonChemicalWords().contains(surf)))
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
		List<NamedEntity> neList = new ArrayList<NamedEntity>(entities);

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
			for (int i = ne.getTokens().get(0).getIndex(); i <= ne.getTokens()
					.get(ne.getTokens().size() - 1).getIndex(); i++) {
				if (occupied.contains(i)) {
					isblocked = true;
				}
			}
			if (isblocked) {
				blocked.add(ne);
				ne.setBlocked(true);
			} else {
				for (int i = ne.getTokens().get(0).getIndex(); i <= ne.getTokens()
						.get(ne.getTokens().size() - 1).getIndex(); i++) {
					occupied.add(i);
				}
			}
		}
		return blocked;
	}

	public void removeBlocked() {
		for (NamedEntity ne : getBlocked()) {
			LOG.debug("Removing: " + ne);
			entities.remove(ne);
		}
	}

	private List<NamedEntity> getSorted() {
		List<NamedEntity> sorted = new ArrayList<NamedEntity>(entities);
		Collections.sort(sorted, Collections
				.reverseOrder(new Comparator<NamedEntity>() {
					// @SuppressWarnings("unchecked")
					public int compare(NamedEntity o1, NamedEntity o2) {
                        return Double.compare(o1.getConfidence(), o2.getConfidence());
					}
				}));
		return sorted;
	}

	public List<NamedEntity> getEntities() {
		return entities;
	}

}
