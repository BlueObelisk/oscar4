package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.tools.IStandoffTable;


public class ProcessingDocument implements IProcessingDocument {

	private List<TokenSequence> tokenSequences = new ArrayList<TokenSequence>();

	
	public List<TokenSequence> getTokenSequences() {
		return Collections.unmodifiableList(tokenSequences);
	}

	public void addTokenSequence(TokenSequence ts) {
		tokenSequences.add(ts);
	}

	@Deprecated
	public IStandoffTable getStandoffTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public Map<Integer, Token> getTokensByStart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public Map<Integer, Token> getTokensByEnd() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
