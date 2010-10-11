package uk.ac.cam.ch.wwmm.oscar.obo;

/** A synonyn held in an OBO Ontology.
 * 
 * @author ptc24
 *
 */
public final class Synonym {

	private String syn;
	private String type;
	private String source;
	
	Synonym(String syn, String type, String source) {
		this.syn = syn;
		if(type != null && type.length() > 0) this.type = type;
		if(source != null && source.length() > 0) this.source = source;
	}
	
	/**Gets the synonym string.
	 * 
	 * @return The synonym string.
	 */
	public String getSyn() {
		return syn;
	}
	
	/**Gets the type of the synonym.
	 * 
	 * @return The type of the synonym.
	 */
	public String getType() {
		return type;
	}
	
	/**Gets the source of the synonym.
	 * 
	 * @return The source of the synonym.
	 */
	public String getSource() {
		return source;
	}
	
	/**A string representation of the Synonym object.
	 * 
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(syn);
		if(type != null) sb.append(", type=" + type);
		if(source != null) sb.append(", source=" + source);
		sb.append("]");
		return sb.toString();
	}
	
}
