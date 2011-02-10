package uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis;

import java.io.IOException;
import java.io.StringWriter;

import opennlp.maxent.GISModel;
import opennlp.maxent.io.GISModelWriter;

/**Writes opennlp Maxent models to strings.
 * 
 * @author ptc24
 *
 */
public final class StringGISModelWriter extends GISModelWriter {

	private StringWriter stringWriter;
	
	public StringGISModelWriter(GISModel model) {
		super(model);
		
		stringWriter = new StringWriter();
	}
	
	@Override
	protected void writeUTF(String arg0) throws IOException {
		stringWriter.write(arg0);
		stringWriter.write("\n");
	}
	
	@Override
	protected void writeDouble(double arg0) throws IOException {
		stringWriter.write(Double.toString(arg0));
		stringWriter.write("\n");
	}
	
	@Override
	protected void writeInt(int arg0) throws IOException {
		stringWriter.write(Integer.toString(arg0));
		stringWriter.write("\n");
	}
	
	@Override
	protected void close() throws IOException {
		stringWriter.close();
	}
	
	public String toString() {
		return stringWriter.toString();
	}

}
