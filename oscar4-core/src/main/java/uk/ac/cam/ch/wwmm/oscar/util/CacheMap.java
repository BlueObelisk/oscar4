package uk.ac.cam.ch.wwmm.oscar.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** A HashMap from which the least-recently accessed members are removed
 * if the cache goes over capacity.
 * 
 * @author ptc24
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public final class CacheMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = -2368753038583040194L;
	private int capacity;
	
	/**Sets up a CacheMap with a given capacity.
	 * 
	 * @param capacity The capacity of the cache.
	 */
	public CacheMap(int capacity) {
		super(capacity, (float)0.75, true);
		this.capacity = capacity;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > capacity;
	}

}
