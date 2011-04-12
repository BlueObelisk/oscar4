package uk.ac.cam.ch.wwmm.oscardata;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Sam Adams
 */
public class OscarDataTest {

    @Test
    public void testFindData() {
        String s = "Lorem ipsum dolor sit amet, consectetur adipisicing" +
				" elit, sed 1H NMR (d6-DMSO, 400 MHz): 9.25 (t, J=6.4, 1H)," +
				" 9.16 (d, J=8.4, 1H) do eiusmod tempor incididunt ut labore" +
				" et dolore magna aliqua.";

        OscarData oscarData = new OscarData();

		List<DataAnnotation> annotations = oscarData.findData(s);
		assertEquals(1, annotations.size());
		assertEquals("1H NMR (d6-DMSO, 400 MHz): 9.25 (t, J=6.4, 1H), 9.16 (d, J=8.4, 1H)", annotations.get(0).getSurface());
		assertEquals("<spectrum type=\"hnmr\">" +
						"1H NMR (" +
							"<quantity type=\"solvent\">" +
								"d6-DMSO" +
							"</quantity>" +
							", " +
							"<quantity type=\"frequency\">" +
								"<value>" +
									"<point>400</point>" +
								"</value>" +
								" " +
								"<units>MHz</units>" +
							"</quantity>" +
							"): " +
							"<peaks type=\"..\">" +
								"<peak>" +
									"<quantity type=\"shift\">" +
										"<value>" +
											"<point>9.25</point>" +
										"</value>" +
									"</quantity>" +
									" (" +
									"<quantity type=\"peaktype\">" +
										"t" +
									"</quantity>" +
									", J=" +
									"<quantity type=\"coupling\">" +
										"<value>" +
											"<point>6.4</point>" +
										"</value>" +
									"</quantity>" +
									", " +
									"<quantity type=\"integral\">" +
										"<value>" +
											"<point>1</point>" +
										"</value>" +
										"<units>H</units>" +
									"</quantity>" +
									")" +
								"</peak>" +
								", " +
								"<peak>" +
									"<quantity type=\"shift\">" +
										"<value>" +
											"<point>9.16</point>" +
										"</value>" +
									"</quantity>" +
									" (" +
									"<quantity type=\"peaktype\">" +
										"d" +
									"</quantity>" +
									", J=" +
									"<quantity type=\"coupling\">" +
										"<value>" +
											"<point>8.4</point>" +
										"</value>" +
									"</quantity>" +
									", " +
									"<quantity type=\"integral\">" +
										"<value>" +
											"<point>1</point>" +
										"</value>" +
										"<units>H</units>" +
									"</quantity>" +
									")" +
								"</peak>" +
							"</peaks>" +
						"</spectrum>"
							, annotations.get(0).getAnnotatedElement().toXML());
    }

}
