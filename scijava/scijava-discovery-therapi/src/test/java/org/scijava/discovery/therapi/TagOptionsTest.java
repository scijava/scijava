
package org.scijava.discovery.therapi;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TagOptionsTest {

	private List<TaggedElement> getTaggedDiscoveries(
		String tagType)
	{
		return new TaggedElementDiscoverer(tagType).discover(TaggedElement.class);
	}

	@Test
	public void optionsTest() {
		List<TaggedElement> elements = getTaggedDiscoveries(
			"optionsTest");
		TaggedElement annotatedElement = elements.get(0);
		Assert.assertEquals("e", annotatedElement.option("singleKey"));
		Assert.assertEquals("[e1, e2]", annotatedElement.option("listKey"));
	}

	@Test
	public void optionsPerLineTest() {
		List<TaggedElement> elements = getTaggedDiscoveries(
			"optionsPerLineTest");
		TaggedElement annotatedElement = elements.get(0);
		Assert.assertEquals("e", annotatedElement.option("singleKey"));
		Assert.assertEquals("[e1, e2]", annotatedElement.option("listKey"));
	}

	/**
	 * Tests failure upon malformed tag declaration. In this case, failure is
	 * expected when a tag does not delimit key-value pairs with commas.
	 */
	@Test
	public void forgottenCommaTest() {
		List<TaggedElement> elements = getTaggedDiscoveries(
			"forgottenComma");
		TaggedElement annotatedElement = elements.get(0);
		Assert.assertThrows(IllegalArgumentException.class, //
			() -> annotatedElement.option("singleKey"));
	}

	/**
	 * Tests ability to parse options without quotes.
	 */
	@Test
	public void forgottenQuoteTest() {
		List<TaggedElement> elements = getTaggedDiscoveries(
			"forgottenQuote");
		TaggedElement annotatedElement = elements.get(0);
		Assert.assertEquals("e", annotatedElement.option("singleKey"));
	}

	/**
	 * Tests duplicate definition behaviors. When a tag tries to define a tag
	 * twice, we expect that the second definition overwrites the first.
	 */
	@Test
	public void duplicateOptionTest() {
		List<TaggedElement> elements = getTaggedDiscoveries(
			"duplicateOption");
		TaggedElement annotatedElement = elements.get(0);
		Assert.assertEquals("[e1, e2]", annotatedElement.option("singleKey"));
	}

	/**
	 * Tests behavior for options not present on a tagged element.
	 */
	@Test
	public void absentOptionTest() {
		List<TaggedElement> elements = getTaggedDiscoveries(
			"absentOption");
		TaggedElement annotatedElement = elements.get(0);
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
