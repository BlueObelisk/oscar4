package uk.ac.cam.ch.wwmm.oscardata;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;

/**A top-level class to run the RParser over a SciXML document.
 * 
 * @author ptc24
 *
 */
public final class DataParser {

	private Document doc;
	
	/**This puts experimental data markup on a document.
	 * 
	 * @param doc The SciXML document to parse. This will be modified if
	 * experimental data is found.
	 * @param dataOnlyInExperimental Whether to limit the locations in which data is
	 * to be annotated to experimental sections
	 */
	public static void dataParse(Document doc, boolean dataOnlyInExperimental) {
		DataParser dp = new DataParser(doc);
		dp.scrubFormatting();		
		Nodes paras;
		if(dataOnlyInExperimental) {
			paras = doc.query(XMLStrings.getInstance().EXPERIMENTAL_PARAS_XPATH, XMLStrings.getInstance().getXpc());
		} else {
			paras = doc.query(XMLStrings.getInstance().ALL_PARAS_XPATH, XMLStrings.getInstance().getXpc());
		}
		
		for (int i = 0; i < paras.size(); i++) {
			for (int j = 0; j < paras.get(i).getChildCount(); j++) {
				Node n = paras.get(i).getChild(j);
				if(n instanceof Text)
					RParser.getInstance().parse((Text) n);
			}
		}		
	}
	
	/**This puts experimental data markup on a document. Locations in which data will
	 * be annotated are not limited to experimental sections.
	 * 
	 * @param doc The SciXML document to parse. This will be modified if
	 * experimental data is found.
	 */
	public static void dataParse(Document doc) {
		dataParse(doc, false);
	}
	
	private DataParser(Document doc) {
		this.doc = doc;
	}
	
	private void scrubFormatting() {
		Nodes nodes = doc.query(XMLStrings.getInstance().FORMATTING_XPATH, XMLStrings.getInstance().getXpc());
		for (int i = 0; i < nodes.size(); i++) {
			XOMTools.removeElementPreservingText((Element)nodes.get(i));
		}
	}

	public static List<DataAnnotation> findData(ProcessingDocument procDoc) {
		List <DataAnnotation> annotations = new ArrayList<DataAnnotation>();
		for (TokenSequence tokSeq : procDoc.getTokenSequences()) {
			annotations.addAll(RParser.getInstance().findData(tokSeq));
		}
		return annotations;
	}

}
