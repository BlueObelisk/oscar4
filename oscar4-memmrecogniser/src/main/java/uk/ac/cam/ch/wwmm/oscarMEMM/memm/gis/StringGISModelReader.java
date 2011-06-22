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
	
	public StringGISModelReader(String s) throws IOException  {
		 super(new DummyDataReader()); 
         br = new BufferedReader(new StringReader(s));		
	}
	
	@Override
	public int readInt() throws IOException {
        return Integer.parseInt(br.readLine());
	}

	@Override
	public double readDouble() throws IOException {
        return Double.parseDouble(br.readLine());
	}

	@Override
	public String readUTF() throws IOException {
		return br.readLine();
	}

}
