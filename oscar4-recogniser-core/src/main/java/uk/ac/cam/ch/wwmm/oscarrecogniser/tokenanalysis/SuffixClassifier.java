package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SuffixClassifier {

	static class SuffixNode {
		Map<Character,SuffixNode> continuations;
		SuffixNode previous;
		int chemPaths;
		int nonChemPaths;
		
		public SuffixNode() {
			continuations = new HashMap<Character,SuffixNode>();
			chemPaths = 0;
			nonChemPaths = 0;
			previous = null;
		}

		public SuffixNode(SuffixNode previous) {
			continuations = new HashMap<Character,SuffixNode>();
			chemPaths = 0;
			nonChemPaths = 0;
			this.previous = previous;
		}
		
		public void addString(String s, boolean chemical) {
			if(chemical) {
				chemPaths++;
			} else {
				nonChemPaths++;
			}
			if(s.length() > 0) {
				char c = s.charAt(s.length()-1);
				String prefix = s.substring(0, s.length()-1);
				if(!continuations.containsKey(c)) {
					continuations.put(c, new SuffixNode(this));
				}
				continuations.get(c).addString(prefix, chemical);
			}
		}
		
		public void print(String history) {
			for(Character c : continuations.keySet()) {
				continuations.get(c).print(c + history);
			}
		}
		
		public double chemScore() {
			double smoothFactor = 1.0;
			return (chemPaths + smoothFactor) / (chemPaths + nonChemPaths + smoothFactor + smoothFactor);
		}
		
		public boolean interesting() {
			if(previous == null) return true;
			double cs = chemScore();
			double pcs = previous.chemScore();
			if(cs == 0.5 ^ pcs == 0.5) return true;
			if((cs - 0.5) * (pcs - 0.5) < 0) return true;
			if((cs > 0.5) && (cs > pcs)) return true;
			if((cs < 0.5) && (cs < pcs)) return true;
			return false;
		}
		
		public void prune() {
			Set<Character> cs = new HashSet<Character>(continuations.keySet());
			for(Character c : cs) {
				continuations.get(c).prune();
				if(!continuations.get(c).interesting() && continuations.get(c).continuations.size() == 0) {
					continuations.remove(c);
				}
			}
		}
		
		public double lookup(String s) {
			if(s.length() > 0) {
				char c = s.charAt(s.length()-1);
				String prefix = s.substring(0, s.length()-1);
				if(continuations.containsKey(c)) {
					return continuations.get(c).lookup(prefix);
				}
				if(true) {
					return chemScore();
				} else {
					// There doesn't seem to be any advantage to this...
					if(continuations.keySet().size() < 2) return chemScore();
					if(chemPaths * nonChemPaths == 0) return chemScore();
					int chemBranch = 0;
					int nonChemBranch = 0;
					for(Character cc : continuations.keySet()) {
						if(continuations.get(cc).chemScore() > 0.5) {
							chemBranch++;
						} else {
							nonChemBranch++;
						}
					}
					return chemBranch / (1.0 + chemBranch + nonChemBranch);	
				}				
			} 
			return chemScore();
		}
	
		public int lookupCount(String s, boolean chemical) {
			if(s.length() > 0) {
				char c = s.charAt(s.length()-1);
				String prefix = s.substring(0, s.length()-1);
				if(continuations.containsKey(c)) {
					return continuations.get(c).lookupCount(prefix, chemical);
				}
				return chemical ? chemPaths : nonChemPaths;
			} 
			return chemical ? chemPaths : nonChemPaths;
		}

		public String getSuffix(String s, String suffix) {
			if(s.length() > 0) {
				char c = s.charAt(s.length()-1);
				String prefix = s.substring(0, s.length()-1);
				if(continuations.containsKey(c)) {
					return continuations.get(c).getSuffix(prefix, c + suffix);
				} else {
					return suffix;
				}
			} else{
				return suffix;
			}
		}
	}

	private SuffixNode root;
	
	public SuffixClassifier(Collection<String> chemWords, Collection<String> nonChemWords) {
		root = new SuffixNode();
		for(String word : chemWords) root.addString(word, true);
		for(String word : nonChemWords) root.addString(word, false);
		root.prune();
	}
	
	public double scoreWord(String word) {
		return root.lookup(word);
	}

	public int lookupCount(String word, boolean chemical) {
		return root.lookupCount(word, chemical);
	}
	
	
}
