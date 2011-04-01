package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;


public class ProcessingDocument implements IProcessingDocument {

	private List<TokenSequence> tokenSequences = new ArrayList<TokenSequence>();

	
	public List<TokenSequence> getTokenSequences() {
		return Collections.unmodifiableList(tokenSequences);
	}

	public void addTokenSequence(TokenSequence ts) {
		tokenSequences.add(ts);
	}

	
	/*
	 *  getStandoffTable, getTokensByStart and getTokensByEnd shouldn't be necessary
	 *  any more and shouldn't be called during processing.
	*/
	
	
	//getStandOffTable can be called by the tokeniser if Genia is running, but we're
	//dumping that functionality.
	@Deprecated
	public StandoffTable getStandoffTable() {
		throw new UnsupportedOperationException("shouldn't have been called");
	}

	
	/* 
	 * ok, so getTokensByStart is called by the tokeniser, but the return value of null
	 * means that the map doesn't get populated. This isn't ideal.
	 */
	@Deprecated
	public Map<Integer,Token> getTokensByStart() {
		return null;
	}

	@Deprecated
	public Map<Integer,Token> getTokensByEnd() {
		throw new UnsupportedOperationException("shouldn't have been called");
	}
	
}
