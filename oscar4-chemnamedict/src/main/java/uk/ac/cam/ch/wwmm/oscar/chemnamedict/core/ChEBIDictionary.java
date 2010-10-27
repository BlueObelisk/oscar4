package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.net.URI;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictIO;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IInChIProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IMutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ISMILESProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.MutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

public class ChEBIDictionary extends MutableChemNameDict
implements IMutableChemNameDict, IInChIProvider, ISMILESProvider {

	public ChEBIDictionary() throws Exception {
		super(new URI("http://wwmm.ch.cam.ac.uk/dictionary/chebi/"));
		ChemNameDictIO.readXML(
			new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/chemnamedict/")
				.getXMLDocument("chemnamedict.xml"),
			this
		);
	}

}
