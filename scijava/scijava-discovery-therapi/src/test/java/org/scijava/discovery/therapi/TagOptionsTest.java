
package org.scijava.discovery.therapi;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TagOptionsTest {

	private List<TherapiDiscovery> getTaggedDiscoveries(
		String tagType)
	{
		return new TherapiDiscoveryDiscoverer(tagType).discover(TherapiDiscovery.class);
	}

	@Test
	public void optionsTest() {
		List<TherapiDiscovery> elements = getTaggedDiscoveries(
			"optionsTest");
		TherapiDiscovery annotatedElement = elements.get(0);
		Assert.assertEquals("e", annotatedElement.option("singleKey"));
		Assert.assertEquals("[e1, e2]", annotatedElement.option("listKey"));
	}

	@Test
	public void optionsPerLineTest() {
		List<TherapiDiscovery> elements = getTaggedDiscoveries(
			"optionsPerLineTest");
		TherapiDiscovery annotatedElement = elements.get(0);
		Assert.assertEquals("e", annotatedElement.option("singleKey"));
		Assert.assertEquals("[e1, e2]", annotatedElement.option("listKey"));
	}

	/**
	 * Tests failure upon malformed tag declaration. In this case, failure is
	 * expected when a tag does not delimit key-value pairs with commas.
	 */
	@Test
	public void forgottenCommaTest() {
		List<TherapiDiscovery> elements = getTaggedDiscoveries(
			"forgottenComma");
		TherapiDiscovery annotatedElement = elements.get(0);
		Assert.assertThrows(IllegalArgumentException.class, //
			() -> annotatedElement.option("singleKey"));
	}

	/**
	 * Tests failure upon malformed tag declaration. In this case, failure is
	 * expected when a tag does not surround a value with quotes.
	 */
	@Test
	public void forgottenQuoteTest() {
		List<TherapiDiscovery> elements = getTaggedDiscoveries(
			"forgottenQuote");
		TherapiDiscovery annotatedElement = elements.get(0);
		Assert.assertThrows(IllegalArgumentException.class, //
			() -> annotatedElement.option("singleKey"));
	}

	/**
	 * Tests duplicate definition behaviors. When a tag tries to define a tag
	 * twice, we expect that the second definition overwrites the first.
	 */
	@Test
	public void duplicateOptionTest() {
		List<TherapiDiscovery> elements = getTaggedDiscoveries(
			"duplicateOption");
		TherapiDiscovery annotatedElement = elements.get(0);
		Assert.assertEquals("[e1, e2]", annotatedElement.option("singleKey"));
	}

	/**
	 * Tests behavior for options not present on a tagged element.
	 */
	@Test
	public void absentOptionTest() {
		List<TherapiDiscovery> elements = getTaggedDiscoveries(
			"absentOption");
		TherapiDiscovery annotatedElement = elements.get(0);
		Assert.assertEquals("", annotatedElement.option("singleKey"));
	}

	/**
	 * @implNote optionsTest singleKey='e', listKey={'e1', 'e2'}
	 */
	@SuppressWarnings("unused")
	public void foo() {}

	/**
	 * @implNote optionsPerLineTest
	 * singleKey='e',
	 * listKey={'e1', 'e2'}
	 */
	@SuppressWarnings("unused")
	public void boo() {}

	/**
	 * A tagged element whose tag doesn't have a comma between options
	 * 
	 * @implNote forgottenComma singleKey='e' listKey={'e1', 'e2'}
	 */
	@SuppressWarnings("unused")
	public void too() {}

	/**
	 * A tagged element whose tag doesn't have a quotes surrounding the value
	 * 
	 * @implNote forgottenQuote singleKey=e, listKey={'e1', 'e2'}
	 */
	@SuppressWarnings("unused")
	public void moo() {}

	/**
	 * A tagged element whose tag tries to define a key twice
	 * 
	 * @implNote duplicateOption singleKey='e', singleKey={'e1', 'e2'}
	 */
	@SuppressWarnings("unused")
	public void coo() {}

	/**
	 * A tagged element who has no options
	 * 
	 * @implNote absentOption
	 */
	@SuppressWarnings("unused")
	public void woo() {}
}
