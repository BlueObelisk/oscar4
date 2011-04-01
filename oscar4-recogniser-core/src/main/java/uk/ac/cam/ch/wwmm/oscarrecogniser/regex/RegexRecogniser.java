package uk.ac.cam.ch.wwmm.oscarrecogniser.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarrecogniser.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver.ResolutionMode;


/**
 * 
 * A class to recognise named entities that match a given regular expression.
 * Intended to be used to recognise serial numbers etc.
 * 
 * @author dmj30
 *
 */
public class RegexRecogniser implements ChemicalEntityRecogniser {

	private Pattern pattern;
	private NamedEntityType neType = NamedEntityType.COMPOUND;
	
	public RegexRecogniser(String regex) {
		if ("".equals(regex)) {
			throw new IllegalArgumentException("regex must not be empty");
		}
		pattern = Pattern.compile(regex);
	}

	
	public List<NamedEntity> findNamedEntities(List<TokenSequence> tokenSequences) {
		return findNamedEntities(tokenSequences, ResolutionMode.REMOVE_BLOCKED);
	}
	
	public List<NamedEntity> findNamedEntities(List<TokenSequence> tokenSequences, ResolutionMode resolutionMode) {
		
		List <NamedEntity> nes = new ArrayList<NamedEntity>();
		for (TokenSequence tokSeq : tokenSequences) {
			Matcher matcher = pattern.matcher(tokSeq.getSurface());
			while(matcher.find()) {
				Token startToken = tokSeq.getTokenByStartIndex(matcher.start());
				Token endToken = tokSeq.getTokenByEndIndex(matcher.end());
				if (startToken != null && endToken != null) {
					List <Token> tokens = new ArrayList<Token>(tokSeq.getTokens());
					Token token;
					while ((token = tokens.get(0)) != startToken) {
						tokens.remove(token);
					}
					while ((token = tokens.get(tokens.size()-1)) != endToken) {
						tokens.remove(token);
					}
					NamedEntity ne = new NamedEntity(tokens, matcher.group(), neType);
					nes.add(ne);
				}
			}
		}
		
		if (resolutionMode == ResolutionMode.REMOVE_BLOCKED) {
			StandoffResolver.resolveStandoffs(nes);
		}
		else if (resolutionMode == ResolutionMode.MARK_BLOCKED) {
			StandoffResolver.markBlockedStandoffs(nes);
		}
		else {
			throw new RuntimeException(resolutionMode + " not yet implemented");
		}
		
		return nes;
	}

	
	/**
	 * @return the pattern matched by the RegexRecogniser
	 */
	public Pattern getPattern() {
		return pattern;
	}


	/**
	 * Sets the NamedEntityType to be applied to named entities identified
	 * by the RegexRecogniser.
	 * @param neType
	 */
	public void setNamedEntityType(NamedEntityType neType) {
		this.neType  = neType;
	}

}
