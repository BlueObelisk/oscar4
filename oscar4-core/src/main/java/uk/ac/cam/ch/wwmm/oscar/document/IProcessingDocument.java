package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;

public interface IProcessingDocument {


	List <TokenSequence> getTokenSequences();

	StandoffTable getStandoffTable();

	Map<Integer, Token> getTokensByStart();

	Map<Integer, Token> getTokensByEnd();

}
