package uk.ac.cam.ch.wwmm.oscar.formatter;

import java.io.OutputStream;
import java.io.PrintStream;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;

public class STDOUTFormatter implements IOutputFormatter {

	private PrintStream stream;

	public STDOUTFormatter(OutputStream stream) {
		if (stream instanceof PrintStream) {
			this.stream = (PrintStream)stream;
		} else {
			this.stream = new PrintStream(stream);
		}
	}

	public void write(NamedEntity entity, String inchi) {
		this.stream.print(entity.getSurface() + ": ");
		this.stream.println(inchi);
	}

}
