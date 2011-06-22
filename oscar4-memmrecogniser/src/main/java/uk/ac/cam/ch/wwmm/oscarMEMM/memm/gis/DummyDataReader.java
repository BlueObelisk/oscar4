package uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis;

import java.io.IOException;

import opennlp.model.DataReader;

/********************************
 * Dummy class to allow the instantiation of a model
 * @author lezan
 *
 */
public class DummyDataReader implements DataReader{

	public double readDouble() throws IOException{
		throw new UnsupportedOperationException();
		
	}

	public int readInt() throws IOException {
		throw new UnsupportedOperationException();
	}

	public String readUTF() throws IOException {
		throw new UnsupportedOperationException();
	}

}
