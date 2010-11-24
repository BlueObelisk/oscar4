package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.tools.IStandoffTable;

public interface IProcessingDocument {


	List <TokenSequence> getTokenSequences();

	IStandoffTable getStandoffTable();

	Map<Integer, Token> getTokensByStart();

	Map<Integer, Token> getTokensByEnd();

}
