package uk.ac.cam.ch.wwmm.oscar.document;

import nu.xom.Element;

/**
 * @author egonw
 */
public interface ITokeniser {

	public TokenSequence tokenise(String text, IProcessingDocument procDoc, 
			int offset,	Element element);

}
