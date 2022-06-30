package org.scijava.discovery.therapi;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scijava.discovery.Discovery;
import org.scijava.parse2.Parser;

public class TherapiDiscovererTest {

	private Parser parser;

	@Before
	public void setUp() {
		parser = ServiceLoader.load(Parser.class).findFirst().get();
	}

	private List<Discovery<AnnotatedElement>> getTaggedDiscoveries(
		String tagType)
	{
		return new TherapiDiscoverer(parser).elementsTaggedWith(tagType);
	}

	private List<AnnotatedElement> getTaggedElements(String tagType) {
		return getTaggedDiscoveries(tagType).stream() //
				.map(d -> d.discovery()) //
				.collect(Collectors.toList());
	}

	@Test
	public void discoverClass() {
		List<AnnotatedElement> elements = getTaggedElements("test");
		Assert.assertTrue(elements.contains(ClassTest.class));
	}

	@Test
	public void discoverField() throws NoSuchFieldException, SecurityException {
		List<AnnotatedElement> elements = getTaggedElements("test");
		Assert.assertTrue(elements.contains(this.getClass().getDeclaredField("fieldTest")));
	}

	@Test
	public void discoverMethod() throws NoSuchMethodException, SecurityException {
		List<AnnotatedElement> elements = getTaggedElements("test");
		Assert.assertTrue(elements.contains(this.getClass().getDeclaredMethod("methodTest")));
	}

	/**
	 * @implNote test
	 */
	public void methodTest() {
		
	}

	/**
	 * @implNote test
	 */
	public final Function<Integer, Integer> fieldTest = (in) -> in+ 1;
}

/**
 * @implNote test
 * @author gselz
 *
 */
class ClassTest {
	
}
