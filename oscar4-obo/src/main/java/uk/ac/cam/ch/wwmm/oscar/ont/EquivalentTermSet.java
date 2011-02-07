package uk.ac.cam.ch.wwmm.oscar.ont;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * A class to help manage a set of equivalent terms and their
 * associated ids.
 * 
 * @author dmj30
 */
public class EquivalentTermSet {

	Set <String> names = new HashSet<String>();
	Set <String> ids = new HashSet<String>();
	
	public EquivalentTermSet(String name, String id) {
		names.add(name);
		ids.add(id);
	}

	public Collection <String> getNames() {
		return names;
	}

	public Collection <String> getIds() {
		return ids;
	}

	public void addNameIfNovel(String name) {
		names.add(name);
	}

	public void addIdIfNovel(String id) {
		ids.add(id);
	}

	public ListMultimap<String, String> toTermMap() {
		ListMultimap<String, String> termMap = ArrayListMultimap.create();
		for (String name : names) {
			for (String id : ids) {
				termMap.put(name, id);				
			}
		}
		return termMap;
	}

}
