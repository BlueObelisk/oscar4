package uk.ac.cam.ch.wwmm.oscarMEMM.memm.data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.set.UnmodifiableSet;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import opennlp.maxent.GISModel;
import opennlp.maxent.MaxentModel;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarException;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.types.BioTag;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMM;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelReader;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelWriter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.MEMMOutputRescorer;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGram;

/**
 * Data model for {@link MEMM}.
 *
 * @author ptc24
 * @author egonw
 */
public class MEMMModel {

    protected Map<BioType, Double> zeroProbs;
    protected Map<BioType, GISModel> gmByPrev;
    protected GISModel ubermodel;
    protected MEMMOutputRescorer rescorer;
    protected Set<BioType> tagSet;
    protected Set<NamedEntityType> namedEntityTypes;
    protected ExtractedTrainingData etd;
    protected NGram nGram;
    protected UnmodifiableSet chemNameDictNames;
    

    public MEMMModel() {
        zeroProbs = new HashMap<BioType, Double>();
        gmByPrev = new HashMap<BioType, GISModel>();
        tagSet = new HashSet<BioType>();
        namedEntityTypes = new HashSet<NamedEntityType>();
        rescorer = null;
	}

    public MEMMModel(Element trainedModel) {
		this();
		try {
			readModel(trainedModel);
		} catch (IOException e) {
			throw new OscarInitialisationException("failed to load MEMM model", e);
		}
	}

	/**
     * Reads in an XML document containing a MEMM model.
     *
     * @param doc The XML document.
     * @throws IOException
     */
    protected void readModel(Document doc) throws IOException {
        readModel(doc.getRootElement());
    }

    /**
     * Reads in a MEMM model from an XML element.
     *
     * @param modelRoot The XML element.
     * @throws IOException
     */
    protected void readModel(Element modelRoot) throws IOException {
		Element memmElem = modelRoot.getFirstChildElement("memm");
        Elements maxents = memmElem.getChildElements("maxent");
        gmByPrev = new HashMap<BioType,GISModel>();
        tagSet = new HashSet<BioType>();
        for (int i = 0; i < maxents.size(); i++) {
            Element maxent = maxents.get(i);
            BioType prev = BioType.fromString(maxent.getAttributeValue("prev"));
            StringGISModelReader sgmr = new StringGISModelReader(maxent.getValue());
            GISModel gm = sgmr.getModel();
            gmByPrev.put(prev, gm);
            tagSet.add(prev);
            for (int j = 0; j < gm.getNumOutcomes(); j++) {
                tagSet.add(BioType.fromString(gm.getOutcome(j)));
            }
        }
        
        Element rescorerElem = memmElem.getFirstChildElement("rescorer");
        if(rescorerElem != null) {
            rescorer = new MEMMOutputRescorer();
            rescorer.readElement(rescorerElem);
        } else {
        	
            rescorer = null;
        }
        Element etdElem = modelRoot.getFirstChildElement("etd");
		if (etdElem != null) {
			this.etd = new ExtractedTrainingData(etdElem);
		} else {
            this.etd = null;
		}
        makeEntityTypesAndZeroProbs();
    }

    /**
     * Produces an XML element containing the current MEMM model.
     *
     * @return The XML element.
     * @throws IOException
     */
    public Element writeModel() throws IOException {
    	Element modelRoot = new Element("model");
    	// append the rescorer bits
    	if (etd != null)
    		modelRoot.appendChild(etd.toXML());
		// append the MEMM bits
        Element memmRoot = new Element("memm");
        for (BioType prev : gmByPrev.keySet()) {
            Element maxent = new Element("maxent");
            maxent.addAttribute(new Attribute("prev", prev.toString()));
            StringGISModelWriter sgmw = new StringGISModelWriter(gmByPrev.get(prev));
            sgmw.persist();
            maxent.appendChild(sgmw.toString());
            memmRoot.appendChild(maxent);
        }
        if(rescorer != null) {
            memmRoot.appendChild(rescorer.writeElement());
        }
        if (memmRoot.getChildCount() != 0)
        	modelRoot.appendChild(memmRoot);
//		NESubtypes subtypes = NESubtypes.getInstance();
//		if(subtypes.OK) modelRoot.appendChild(subtypes.toXML());
        return modelRoot;
    }

    protected void makeEntityTypesAndZeroProbs() {
        namedEntityTypes = new HashSet<NamedEntityType>();
        for (BioType tagType : tagSet) {
            if (tagType.getBio() == BioTag.B) {
                namedEntityTypes.add(tagType.getType());
            }
        }
        for(BioType tag : tagSet) {
            zeroProbs.put(tag, 0.0);
        }
    }

    public Set<BioType> getTagSet() {
        return Collections.unmodifiableSet(tagSet);
    }

    public Set<NamedEntityType> getNamedEntityTypes() {
        return Collections.unmodifiableSet(namedEntityTypes);
    }

    public Map<BioType, Double> getZeroProbs() {
    	return Collections.unmodifiableMap(zeroProbs);
    }

	public MaxentModel getMaxentModelByPrev(BioType tag) {
		return gmByPrev.get(tag);
	}

	public Set<BioType> getGISModelPrevs() {
		return Collections.unmodifiableSet(gmByPrev.keySet());
	}

	public GISModel getUberModel() {
		return ubermodel;
	}

	public MEMMOutputRescorer getRescorer() {
		return rescorer;
	}

	public ExtractedTrainingData getExtractedTrainingData() {
		return etd;
	}
	
	public NGram getNGram() {
		return nGram;
	}

	public UnmodifiableSet getChemNameDictNames() {
		return chemNameDictNames;
	}
}
