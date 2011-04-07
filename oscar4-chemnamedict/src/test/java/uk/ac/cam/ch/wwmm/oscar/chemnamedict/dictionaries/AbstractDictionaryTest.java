package uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IChemNameDict;


public abstract class AbstractDictionaryTest {

	public abstract IChemNameDict getDictionary() throws Exception;

	@Test
	public void testURI() throws Exception {
		IChemNameDict dict = getDictionary();
		assertNotNull(dict.getURI());
	}

}
