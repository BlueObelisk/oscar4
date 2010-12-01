package uk.ac.cam.ch.wwmm.oscar.document;

import nu.xom.Element;

/**
 * @author egonw
 */
public interface ITokeniser {

	public ITokenSequence tokenise(String text, IProcessingDocument procDoc, 
			int offset,	Element element);

}
