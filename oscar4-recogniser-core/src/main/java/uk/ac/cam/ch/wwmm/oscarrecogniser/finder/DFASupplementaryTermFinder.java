package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * A class to permit the identification of a small number of additional
 * literal terms. The performance for a large number of terms is currently poor.
 * 
 * @author dmj30
 *
 */
public class DFASupplementaryTermFinder extends DFAFinder {
	 
	private ListMultimap<NamedEntityType, String> supplementaryTerms;
	
	/**
	 * Creates a new DFASupplementaryTermFinder to identify the given terms.
	 * 
	 * @param supplementaryTerms a {@link ListMultimap} of
	 * {@link NamedEntityType} to the corresponding literal
	 * terms of that type.
	 */
	public DFASupplementaryTermFinder(ListMultimap<NamedEntityType,String> supplementaryTerms) {
		this.supplementaryTerms = supplementaryTerms;
		super.init();
	}

	
	/**
	 * Convenience constructor - all names in the supplied
	 * {@link ChemNameDictRegistry} are added as supplementary terms
	 * of type {@link NamedEntityType#COMPOUND}
	 * 
	 * @param registry
	 */
	public DFASupplementaryTermFinder(ChemNameDictRegistry registry) {
		this.supplementaryTerms = ArrayListMultimap.create();
		for (String name : registry.getAllNames()) {
			supplementaryTerms.put(NamedEntityType.COMPOUND, name);
		}
		super.init();
		
	}


	ListMultimap<NamedEntityType, String> getTerms() {
		return supplementaryTerms;
	}
	
	@Override
	protected void loadTerms() {
		for(NamedEntityType type : supplementaryTerms.keys()){
			for (String name : supplementaryTerms.get(type)) {
				addNamedEntity(name, type, true);	
			}
		}
	}

	public List<NamedEntity> findNamedEntities(TokenSequence tokenSequence) {
		NECollector nec = new NECollector();
		List<RepresentationList> repsList = generateTokenRepresentations(tokenSequence);
		findItems(tokenSequence, repsList, nec);
		return nec.getNes();
	}
	
	private List<RepresentationList> generateTokenRepresentations(TokenSequence tokenSequence) {
		List<RepresentationList> repsList = new ArrayList<RepresentationList>();
		for(Token token : tokenSequence.getTokens()) {
			repsList.add(generateTokenRepresentations(token));
		}
		return repsList;
	}
	
	protected RepresentationList generateTokenRepresentations(Token token) {
		RepresentationList representations = new RepresentationList();
		String tokenValue = token.getSurface();
		representations.addRepresentation(tokenValue);
		String normalisedValue = StringTools.normaliseName(tokenValue);
		if (!normalisedValue.equals(tokenValue)) {
            representations.addRepresentation(normalisedValue);
        }
		representations.addRepresentations(getSubReRepsForToken(tokenValue));
		return representations;
	}

	
	

}
