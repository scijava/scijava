
package org.scijava.discovery.therapi;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.discovery.Discovery;
import org.scijava.parse2.ParseService;

public class TagOptionsTest {

	private ParseService parser;

	@Before
	public void setUp() {
		Context ctx = new Context(ParseService.class);
		parser = ctx.getService(ParseService.class);
	}

	@After
	public void tearDown() {
		parser.getContext().dispose();
	}

	private List<Discovery<AnnotatedElement>> getTaggedDiscoveries(
		String tagType)
	{
		return new TherapiDiscoverer(parser).elementsTaggedWith(tagType);
	}

	@Test
	public void optionsTest() {
		List<Discovery<AnnotatedElement>> elements = getTaggedDiscoveries(
			"optionsTest");
		Discovery<AnnotatedElement> annotatedElement = elements.get(0);
		Assert.assertEquals("e", annotatedElement.option("singleKey"));
		Assert.assertEquals("[e1, e2]", annotatedElement.option("listKey"));
	}

	@Test
	public void optionsPerLineTest() {
		List<Discovery<AnnotatedElement>> elements = getTaggedDiscoveries(
			"optionsPerLineTest");
		Discovery<AnnotatedElement> annotatedElement = elements.get(0);
		Assert.assertEquals("e", annotatedElement.option("singleKey"));
		Assert.assertEquals("[e1, e2]", annotatedElement.option("listKey"));
	}

	/**
	 * Tests failure upon malformed tag declaration. In this case, failure is
	 * expected when a tag does not delimit key-value pairs with commas.
	 */
	@Test
	public void forgottenCommaTest() {
		List<Discovery<AnnotatedElement>> elements = getTaggedDiscoveries(
			"forgottenComma");
		Discovery<AnnotatedElement> annotatedElement = elements.get(0);
		Assert.assertThrows(IllegalArgumentException.class, //
			() -> annotatedElement.option("singleKey"));
	}

	/**
	 * Tests failure upon malformed tag declaration. In this case, failure is
	 * expected when a tag does not surround a value with quotes.
	 */
	@Test
	public void forgottenQuoteTest() {
		List<Discovery<AnnotatedElement>> elements = getTaggedDiscoveries(
			"forgottenQuote");
		Discovery<AnnotatedElement> annotatedElement = elements.get(0);
		Assert.assertThrows(IllegalArgumentException.class, //
			() -> annotatedElement.option("singleKey"));
	}

	/**
	 * Tests duplicate definition behaviors. When a tag tries to define a tag
	 * twice, we expect that the second definition overwrites the first.
	 */
	@Test
	public void duplicateOptionTest() {
		List<Discovery<AnnotatedElement>> elements = getTaggedDiscoveries(
			"duplicateOption");
		Discovery<AnnotatedElement> annotatedElement = elements.get(0);
		Assert.assertEquals("[e1, e2]", annotatedElement.option("singleKey"));
	}

	/**
	 * Tests behavior for options not present on a tagged element.
	 */
	@Test
	public void absentOptionTest() {
		List<Discovery<AnnotatedElement>> elements = getTaggedDiscoveries(
			"absentOption");
		Discovery<AnnotatedElement> annotatedElement = elements.get(0);
		Assert.assertEquals("", annotatedElement.option("singleKey"));
	}

	/**
	 * @implNote optionsTest singleKey='e', listKey={'e1', 'e2'}
	 */
	public void foo() {}

	/**
	 * @implNote optionsPerLineTest
	 * singleKey='e',
	 * listKey={'e1', 'e2'}
	 */
	public void boo() {}

	/**
	 * A tagged element whose tag doesn't have a comma between options
	 * 
	 * @implNote forgottenComma singleKey='e' listKey={'e1', 'e2'}
	 */
	public void too() {}

	/**
	 * A tagged element whose tag doesn't have a quotes surrounding the value
	 * 
	 * @implNote forgottenQuote singleKey=e, listKey={'e1', 'e2'}
	 */
	public void moo() {}

	/**
	 * A tagged element whose tag tries to define a key twice
	 * 
	 * @implNote duplicateOption singleKey='e', singleKey={'e1', 'e2'}
	 */
	public void coo() {}

	/**
	 * A tagged element who has no options
	 * 
	 * @implNote absentOption
	 */
	public void woo() {}
}
