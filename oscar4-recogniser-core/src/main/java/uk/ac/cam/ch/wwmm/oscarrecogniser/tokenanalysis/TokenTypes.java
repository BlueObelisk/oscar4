package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/**
 * @author Peter Corbett
 * @author ojd20 Tightened some of the performance.
 * 
 */
public class TokenTypes {
	
	private static final String ESS = "s";
	private static final String BIPHENYL = "biphenyl";
	private static final String IC = "ic";
	private static final String ARSENICS = "arsenics";
	private static final String ARSENIC = "arsenic";
	private static final String NUCLEOBASES = "nucleobases";
	private static final String NUCLEOBASE = "nucleobase";

	private static final Pattern ASES = Pattern.compile(".*ases?");
	private static final Pattern GROUPS = Pattern.compile(".*(yl|o|oxy)\\)?-?");
	private static final Pattern REACTION1 = Pattern
			.compile(".*at(ed|ions?|ing)");
	private static final Pattern REACTION2 = Pattern
			.compile(".*i[sz](ed|ations?|ing)");
	private static final Pattern REACTION3 = Pattern
			.compile(".*(lys(is|es|ed?|ing|tic))");

	public static Pattern twoLowerPattern = Pattern.compile("[a-z][a-z]");
	public static Pattern oneCapitalPattern = Pattern.compile("[A-Z]");
	public static Pattern oxidationStatePattern = Pattern.compile("\\((o|i{1,4}|i{0,3}[xv]|[xv]i{0,4})\\)", Pattern.CASE_INSENSITIVE);
	public static Pattern oxidationStateEndPattern = Pattern.compile(".*\\((o|i{1,4}|i{0,3}[xv]|[xv]i{0,4})\\)", Pattern.CASE_INSENSITIVE);

	
	/*
	 * SciXML dependent
	 */
	@Deprecated
	public static boolean isCompRef(IToken token) {
		boolean isCr = false;
		if(token.getDoc() != null && XMLStrings.getInstance().isCompoundReferenceUnderStyle(token.getDoc().getStandoffTable().getElemAtOffset(token.getStart()))) {
			isCr = true;
			for(int i=0;i<token.getValue().length();i++) {
				if(!XMLStrings.getInstance().isCompoundReferenceUnderStyle(token.getDoc().getStandoffTable().getElemAtOffset(token.getStart()))) {
					isCr = false;
					break;
				}
			}
		}
		return isCr;
	}
	
	/*
	 * SciXML dependent
	 */
	@Deprecated
	public static boolean isRef(IToken token) {
		if(token.getDoc() == null) return false;
		return XMLStrings.getInstance().isCitationReferenceUnderStyle(token.getDoc().getStandoffTable().getElemAtOffset(token.getStart()));
	}


	public static NamedEntityType getTypeForSuffix(String s) {
		if (s.endsWith(NUCLEOBASE)) {
			return NamedEntityType.COMPOUND;
		}
		if (s.endsWith(NUCLEOBASES)) {
			return NamedEntityType.COMPOUNDS;
		}
		if (ASES.matcher(s).matches()) {
			return NamedEntityType.ASE;
		}
		if (s.endsWith(ARSENIC)) {
			return NamedEntityType.COMPOUND;
		}
		if (s.endsWith(ARSENICS)) {
			return NamedEntityType.COMPOUNDS;
		}
		if (s.endsWith(IC)) {
			return NamedEntityType.ADJECTIVE;
		}
		if (s.endsWith(BIPHENYL)) {
			return NamedEntityType.COMPOUND;
		}
		if (GROUPS.matcher(s).matches()) {
			return NamedEntityType.GROUP;
		}
		if (REACTION1.matcher(s).matches()) {
			return NamedEntityType.REACTION;
		}
		if (REACTION2.matcher(s).matches()) {
			return NamedEntityType.REACTION;
		}
		if (REACTION3.matcher(s).matches()) {
			return NamedEntityType.REACTION;
		}
		if (s.endsWith(ESS)) {
			return NamedEntityType.COMPOUNDS;
		}
		return NamedEntityType.COMPOUND;
	}

}
