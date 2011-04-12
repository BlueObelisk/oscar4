package uk.ac.cam.ch.wwmm.oscardata;

import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sam Adams
 */
public class OscarData {

    private Tokeniser tokeniser;
    private RParser rParser;

    public List<DataAnnotation> findData(String s) {
        Tokeniser tokeniser = getTokeniser();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, s);

        List<DataAnnotation> annotations = new ArrayList<DataAnnotation>();
		for (TokenSequence tokSeq : procDoc.getTokenSequences()) {
			annotations.addAll(getRParser().findData(tokSeq));
		}

		return annotations;
    }

    private synchronized Tokeniser getTokeniser() {
        if (tokeniser == null) {
            tokeniser = Tokeniser.getDefaultInstance();
        }
        return tokeniser;
    }

    private synchronized RParser getRParser() {
        if (rParser == null) {
            rParser = RParser.getInstance();
        }
        return rParser;
    }

}
