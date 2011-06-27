package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Sam Adams
 */
public class NGram {

    private static final String MODEL_FILE = "uk/ac/cam/ch/wwmm/oscarrecogniser/tokenanalysis/ngram-model.dat.gz";

    private static NGram instance;

    private final int len = NGramBuilder.ALPHABET.length();
    private final int step0 = len*len*len;
    private final int step1 = len*len;
    private final int step2 = len;
    private SuffixClassifier suffixClassifier = null;//Disabled during OMII refactor due to computational cost //suffixClassifier = new SuffixClassifier(chemSet, engSet);

    private final short[] data;

    /**
     * Loads the serialised NGram model from disk. This is not
     * recommended; the NGram model should be customised according
     * to the ExtractedTrainingData. The serialised model inherited
     * from OSCAR3 was customised using unknown training data
     * and due to a serialisation bug is missing probabilities
     * at the end of the data array. This method is currently
     * retained for convenience during testing.
     * 
     * Use NGramBuilder.buildModel instead.
     */
    @Deprecated
    public static synchronized NGram getInstance() {
        if (instance == null) {
            try {
                instance = new NGram();
                instance.loadData(MODEL_FILE);
            } 
            catch (EOFException e) {
            	//squish it
            }
            catch (IOException e) {
                throw new RuntimeException("Error loading data", e);
            }
        }
        return instance;
    }

    static NGram loadModel(String modelFile) throws IOException {
    	NGram nGram = new NGram();
    	nGram.loadData(modelFile);
    	return nGram;
    }

    private NGram() throws IOException {
        this.data = new short[len*len*len*len];
    }

    NGram(short[] data) {
        this.data = data;
    }

    private void loadData(String modelFile) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream(modelFile);
        if (is == null) {
            throw new FileNotFoundException("File not found: "+modelFile);
        }
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(
                        new GZIPInputStream(is)));
        int len4 = len*len*len*len;
        for (int i = 0; i < len4; i++) {
            data[i] = in.readShort();
        }
    }

    void saveData(OutputStream os) throws IOException {
        DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(
                        new GZIPOutputStream(os)));
        try {
            int len4 = len*len*len*len;
            for (int i = 0; i < len4; i++) {
                out.writeShort(data[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
        	out.close();
        }
    }
    

    /**
     * Test a word against training data.
     * Returned score represents relative log probabilities of chemical vs
     * english; i.e. scores > 0 are probably chemical.
     * @param word String to be tested
     * @return <PRE>ln(P(chemical|word)/P(english|word))</PRE>
     */
    public double testWord(String word) {
        String w = parseWord(word);

        int l = w.length();
        if (l <= 1) {
            return 0;
        }

        w = addStartAndEnd(w);
        l = w.length();

        int s1 = NGramBuilder.ALPHABET.indexOf(w.charAt(0));
        int s2 = NGramBuilder.ALPHABET.indexOf(w.charAt(1));
        int s3 = NGramBuilder.ALPHABET.indexOf(w.charAt(2));
        int s0 = 0;
        double logP = 0;
        for (int i = 3; i < l; i++) {
            s0 = s1;
            s1 = s2;
            s2 = s3;
            s3 = NGramBuilder.ALPHABET.indexOf(w.charAt(i));
            short score = data[s0* step0 + s1* step1 + s2* step2 + s3];
            logP += score;
        }
        return logP/NGramBuilder.SCALE;
    }
    
    
	public double testWordSuffixProb(String word) {
		String w = parseWord(word);
		double p = suffixClassifier.scoreWord(w);
		return p;
	}

	
	public double testWordSuffix(String word) {
		String w = parseWord(word);
		double p = suffixClassifier.scoreWord(w);
		return Math.log(p) - Math.log(1-p);
		//return Math.log(p)/Math.log(1-p);
	}
	
	/**
	 * Prepare a word for nGram parsing by lowercasing and tokenising symbols
	 * TODO should be improved, esp. wrt numbers
	 */
	private static Pattern p1 = Pattern.compile("[1-9]");
	private static Pattern p2 = Pattern.compile("0+");
	private static Pattern p3 = Pattern.compile("[^$^S0%<>&'()*+,./:;=?@|~a-z\\[\\]-]");
	
	public static String parseWord(String w) {
		return 	p3.matcher(
				p2.matcher(
					p1.matcher(
						w.toLowerCase()
						.replace('"', '\'')
						.replace('{', '[')
						.replace('}', ']')
						.replace('\u2013', '-')
						.replace('\u2014', '-')
					).replaceAll("0") )
				.replaceAll("0")  )
			.replaceAll("S");
	}

	public static String addStartAndEnd(String s) {
		return "^^^" + s + "$";
	}

	
	short[] getData() {
		return data;
	}

	
	/**
	 * Compares the n-gram probabilities of the two n-gram models
	 * 
	 * @param nGram
	 * @return true if all of the n-gram probabilities are identical, false otherwise
	 */
	public boolean compareTo(NGram nGram) {
		short [] otherData = nGram.getData();
		if (!(otherData.length == data.length)) {
			return false;
		}
		for (int i = 0; i < data.length; i++) {
			if (!(data[i] == otherData[i])) {
				return false;
			}
		}
		return true;
	}
}
