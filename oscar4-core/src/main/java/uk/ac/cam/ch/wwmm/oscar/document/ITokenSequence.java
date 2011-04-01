package uk.ac.cam.ch.wwmm.oscar.document;

import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ITokenSequence {

	
	public abstract String getSurface();


	public abstract IProcessingDocument getDoc();


	public abstract int getOffset();


	public abstract List<IToken> getTokens();


	public abstract List<IToken> getTokens(int from, int to);


	public abstract IToken getToken(int i);


	public abstract int getSize();


	public abstract List<String> getTokenStringList();


	public abstract String getSubstring(int startToken, int endToken);

	public abstract String getStringAtOffsets(int start, int end);


	public abstract Set<String> getAfterHyphens();


	public abstract Map<NamedEntityType, List<List<String>>> getNes();


	public abstract List<String> getNonNes();


	public abstract IToken getTokenByEndIndex(int index);


	public abstract IToken getTokenByStartIndex(int index);


}