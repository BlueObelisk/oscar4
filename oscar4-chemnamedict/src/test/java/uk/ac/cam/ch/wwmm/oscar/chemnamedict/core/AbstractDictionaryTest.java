package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict;

public abstract class AbstractDictionaryTest {

	public abstract IChemNameDict getDictionary() throws Exception;

	@Test
	public void testURI() throws Exception {
		IChemNameDict dict = getDictionary();
		assertNotNull(dict.getURI());
	}

}
