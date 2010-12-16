package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscarrecogniser.manualAnnotations.ManualAnnotations;

public class NGramBuilderTest {

	@Test
	public void testBuildModels() {
		NGram vanillaNGram1 = NGramBuilder.buildModel();
		NGram vanillaNGram2 = NGramBuilder.buildModel();
		
		ManualAnnotations etd = ManualAnnotations.loadManualAnnotations(OscarProperties.getData().model);
		NGram customisedNGram = NGramBuilder.buildModel(etd);
		
		short [] data1 = vanillaNGram1.getData();
		short [] data2 = vanillaNGram2.getData();
		short [] data3 = customisedNGram.getData();
		
		assertEquals(data1.length, data2.length);
		assertEquals(data1.length, data3.length);
		
		boolean identicalData3 = true;
		for (int i = 0; i < data1.length; i++) {
			assertEquals(data1[i], data2[i]);
			if (data1[i] != data3[i]) {
				identicalData3 = false;
			}
		}
		assertFalse(identicalData3);
	}
}
