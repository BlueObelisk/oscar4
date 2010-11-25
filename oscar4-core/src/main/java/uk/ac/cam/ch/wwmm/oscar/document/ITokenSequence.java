package uk.ac.cam.ch.wwmm.oscar.document;

import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ITokenSequence {

	/**Gets the string that was tokenised to make this TokenSequence.
	 * 
	 * @return The string that was tokenised to make this TokenSequence.
	 */
	public abstract String getSurface();

	/**Gets the ProcessingDocument (or null) that this TokenSequence was made
	 * from.
	 * 
	 * @return The ProcessingDocument (or null) that this TokenSequence was made
	 * from.
	 */
	public abstract IProcessingDocument getDoc();

	/**Gets the start offset of this TokenSequence. If this information was
	 * not supplied during tokenisation, this will be 0.
	 * 
	 * @return The start offset of this TokenSequence.
	 */
	public abstract int getOffset();

	/**Gets the list of tokens that comprise this TokenSequence.
	 * 
	 * @return The list of tokens that comprise this TokenSequence.
	 */
	public abstract List<IToken> getTokens();

	/**Gets a the sublist of tokens that occur between the given indices.
	 * 
	 * @param from The first token in the sublist (inclusive).
	 * @param to The last token in the sublist (inclusive).
	 * @return The sublist of tokens.
	 */
	public abstract List<IToken> getTokens(int from, int to);

	/**Gets a single token.
	 * 
	 * @param i The index of the token to get.
	 * @return The token.
	 */
	public abstract IToken getToken(int i);

	/**Gets the number of tokens in the TokenSequence.
	 * 
	 * @return the number of tokens in the TokenSequence.
	 */
	public abstract int size();

	/**Gets a list of strings corresponding to the tokens.
	 * 
	 * @return The list of strings corresponding to the tokens.
	 */
	public abstract List<String> getTokenStringList();

	/**Gets a substring of the source string that runs between two tokens 
	 * (inclusive).
	 * 
	 * @param startToken The first token (inclusive).
	 * @param endToken The last token (inclusive).
	 * @return The substring.
	 */
	public abstract String getSubstring(int startToken, int endToken);

	/**Gets a substring of the source string that runs between two offsets.
	 * 
	 * @param start The start offset.
	 * @param end The end offset.
	 * @return The substring.
	 */
	public abstract String getStringAtOffsets2(int start, int end);

	public abstract String getStringAtOffsets(int start, int end);

	/**Gets all of the token values of tokens that are hyphenated with 
	 * named entities. For example, this would get "based" in "acetone-based".
	 * 
	 * @return The token values.
	 */
	public abstract Set<String> getAfterHyphens();

	/**Gets all of the named entities in the TokenSequence. This produces a
	 * map, where the keys are the named entity types. The values are a list
	 * of all NEs of the corresponding type, which are represented as lists
	 * of strings.
	 * 
	 * @return The named entities.
	 */
	public abstract Map<NamedEntityType, List<List<String>>> getNes();

	/**Gets the string values of all of the non-NE tokens.
	 * 
	 * @return The string values.
	 */
	public abstract List<String> getNonNes();

	/**Converts the BIO-coding of the NE information to a BIOEW-coding.
	 * This alters the Token objects that comprise the token sequence.
	 * 
	 */
	public abstract void toBIOEW();

	/**Converts the BIO-coding of the NE information to a much richer tagset.
	 * This alters the Token objects that comprise the token sequence.
	 * 
	 */
	public abstract void toRichTags();

}