package uk.ac.cam.ch.wwmm.oscar.ont;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ListMultimap;


/**
 * 
 * @author dmj30
 *
 */
public class EquivalentTermSetTest {

	@Test
	public void testConstructor() {
		EquivalentTermSet termSet = new EquivalentTermSet("foo", "bar");
		assertEquals(1, termSet.getNames().size());
		assertEquals(1, termSet.getIds().size());
		assertTrue(termSet.getNames().contains("foo"));
		assertTrue(termSet.getIds().contains("bar"));
	}
	
	@Test
	public void testAddNameIfNovel() {
		EquivalentTermSet termSet = new EquivalentTermSet("foo", "");
		assertEquals(1, termSet.getNames().size());
		termSet.addNameIfNovel("foo");
		assertTrue(termSet.getNames().contains("foo"));
		assertEquals(1, termSet.getNames().size());
		termSet.addNameIfNovel("bar");
		assertEquals(2, termSet.getNames().size());
		assertTrue(termSet.getNames().contains("bar"));
	}
	
	@Test
	public void testAddIdIfNovel() {
		EquivalentTermSet termSet = new EquivalentTermSet("", "foo");
		assertEquals(1, termSet.getIds().size());
		termSet.addIdIfNovel("foo");
		assertTrue(termSet.getIds().contains("foo"));
		assertEquals(1, termSet.getIds().size());
		termSet.addIdIfNovel("bar");
		assertEquals(2, termSet.getIds().size());
		assertTrue(termSet.getIds().contains("bar"));
	}
	
	
	@Test
	public void testToTermMap() {
		EquivalentTermSet termSet = new EquivalentTermSet("Foo", "bar1");
		termSet.addNameIfNovel("foo");
		termSet.addIdIfNovel("bar2");
		termSet.addIdIfNovel("bar3");
		
		ListMultimap <String, String> termMap = termSet.toTermMap();
		assertEquals(6, termMap.size());
		assertEquals(2, termMap.keySet().size());
		assertTrue(termMap.containsKey("Foo"));
		assertTrue(termMap.containsKey("foo"));
		
		List <String> ids1 = termMap.get("Foo");
		List <String> ids2 = termMap.get("foo");
		assertEquals(3, ids1.size());
		assertEquals(3, ids2.size());
		assertTrue(ids1.contains("bar1"));
		assertTrue(ids1.contains("bar2"));
		assertTrue(ids1.contains("bar3"));
		assertTrue(ids2.contains("bar1"));
		assertTrue(ids2.contains("bar2"));
		assertTrue(ids2.contains("bar3"));
	}
}
