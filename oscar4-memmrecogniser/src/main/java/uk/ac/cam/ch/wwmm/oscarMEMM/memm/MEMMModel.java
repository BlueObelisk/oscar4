package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

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
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelReader;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelWriter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.MEMMOutputRescorer;

/**
 * Data model for {@link MEMM}.
 *
 * @author ptc24
 * @author egonw
 */
public class MEMMModel {

    private Map<String, Double> zeroProbs;
    private Map<String, GISModel> gmByPrev;
    private GISModel ubermodel;
    private MEMMOutputRescorer rescorer;
    private Set<String> tagSet;
    private Set<NamedEntityType> namedEntityTypes;

    public MEMMModel() {
        zeroProbs = new HashMap<String, Double>();
        gmByPrev = new HashMap<String, GISModel>();
        tagSet = new HashSet<String>();
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
    public void readModel(Element memmRoot) throws Exception {
        Elements maxents = memmRoot.getChildElements("maxent");
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
        Element rescorerElem = memmRoot.getFirstChildElement("rescorer");
        if(rescorerElem != null) {
            rescorer = new MEMMOutputRescorer();
            rescorer.readElement(rescorerElem);
        } else {
            rescorer = null;
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
        Element root = new Element("memm");
        for (String prev : gmByPrev.keySet()) {
            Element maxent = new Element("maxent");
            maxent.addAttribute(new Attribute("prev", prev));
            StringGISModelWriter sgmw = new StringGISModelWriter(gmByPrev.get(prev));
            sgmw.persist();
            maxent.appendChild(sgmw.toString());
            root.appendChild(maxent);
        }
        if(rescorer != null) {
            root.appendChild(rescorer.writeElement());
        }
        return root;
    }

    private void makeEntityTypesAndZeroProbs() {
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

    Set<String> getTagSet() {
        return tagSet;
    }

    Set<NamedEntityType> getNamedEntityTypes() {
        return namedEntityTypes;
    }

    Map<String, Double> getZeroProbs() {
    	return zeroProbs;
    }

	public GISModel getGISModelByPrev(String tag) {
		return gmByPrev.get(tag);
	}

	public GISModel getUberModel() {
		return ubermodel;
	}

	public MEMMOutputRescorer getRescorer() {
		return rescorer;
	}
}
