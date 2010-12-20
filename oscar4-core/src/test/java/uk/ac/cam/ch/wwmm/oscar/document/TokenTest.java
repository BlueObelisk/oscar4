package uk.ac.cam.ch.wwmm.oscar.document;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.types.BioTag;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

public class TokenTest {

	@Test
	public void testValue() {
		Token token = new Token(
			"alsoAToken", 0, 0, null, null, null
		);
		Assert.assertEquals("alsoAToken", token.getValue());
		token.setValue("token");
		Assert.assertEquals("token", token.getValue());
	}

	@Test
	public void testId() {
		Token token = new Token(
			null, 0, 0, null, null, null
		);
		token.setId(777);
		Assert.assertEquals(777, token.getId());
	}

	@Test
	public void testStart() {
		Token token = new Token(
			null, 77, 0, null, null, null
		);
		Assert.assertEquals(77, token.getStart());
	}

	@Test
	public void testEnd() {
		Token token = new Token(
			null, 0, 77, null, null, null
		);
		Assert.assertEquals(77, token.getEnd());
		token.setEnd(88);
		Assert.assertEquals(88, token.getEnd());
	}

	@Test
	public void testBioTag() {
		Token token = new Token(
			null, 0, 77, null, null, null
		);
		token.setBioTag(new BioType(BioTag.B, NamedEntityType.COMPOUND));
		Assert.assertEquals(
			new BioType(BioTag.B, NamedEntityType.COMPOUND),
			token.getBioTag()
		);
	}
}
