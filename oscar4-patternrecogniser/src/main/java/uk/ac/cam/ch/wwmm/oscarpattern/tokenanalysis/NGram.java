package uk.ac.cam.ch.wwmm.oscarpattern.tokenanalysis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Sam Adams
 */
public class NGram {

    private static final String ALPHABET = "$^S0%<>&'()*+,-./:;=?@[]abcdefghijklmnopqrstuvwxyz|~";
    private static final String MODEL_FILE = "ngram-model.dat.gz";

    private static NGram instance;

    // Empirically determined scaling factor to limit loss of precision
    // in moving from double to short
    // Values range -40 to + 40
    private static final double SCALE = 500;

    private final int len = ALPHABET.length();
    private final int step0 = len*len*len;
    private final int step1 = len*len;
    private final int step2 = len;
    private SuffixClassifier suffixClassifier = null;//Disabled during OMII refactor due to computational cost //suffixClassifier = new SuffixClassifier(chemSet, engSet);

    private final short[] data;

    public static synchronized NGram getInstance() {
        if (instance == null) {
            try {
                instance = new NGram();
            } catch (IOException e) {
                throw new RuntimeException("Error loading data", e);
            }
        }
        return instance;
    }

    private NGram() throws IOException {
        // private constructor
        this.data = new short[len*len*len*len];
        loadData();
    }

    private NGram(short[] data) {
        this.data = data;
    }

    private void loadData() throws IOException {
        InputStream is = getClass().getResourceAsStream(MODEL_FILE);
        if (is == null) {
            throw new FileNotFoundException("File not found: "+MODEL_FILE);
        }
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(
                        new GZIPInputStream(is)));
        try {
            int len4 = len*len*len*len;
            for (int i = 0; i < len4; i++) {
                data[i] = in.readShort();
            }
        } catch (IOException e) {
            in.close();
        }
    }

    private void saveData() throws IOException {
        DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(
                        new GZIPOutputStream(
                                new FileOutputStream(MODEL_FILE))));
        try {
            int len4 = len*len*len*len;
            for (int i = 0; i < len4; i++) {
                out.writeShort(data[i]);
            }
        } catch (IOException e) {
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

        int s1 = ALPHABET.indexOf(w.charAt(0));
        int s2 = ALPHABET.indexOf(w.charAt(1));
        int s3 = ALPHABET.indexOf(w.charAt(2));
        int s0 = 0;
        double logP = 0;
        for(int i=3;i<l;i++) {
            s0 = s1;
            s1 = s2;
            s2 = s3;
            s3 = ALPHABET.indexOf(w.charAt(i));
            short score = data[s0* step0 + s1* step1 + s2* step2 + s3];
            logP += (score/SCALE);
        }
        return logP;
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

    

    public static void main(String[] args) throws IOException {

//        buildModelFile();
        String s = "The quick brown ethyl ethanoate ethanoate. jumps over the lazy ferrous ferrous. bromide bromide.";
//        compareModels(s);

    }
}
