package uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import opennlp.maxent.io.GISModelReader;

/**Reads opennlp Maxent models from strings
 * 
 * @author ptc24
 *
 */
public final class StringGISModelReader extends GISModelReader {

	private BufferedReader br;
	
	public StringGISModelReader(String s) {
		 br = new BufferedReader(new StringReader(s));
	}
	
	@Override
	protected int readInt() throws IOException {
        return Integer.parseInt(br.readLine());
	}

	@Override
	protected double readDouble() throws IOException {
        return Double.parseDouble(br.readLine());
	}

	@Override
	protected String readUTF() throws IOException {
		return br.readLine();
	}

}
