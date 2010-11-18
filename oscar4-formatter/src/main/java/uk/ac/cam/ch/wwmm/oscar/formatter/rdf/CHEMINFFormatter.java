package uk.ac.cam.ch.wwmm.oscar.formatter.rdf;

import java.io.OutputStream;
import java.io.PrintStream;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.formatter.IOutputFormatter;

/**
 * Creates Oscar output in the RDF/Turtle format using the
 * <a href="http://code.google.com/p/semanticchemistry/">CHEMINF</a>
 * ontology.
 *
 * @author egonw
 */
public class CHEMINFFormatter implements IOutputFormatter {

	private PrintStream stream;
	private int counter;

	public CHEMINFFormatter(OutputStream stream) {
		if (stream instanceof PrintStream) {
			this.stream = (PrintStream)stream;
		} else {
			this.stream = new PrintStream(stream);
		}
		counter = 0;
		this.stream.println("@prefix dc: <http://purl.org/dc/elements/1.1/> .");
		this.stream.println("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .");
		this.stream.println("@prefix ex: <http://example.org/stuff/1.0/> .");
		this.stream.println("@prefix cheminf: <http://semanticscience.org/resource/> .");
		this.stream.println("@prefix sio: <http://semanticscience.org/resource/>.");
	}

	public void write(NamedEntity entity, String inchi) {
		this.stream.println("ex:entity" + (counter++) );
		this.stream.println("  rdfs:subClassOf cheminf:CHEMINF_000000 ;");
		this.stream.println("  dc:label \"" + entity.getSurface() + "\" ;");
		this.stream.println("  cheminf:CHEMINF_000200 [");
		this.stream.println("    a cheminf:CHEMINF_000113 ;");
		this.stream.println("    sio:SIO_000300 \"" + inchi + "\" .");
		this.stream.println("  ] .");
	}

}
