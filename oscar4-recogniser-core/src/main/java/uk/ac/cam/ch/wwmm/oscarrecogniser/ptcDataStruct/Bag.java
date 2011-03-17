package uk.ac.cam.ch.wwmm.oscarrecogniser.ptcDataStruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A Bag (multiset) class, that holds objects and a count of their frequency,
 * but no sequence information. The information is held internally in a 
 * HashMap&lt;X,Integer&gt;. Only counts of 1 and above are stored by this
 * class.
 * 
 * @author ptc24
 *
 * @param <X> The class of the information to be held in the Bag.
 */
public class Bag<X> {

	private Map<X, Integer> counts;
	
	private class BagItemComparator implements Comparator<X> {
		public int compare(X o1, X o2) {
			return counts.get(o1).compareTo(counts.get(o2));
		}
	}
	
	/**Produce an empty new bag.
	 * 
	 */
	public Bag() {
		counts = new HashMap<X, Integer>();
	}

	/**Copy constructor.
	 * 
	 * @param other The bag to copy.
	 */
	public Bag(Bag<X> other) {
		counts = new HashMap<X, Integer>();
		addAll(other);
	}
	
	/**Adds a single instance of an item to the bag.
	 * 
	 * @param item The item to add.
	 */
	public void add(X item) {
		if(!counts.containsKey(item)) counts.put(item, 0);
		counts.put(item, counts.get(item) + 1);
	}

	/**Adds several instances of an item to the bag. Instances can be removed
	 * by adding negative numbers. If the count of the item goes below 1
	 * it will be removed.
	 * 
	 * @param item The item to add.
	 * @param count The number of instances of the item to add.
	 */
	public void add(X item, int count) {
		if(!counts.containsKey(item)) counts.put(item, 0);
		int newCount = counts.get(item) + count;
		if(newCount > 0) {
			counts.put(item, newCount);			
		} else {
			counts.remove(item);
		}
	}

	/**Sets the number of instances for an item in the bag. If the number of
	 * instances is below 1 the item will be removed.
	 * 
	 * @param item The item.
	 * @param count The number of instances.
	 */
	public void set(X item, int count) {
		if(count > 0) {
			counts.put(item, count);			
		} else if(counts.containsKey(item)) {
			counts.remove(item);
		}
	}
	
	/**Adds the contents of another bag to this bag.
	 * 
	 * @param other The other bag.
	 */
	public void addAll(Bag<X> other) {
		for(X word : other.getSet()) {			
			if(!counts.containsKey(word)) counts.put(word, 0);
			counts.put(word, counts.get(word) + other.getCount(word));			
		}
	}
	
	/**Gets the number of instances of the item in the bag.
	 * 
	 * @param item The item to query.
	 * @return The number of instances, zero if the item is not present.
	 */
	public int getCount(X item) {
		if(!counts.containsKey(item)) return 0;
		return counts.get(item);		
	}
	
	/**Gets a list of items from the bag, sorted to place the items with the
	 * highest count first.
	 * 
	 * @return The items in the bag, in decreasing order of frequency.
	 */
	public List<X> getList() {
		List<X> list = new ArrayList<X>(counts.keySet());
		Collections.sort(list, Collections.reverseOrder(new BagItemComparator()));
		return list;
	}
	
	/**A time-optimised version of getList. This works by grouping together
	 * all items with the same count; the procedure should be 
	 * <i>n</i>log<i>n</i> in the number of distinct count values, rather than
	 * in the number of items, which should be helpful where there is a
	 * Zipf-like distribution.
	 * 
	 * @return The items in the bag, in decreasing order of frequency.
	 */
	public List<X> getListQuickly() {
		List<X> results = new ArrayList<X>(counts.size());
		Map<Integer,List<X>> inverted = new HashMap<Integer,List<X>>();
		for(X x : counts.keySet()) {
			Integer count = counts.get(x);
			if(!inverted.containsKey(count)) inverted.put(count, new LinkedList<X>());
			inverted.get(count).add(x);
		}
		List<Integer> l = new ArrayList<Integer>(inverted.keySet());
		Collections.sort(l, Collections.reverseOrder());
		for(Integer i : l) {
			results.addAll(inverted.get(i));
		}
		return results;
	}
	
	/**Gets the set of items in the bag. WARNING: This is the keySet from the
	 * underlying HashMap, and should not be modified.
	 * 
	 * @return The set of items in the bag.
	 */
	public Set<X> getSet() {
		return counts.keySet();
	}
	
	/**Gets the underlying HashMap for the bag.
	 * 
	 * @return The underlying HashMap for the bag.
	 */
	public Map<X,Integer> getCounts() {
		return counts;
	}
	
	/**Gets the number of different items in the bag.
	 * 
	 * @return The number of different items in the bag.
	 */
	public int size() {
		return counts.size();
	}
	
	/**Discard all items from the bag whose count is below a minimum.
	 * 
	 * @param minFreq The minimum count.
	 */
	public void discardInfrequent(int minFreq) {
		for(X s : new ArrayList<X>(counts.keySet())) {
			if(counts.get(s) < minFreq) counts.remove(s);
		}
	}
	
	/**Discard all items from the bag that do not appear in the given set.
	 * 
	 * @param filter The set of items to filter on.
	 */
	public void discardNotInSet(Set<X> filter) {
		for(X s : new HashSet<X>(counts.keySet())) {
			if(!filter.contains(s)) {
				counts.remove(s);
			}
		}
	}
	
	/**Removes all instances of an item from the bag.
	 * 
	 * @param item The item to remove.
	 */
	public void remove(X item) {
		if(counts.containsKey(item)) counts.remove(item);
	}
	
	/**Remove one instance of an item from the bag. This decreases the count
	 * by one, and removes the item entirely if the count goes to zero.
	 * 
	 * @param item The item to remove one of.
	 */
	public void removeOne(X item) {
		int c = getCount(item);
		if(c == 1) {
			counts.remove(item);
		} else if(c > 1) {
			counts.put(item, c-1);
		}
	}
	
	/**Gets the total number of item instances in the bag - that is, the sum of
	 * all of the counts.
	 * 
	 * @return The total number of item instances.
	 */
	public int totalCount() {
		int c = 0;
		for(X s : counts.keySet()) c += counts.get(s);
		return c;
	}

	/**Treats the bag as a probability distribution, and gets the entropy 
	 * (in bits) for that distribution.
	 * 
	 * @return The entropy.
	 */
	public double entropy() {
		double total = totalCount();
		double entropy = 0.0;
		for(X s : counts.keySet()) {
			entropy -= (counts.get(s)/total) * Math.log(counts.get(s)/total) / Math.log(2);
		}
		return entropy;
	}

	/**Gets the cross entropy for this bag and a reference bag. The result is
	 * equivalent to the minimum average space needed to store an item from this
	 * bag, using a coding scheme derived from the reference bag. Note that the
	 * set of items in the reference bag must be a superset of the items in this
	 * bag.
	 * 
	 * @param reference The reference bag.
	 * @return The cross entropy.
	 */
	public double crossEntropy(Bag<X> reference) {
		double refTotal = reference.totalCount();
		double total = totalCount();
		double crossEntropy = 0.0;
		for(X s : counts.keySet()) {
			crossEntropy -= (counts.get(s)/total) * Math.log(reference.getCount(s)/refTotal) / Math.log(2);
		}
		return crossEntropy;
	}

	/**Gets the perplexity of the bag.
	 * 
	 * @return The perplexity.
	 */
	public double perplexity() {
		return Math.pow(2, entropy());
	}
	
	
	//This is hopelessly inefficient anyway. 
	/*public X selectMember() {
		int c = totalCount();
		int n = new Random().nextInt(c);
		int i = 0;
		for(X s : counts.keySet()) {
			i += counts.get(s);
			if(i > n) {
				return s;
			}
		}
		throw new Error("This should never happen");
	}*/
	
	@Override
	public String toString() {
		return counts.toString();
	}
	
	public boolean equals(Bag<X> bag) {
		return counts.equals(bag.counts);
	}
	
	/**Selects the most common member of the bag. If there are several such
	 * items, one of these is chosen arbitrarily
	 * 
	 * @return The most common item.
	 */
	public X mostCommon() {
		X top = null;
		int topFreq = 0;
		for(X x : counts.keySet()) {
			if(counts.get(x) > topFreq) {
				top = x;
				topFreq = counts.get(x);
			}
		}
		return top;
	}
	
}
