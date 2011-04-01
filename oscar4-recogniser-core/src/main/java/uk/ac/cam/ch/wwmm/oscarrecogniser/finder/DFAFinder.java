package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.PrefixFinder;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

/** A DFA-based method for finding multi-token items in text.
 * 
 * Note that there are several sub-classes of this class; however, OSCAR users
 * should not subclass this.
 * 
 * @author ptc24
 *
 */
public abstract class DFAFinder implements Serializable {
	
	/* @dmj30
	 * 
	 * Logging has been disabled as Logger does not implement serialisable
	 * and prevents the serialisation of DFAONTCPRFinder.
	 */
//	private final Logger logger = Logger.getLogger(DFAFinder.class);

	private static final long serialVersionUID = 6130629462990087075L;

	private final Map<NamedEntityType, List<Automaton>> autLists = new HashMap<NamedEntityType, List<Automaton>>();//mapping between token type and automata. Possibly these automata are only used when performing Pattern based entity recognition
	private final Map<NamedEntityType, SuffixTree> simpleAuts = new HashMap<NamedEntityType, SuffixTree>();
	private final Map<NamedEntityType, RunAutomaton> runAuts = new HashMap<NamedEntityType, RunAutomaton>();
	private final Map<String,String> tokenToRep = new HashMap<String,String>();//mapping between token strings and a unique representation code, usually an integer
	private final Set<String> literals = new HashSet<String>();
	private final AtomicInteger tokenId = new AtomicInteger();
	
	protected OntologyTerms ontologyTerms;
	private final Map<String,Integer> ontIdToIntId = new HashMap<String,Integer>();
    private final List<String> ontIds = new ArrayList<String>();
	private final Map<NamedEntityType,Map<Integer,Set<String>>> runAutToStateToOntIds = new HashMap<NamedEntityType,Map<Integer,Set<String>>>();;
	
	private final Map<String,Pattern> subRes = new HashMap<String,Pattern>();
	
	private final static Pattern matchSubRe = Pattern.compile("\\$\\{.*\\}");//dl387: I'm not sure what this is actually supposed to be matching!
	private final static Pattern digitOrSpace = Pattern.compile("[0-9 ]+");

	protected DFAFinder() {}

	protected abstract void loadTerms();
	
	protected void init() {
        initLiterals();
		loadTerms();
		finishInit();
//		logger.debug("Finished initialising DFA Finder");
	}

    private void initLiterals() {
        literals.add("$(");
        literals.add("$)");
        literals.add("$+");
        literals.add("$*");
        literals.add("$|");
        literals.add("$?");
        literals.add("$^");
    }
    
    public OntologyTerms getOntologyTerms() {
    	return ontologyTerms;
    }
    

    private String generateTokenRepresentation(String token) {
		//TODO canonicalise token
		if (isLiteral(token)) {
			return token.substring(1);
		} else if (tokenToRep.containsKey(token)) {
			return tokenToRep.get(token);
		} else {
			String representation = tokenId.incrementAndGet() + " ";
			tokenToRep.put(token, representation);
			if (isSubRe(token)) {
				subRes.put(token, Pattern.compile(token.substring(2, token.length()-1)));
			}
			return representation;
		}
	}

    private boolean isSubRe(String token) {
        return matchSubRe.matcher(token).matches();
    }

    private boolean isLiteral(String token) {
        return literals.contains(token);
    }

    protected Set<String> getSubReRepsForToken(String token) {
		Set<String> representations = new HashSet<String>();
		for(String regex : subRes.keySet()) {
			if (subRes.get(regex).matcher(token).matches()) {
				representations.add(regex);
			}
		}
		return representations;
	}
	
	protected String getCachedTokenRepresentation(String token) {
		//TODO canonicalise token
        if (tokenToRep.containsKey(token)) {
			return tokenToRep.get(token);
		} else {
			return null;
		}
	}
	
	protected void addNamedEntity(String namedEntity, NamedEntityType namedEntityType, boolean alwaysAdd) {

		TokenSequence tokenSequence = Tokeniser.getDefaultInstance().tokenise(namedEntity);
		List<String> tokens = tokenSequence.getTokenStringList();

		if (!alwaysAdd && tokens.size() == 1 && !namedEntity.contains("$")) {
			//seems to be a too-clever way of boosting performance, as single-token
			//named entities will be caught by the chemNameDict.contains(word) call
            return;
        }
		StringBuffer sb = new StringBuffer();
		for(String token : tokens) {
			sb.append(generateTokenRepresentation(token));
		}
		String preReStr = sb.toString();
        TermMaps termMaps = TermMaps.getInstance();
        for(String reStr : StringTools.expandRegex(preReStr)) {
			if (digitOrSpace.matcher(reStr).matches()) {
				if (simpleAuts.containsKey(namedEntityType)) {
					simpleAuts.get(namedEntityType).addContents(reStr);
				} else {
					simpleAuts.put(namedEntityType, new SuffixTree(reStr));
				}
				if (isOntologyTerm(namedEntity, namedEntityType)) {
					List<String> ontologyIds = ontologyTerms.getIdsForTerm(namedEntity);
					for(String ontologyId : ontologyIds) {
						simpleAuts.get(namedEntityType).addContents(reStr + "X" + getNumberForOntologyId(ontologyId));
					}
				} else if (isCustomTerm(namedEntity, namedEntityType)) {
					String customTypeString = termMaps.getCustEnt().get(namedEntity);
					List<String> customTypes = Arrays.asList(StringTools.splitOnWhitespace(customTypeString));
					for(String customType : customTypes) {
						simpleAuts.get(namedEntityType).addContents(reStr + "X" + getNumberForOntologyId(customType));
					}
				}
			} else {
				if (isOntologyTerm(namedEntity, namedEntityType)) {
					List<String> ontologyIds = ontologyTerms.getIdsForTerm(namedEntity);
					sb.append("(X(");
					for (Iterator<String> it = ontologyIds.iterator(); it.hasNext();) {
                        String ontologyId = it.next();
                        sb.append(Integer.toString(getNumberForOntologyId(ontologyId)));
                        if (it.hasNext()) {
                            sb.append('|');
                        }
                    }
					sb.append("))?");
				} else if (isCustomTerm(namedEntity, namedEntityType)) {
					String customTypeString = termMaps.getCustEnt().get(namedEntity);
					List<String> customTypes = Arrays.asList(StringTools.splitOnWhitespace(customTypeString));
					sb.append("(X(");
					for (Iterator<String> it = customTypes.iterator(); it.hasNext();) {
                        String customType = it.next();
                        sb.append(Integer.toString(getNumberForOntologyId(customType)));
                        if (it.hasNext()) {
                            sb.append('|');
                        }
                    }
					sb.append("))?");
				}
				Automaton subAut = new RegExp(sb.toString()).toAutomaton();
                getAutomatonList(namedEntityType).add(subAut);
			}
		}
	}

    private boolean isCustomTerm(String namedEntity, NamedEntityType namedEntityType) {
        return NamedEntityType.CUSTOM.isInstance(namedEntityType) && TermMaps.getInstance().getCustEnt().containsKey(namedEntity);
    }

    private boolean isOntologyTerm(String namedEntity, NamedEntityType namedEntityType) {
        return NamedEntityType.ONTOLOGY.isInstance(namedEntityType) && ontologyTerms.containsTerm(namedEntity);
    }

    private List<Automaton> getAutomatonList(NamedEntityType namedEntityType) {
        if (!autLists.containsKey(namedEntityType)) {
            autLists.put(namedEntityType, new ArrayList<Automaton>());
        }
        return autLists.get(namedEntityType);        
    }

    private int getNumberForOntologyId(String ontId) {
		if (ontIdToIntId.containsKey(ontId)) {
			return ontIdToIntId.get(ontId);
		} else {
			int intId = ontIds.size();
			ontIds.add(ontId);
			ontIdToIntId.put(ontId, intId);
			return intId;
		}
	}
	
	private void finishInit() {
		for(NamedEntityType type : new HashSet<NamedEntityType>(autLists.keySet())) {
			Automaton mainAut = Automaton.union(autLists.get(type));
			mainAut.determinize();
			runAuts.put(type, new RunAutomaton(mainAut, false));
			autLists.remove(type);
		}
		for(NamedEntityType type : new HashSet<NamedEntityType>(simpleAuts.keySet())) {
			Automaton mainAut = simpleAuts.get(type).toAutomaton();
			runAuts.put(NamedEntityType.valueOf(type.getName()+"-b"), new RunAutomaton(mainAut, false));
			simpleAuts.remove(type);
		}
		runAutToStateToOntIds.clear();
		for (NamedEntityType type : runAuts.keySet()) {
			if (NamedEntityType.ONTOLOGY.isInstance(type) || NamedEntityType.CUSTOM.isInstance(type)) {
			    runAutToStateToOntIds.put(type, analyseAutomaton(runAuts.get(type), 'X'));
            }
		}
	}
	
	private Set<String> readOffTags(RunAutomaton runAut, int state) {
		Set<String> tags = new HashSet<String>();
		readOffTags(runAut, state, "", tags);
		return tags;
	}
	
	private void readOffTags(RunAutomaton runAut, int state, String startOfTag, Set<String> tagsFound) {
		if (runAut.isAccept(state)) {
			String ontId = ontIds.get(Integer.parseInt(startOfTag));
			tagsFound.add(ontId);
		}
		for(int i = 0; i < 10; i++) {
			char c = Integer.toString(i).charAt(0);
			int newState = runAut.step(state, c);
			if (newState != -1) {
				readOffTags(runAut, newState, startOfTag + i, tagsFound);
			}
		}
	}
	
	private Map<Integer,Set<String>> analyseAutomaton(RunAutomaton runAut, char tagChar) {
		Map<Integer,Set<String>> tagMap = new HashMap<Integer,Set<String>>();
		for (int i = 0; i < runAut.getSize(); i++) {
			if (runAut.isAccept(i) && runAut.step(i, tagChar) != -1) {
				tagMap.put(i, readOffTags(runAut, runAut.step(i, tagChar)));
			}
		}
		return tagMap;
	}
	
	protected void handleNamedEntity(AutomatonState a, int endToken, TokenSequence t, NECollector collector) {
		String surface = t.getSubstring(a.getStartToken(), endToken);
        NamedEntityType type = a.getType();
        if (type.getParent() != null) {
            type = type.getParent();
        }
		NamedEntity namedEntity = new NamedEntity(t.getTokens(a.getStartToken(), endToken), surface, type);
        collector.collect(namedEntity);
        if (NamedEntityType.ONTOLOGY.isInstance(a.getType())) {
			Set<String> ontologyIds = runAutToStateToOntIds.get(a.getType()).get(a.getState());
            List<String> ontologyIdList = ontologyTerms.getIdsForTerm(StringTools.normaliseName(surface));
			if (ontologyIdList != null) {
                if (ontologyIds == null) {
                    ontologyIds = new HashSet<String>();
                }
                ontologyIds.addAll(ontologyIdList);
            }
            namedEntity.addOntIds(ontologyIds);
		}
		if (NamedEntityType.CUSTOM.isInstance(a.getType())) {
			Set<String> customTypes = runAutToStateToOntIds.get(a.getType()).get(a.getState());
			namedEntity.addCustTypes(customTypes);
		}
	}
	
	/**
	 * Collects a CPR named entity from the beginning of a token where appropriate
	 * (e.g. "1,2-disubstituted") or by combination with the previous token where
	 * the tokeniser has split the CPR (e.g. in "1,2-").
	 * @param t
	 * @param collector
	 */
	protected void handleTokenForPrefix(Token t, NECollector collector) {
		String prefix = PrefixFinder.getPrefix(t.getSurface());
        if (prefix != null) {
            collector.collect(NamedEntity.forPrefix(t, prefix));
        }
        else if ("-".equals(t.getSurface())) {
        	Token prev = t.getNAfter(-1);
        	if (prev != null) {
        		String combinedSurface = t.getTokenSequence().getSurface().substring(
            			prev.getStart(), t.getEnd());
            	prefix = PrefixFinder.getPrefix(combinedSurface);
            	if (prefix != null) {
                    collector.collect(NamedEntity.forPrefix(t, prefix));
                }	
        	}
        }
	}
	
	protected void findItems(TokenSequence tokenSequence, List<RepresentationList> repsList, NECollector collector) {
		findItems(tokenSequence, repsList, 0, tokenSequence.getTokens().size()-1, collector);
	}
	
	protected void findItems(TokenSequence tokenSequence, List<RepresentationList> repsList, int startToken, int endToken, NECollector collector) {
		
		List<AutomatonState> autStates = initAutomatonStates();
        List<AutomatonState> newAutStates = new ArrayList<AutomatonState>();
        for (int i = startToken; i <= endToken; i++) {
            Token token = tokenSequence.getToken(i);
			handleTokenForPrefix(token, collector);
			RepresentationList tokenRepresentations = repsList.get(token.getIndex());
			if (tokenRepresentations.isEmpty()) {
				autStates.clear();
				continue;
			}
			for (NamedEntityType type : runAuts.keySet()) {
				autStates.add(new AutomatonState(runAuts.get(type), type, i));				
			}
			for (String tokenRep : tokenRepresentations) {
                String tokenRepCode = getCachedTokenRepresentation(tokenRep);
                if (tokenRepCode == null) {
                    continue;
                }
				for (int k = 0; k < autStates.size(); k++) {
					AutomatonState a = autStates.get(k).clone();
                    if (stepIntoAutomaton(tokenRepCode, a)) {
						a.addRep(tokenRep);
						if (a.isAccept()) {
                            handleNamedEntity(a, i, tokenSequence, collector);
						}
						newAutStates.add(a);
					}
				}
			}
            // Swap/clear lists
			List<AutomatonState> tmp = autStates;
			autStates = newAutStates;
			tmp.clear();
			newAutStates = tmp;
		}		
	}

    private List<AutomatonState> initAutomatonStates() {
        List<AutomatonState> autStates = new ArrayList<AutomatonState>();
        for (NamedEntityType type : runAuts.keySet()) {
			AutomatonState a = new AutomatonState(runAuts.get(type), type, 0);
			String tokenRepresentation = generateTokenRepresentation("$^");
            if (stepIntoAutomaton(tokenRepresentation, a)) {
				a.addRep("$^");
				autStates.add(a);
			}
		}
        return autStates;
    }

    private boolean stepIntoAutomaton(String tokenRepCode, AutomatonState a) {
        for (int j = 0; j < tokenRepCode.length(); j++) {
            char c = tokenRepCode.charAt(j);
            a.step(c);
            if (a.getState() == -1) {
                return false;
            }
        }
        return true;
    }

}
