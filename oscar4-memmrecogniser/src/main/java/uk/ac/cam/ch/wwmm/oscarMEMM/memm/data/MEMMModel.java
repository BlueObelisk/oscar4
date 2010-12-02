package uk.ac.cam.ch.wwmm.oscarMEMM.memm.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import opennlp.maxent.GISModel;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMM;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelReader;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelWriter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.MEMMOutputRescorer;
import uk.ac.cam.ch.wwmm.oscarrecogniser.etd.ExtractedTrainingData;

/**
 * Data model for {@link MEMM}.
 *
 * @author ptc24
 * @author egonw
 */
public class MEMMModel {

    protected Map<String, Double> zeroProbs;
    protected Map<String, GISModel> gmByPrev;
    protected GISModel ubermodel;
    protected MEMMOutputRescorer rescorer;
    protected Set<String> tagSet;
    protected Set<NamedEntityType> namedEntityTypes;
    protected ExtractedTrainingData extractedTrainingData;

    public MEMMModel() {
        zeroProbs = new HashMap<String, Double>();
        gmByPrev = new HashMap<String, GISModel>();
        tagSet = new HashSet<String>();
        namedEntityTypes = new HashSet<NamedEntityType>();
        rescorer = null;
	}

    /**
     * Reads in an XML document containing a MEMM model.
     *
     * @param doc The XML document.
     * @throws Exception
     */
    public void readModel(Document doc) throws Exception {
        readModel(doc.getRootElement());
    }

    /**
     * Reads in a MEMM model from an XML element.
     *
     * @param memmRoot The XML element.
     * @throws Exception
     */
    public void readModel(Element modelRoot) throws Exception {
		Element memmElem = modelRoot.getFirstChildElement("memm");
        Elements maxents = memmElem.getChildElements("maxent");
        gmByPrev = new HashMap<String,GISModel>();
        tagSet = new HashSet<String>();
        for (int i = 0; i < maxents.size(); i++) {
            Element maxent = maxents.get(i);
            String prev = maxent.getAttributeValue("prev");
            StringGISModelReader sgmr = new StringGISModelReader(maxent.getValue());
            GISModel gm = sgmr.getModel();
            gmByPrev.put(prev, gm);
            tagSet.add(prev);
            for (int j = 0; j < gm.getNumOutcomes(); j++) {
                tagSet.add(gm.getOutcome(j));
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
			this.extractedTrainingData = ExtractedTrainingData.reinitialise(etdElem);
		} else {
            this.extractedTrainingData = null;
		}
        makeEntityTypesAndZeroProbs();
    }

    /**
     * Produces an XML element containing the current MEMM model.
     *
     * @return The XML element.
     * @throws Exception
     */
    public Element writeModel() throws Exception {
    	Element modelRoot = new Element("model");
    	// append the rescorer bits
    	if (extractedTrainingData != null)
    		modelRoot.appendChild(extractedTrainingData.toXML());
		// append the MEMM bits
        Element memmRoot = new Element("memm");
        for (String prev : gmByPrev.keySet()) {
            Element maxent = new Element("maxent");
            maxent.addAttribute(new Attribute("prev", prev));
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
        for (String tagType : tagSet) {
            if (tagType.startsWith("B-") || tagType.startsWith("W-")) {
                namedEntityTypes.add(NamedEntityType.valueOf(tagType.substring(2)));
            }
        }
        for(String tag : tagSet) {
            zeroProbs.put(tag, 0.0);
        }
    }

    public Set<String> getTagSet() {
        return tagSet;
    }

    public Set<NamedEntityType> getNamedEntityTypes() {
        return namedEntityTypes;
    }

    public Map<String, Double> getZeroProbs() {
    	return zeroProbs;
    }

	public GISModel getGISModelByPrev(String tag) {
		return gmByPrev.get(tag);
	}

	public Set<String> getGISModelPrevs() {
		return Collections.unmodifiableSet(gmByPrev.keySet());
	}

	public GISModel getUberModel() {
		return ubermodel;
	}

	public MEMMOutputRescorer getRescorer() {
		return rescorer;
	}

	public ExtractedTrainingData getExtractedTrainingData() {
		return extractedTrainingData;
	}
}
