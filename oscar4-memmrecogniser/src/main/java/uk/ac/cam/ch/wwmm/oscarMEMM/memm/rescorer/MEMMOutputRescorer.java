package uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import opennlp.maxent.GISModel;
import opennlp.model.Event;



import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelReader;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelWriter;

/**Handles rescoring of MEMM output.
 * 
 * @author ptc24
 * @author egonw
 */
public final class MEMMOutputRescorer {

	Map<NamedEntityType,List<Event>> eventsByNamedEntityType;
	Map<NamedEntityType,GISModel> modelsByNamedEntityType;
	List<Double> goodProbsBefore;
	List<Double> goodProbsAfter;
	List<Double> badProbsBefore;
	List<Double> badProbsAfter;
	int totalRecall;
	
	int trainingCycles = 200;
	double grandTotalGain;
	
	static String experName = "rescoreHalf";
	
	/**Initialises an empty rescorer. This rescorer must be given data or a
	 * model for it to work.
	 */
	public MEMMOutputRescorer() {
		eventsByNamedEntityType = new HashMap<NamedEntityType,List<Event>>();
		grandTotalGain = 0.0;
		
		goodProbsBefore = new ArrayList<Double>();
		goodProbsAfter = new ArrayList<Double>();
		badProbsBefore = new ArrayList<Double>();
		badProbsAfter = new ArrayList<Double>();
		totalRecall = 0;
	}

	/**Adjust the confidence scores of a list of named entities.
	 * 
	 * @param entities The named entities to rescore.
	 *  
	 */
	public void rescore(List<NamedEntity> entities, Set<String> chemNameDictNames) {

		FeatureExtractor fe = new FeatureExtractor(entities);

		for(NamedEntity entity : entities) {
			List<String> features = fe.getFeatures(entity, chemNameDictNames);
			NamedEntityType namedEntityType = entity.getType();
			if(modelsByNamedEntityType.containsKey(namedEntityType)) {
				GISModel model = modelsByNamedEntityType.get(namedEntityType);
				if(model.getNumOutcomes() == 2) {
					double prob = model.eval(features.toArray(new String[0]))[model.getIndex("T")];
					entity.setConfidence(prob);

				}
			}
		}
	}

	/**Produce an XML Element that contains the trained rescorer model.
	 * 
	 * @return An XML Element that contains the trained rescorer model.
 	 * @throws IOException
	 */
	public Element writeElement() throws IOException {
		Element root = new Element("rescorer");
		for(NamedEntityType namedEntityType : modelsByNamedEntityType.keySet()) {
			Element maxent = new Element("maxent");
			maxent.addAttribute(new Attribute("type", namedEntityType.getName()));
			StringGISModelWriter sgmw = new StringGISModelWriter(modelsByNamedEntityType.get(namedEntityType));
			sgmw.persist();
			maxent.appendChild(sgmw.toString());
			root.appendChild(maxent);

		}
		return root;
	}
	
	/**Take an XML Element that contains a trained rescorer model, and prepare
	 * to use it.
	 * 
	 * @param elem The XML Element that contains the trained rescorer model.
	 * @throws IOException 
	 */
	public void readElement(Element elem) throws IOException  {
		Elements maxents = elem.getChildElements("maxent");
		modelsByNamedEntityType = new HashMap<NamedEntityType,GISModel>();
		for (int i = 0; i < maxents.size(); i++) {
			Element maxent = maxents.get(i);
			NamedEntityType namedEntityType = NamedEntityType.valueOf(maxent.getAttributeValue("type"));
			StringGISModelReader sgmr = new StringGISModelReader(maxent.getValue());
			GISModel gm = (GISModel) sgmr.getModel();
			modelsByNamedEntityType.put(namedEntityType, gm);
		}		
	}

}
