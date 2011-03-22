package uk.ac.cam.ch.wwmm.oscarrecogniser.saf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

public class StandoffResolverTest {

	@Test
	public void testResolveEthylAcetate() {
		List<NamedEntity> nes = new ArrayList<NamedEntity>();
		nes.add(new NamedEntity("ethyl", 12, 17, NamedEntityType.COMPOUND));
		nes.add(new NamedEntity("acetate", 18, 25, NamedEntityType.COMPOUND));
		NamedEntity ethylAcetate = new NamedEntity("ethyl acetate", 12, 25, NamedEntityType.COMPOUND);
		nes.add(ethylAcetate);
		
		StandoffResolver.resolveStandoffs(nes);
		assertEquals(1, nes.size());
		assertTrue(nes.contains(ethylAcetate));
	}
	
	@Test
	public void testMarkEthylAcetate() {
		List<NamedEntity> nes = new ArrayList<NamedEntity>();
		nes.add(new NamedEntity("ethyl", 12, 17, NamedEntityType.COMPOUND));
		nes.add(new NamedEntity("acetate", 18, 25, NamedEntityType.COMPOUND));
		NamedEntity ethylAcetate = new NamedEntity("ethyl acetate", 12, 25, NamedEntityType.COMPOUND);
		nes.add(ethylAcetate);
		
		StandoffResolver.markBlockedStandoffs(nes);
		assertEquals("ethyl", nes.get(0).getSurface());
		assertTrue(nes.get(0).isBlocked());
		assertEquals("acetate", nes.get(1).getSurface());
		assertTrue(nes.get(1).isBlocked());
		assertEquals("ethyl acetate", nes.get(2).getSurface());
		assertFalse(nes .get(2).isBlocked());
	}
	
	
	@Test
	public void testResolveOnt() {
		List<NamedEntity> nes = new ArrayList<NamedEntity>();
		nes.add(new NamedEntity("acetone", 43, 50, NamedEntityType.ONTOLOGY));
		NamedEntity cm = new NamedEntity("acetone", 43, 50, NamedEntityType.COMPOUND); 
		nes.add(cm);
		
		StandoffResolver.resolveStandoffs(nes);
		assertEquals(1, nes.size());
		assertTrue(nes.contains(cm));
	}
	
	@Test
	public void testMarkOnt() {
		List<NamedEntity> nes = new ArrayList<NamedEntity>();
		nes.add(new NamedEntity("acetone", 43, 50, NamedEntityType.ONTOLOGY));
		nes.add(new NamedEntity("acetone", 43, 50, NamedEntityType.COMPOUND)); 
		
		StandoffResolver.markBlockedStandoffs(nes);
		assertEquals(2, nes.size());
		assertTrue(NamedEntityType.ONTOLOGY.isInstance(nes.get(0).getType()));
		assertTrue(nes.get(0).isBlocked());
		assertTrue(NamedEntityType.COMPOUND.isInstance(nes.get(1).getType()));
		assertFalse(nes.get(1).isBlocked());
	}
	
	
	@Test
	public void testResolveCpr() {
		List<NamedEntity> nes = new ArrayList<NamedEntity>();
		NamedEntity cm = new NamedEntity("1,2-dibromoethane", 43, 60, NamedEntityType.COMPOUND);
		nes.add(cm);
		nes.add(new NamedEntity("1,2-", 43, 47, NamedEntityType.LOCANTPREFIX));
		
		StandoffResolver.resolveStandoffs(nes);
		assertEquals(1, nes.size());
		assertTrue(nes.contains(cm));	
	}
	
	@Test
	public void testMarkCpr() {
		List<NamedEntity> nes = new ArrayList<NamedEntity>();
		NamedEntity cm = new NamedEntity("1,2-dibromoethane", 43, 60, NamedEntityType.COMPOUND);
		nes.add(cm);
		nes.add(new NamedEntity("1,2-", 43, 47, NamedEntityType.LOCANTPREFIX));
		
		StandoffResolver.markBlockedStandoffs(nes);
		assertEquals(2, nes.size());
		assertFalse(nes.get(0).isBlocked());
		assertTrue(NamedEntityType.COMPOUND.isInstance(nes.get(0).getType()));
		assertTrue(nes.get(1).isBlocked());
		assertTrue(NamedEntityType.LOCANTPREFIX.isInstance(nes.get(1).getType()));
	}
}
