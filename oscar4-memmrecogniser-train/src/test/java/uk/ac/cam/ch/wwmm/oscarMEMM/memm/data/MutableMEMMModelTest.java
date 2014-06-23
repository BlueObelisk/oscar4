package uk.ac.cam.ch.wwmm.oscarMEMM.memm.data;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Locale;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.MutableChemNameDict;

public class MutableMEMMModelTest {

    @Test
	public void testConstructor() {
		MutableMEMMModel model = new MutableMEMMModel(
				Collections.unmodifiableSet(Collections.<String> emptySet()));
		assertNotNull(model.getZeroProbs());
		assertNull(model.getRescorer());
		assertNotNull(model.getTagSet());
		assertNotNull(model.getNamedEntityTypes());
		assertNull(model.getExtractedTrainingData());
		assertNotNull(model.getGISModelPrevs());
		assertNotNull(model.getNGram());
		assertNotNull(model.getChemNameDictNames());
	}
    
    @Ignore
    // haven't quite worked out the desired functionality here
    @Test
    public void testNgramCustomisation() throws URISyntaxException {
        MutableMEMMModel vanilla = new MutableMEMMModel(
				Collections.unmodifiableSet(ChemNameDictRegistry
						.getDefaultInstance().getAllNames()));
    	
    	ChemNameDictRegistry customRegistry = new ChemNameDictRegistry();
    	MutableChemNameDict dict = new MutableChemNameDict(new URI("http://www.example.com"), Locale.ENGLISH);
    	dict.addName("asparagus");
    	dict.addName("carrot");
    	dict.addName("cauliflower");
    	dict.addName("potato");
    	customRegistry.register(dict);
    	
    	MutableMEMMModel customised = new MutableMEMMModel(Collections.unmodifiableSet(customRegistry.getAllNames()));
    	
    	assertFalse(customised.getNGram().compareTo(vanilla.getNGram()));
    }
}
