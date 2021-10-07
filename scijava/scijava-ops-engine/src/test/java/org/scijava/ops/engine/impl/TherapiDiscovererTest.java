package org.scijava.ops.engine.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.discovery.Discovery;

public class TherapiDiscovererTest extends TherapiDiscoverer {

	@Test
	public void discoverClass() {
		List<Discovery<AnnotatedElement>> list = new TherapiDiscoverer().elementsTaggedWith("test");
		List<AnnotatedElement> elements = list.stream().map(d -> d.discovery()).collect(Collectors.toList());
		Assert.assertTrue(elements.contains(ClassTest.class));
	}

	@Test
	public void discoverField() throws NoSuchFieldException, SecurityException {
		List<Discovery<AnnotatedElement>> list = new TherapiDiscoverer().elementsTaggedWith("test");
		List<AnnotatedElement> elements = list.stream().map(d -> d.discovery()).collect(Collectors.toList());
		Assert.assertTrue(elements.contains(this.getClass().getDeclaredField("fieldTest")));
	}

	@Test
	public void discoverMethod() throws NoSuchMethodException, SecurityException {
		List<Discovery<AnnotatedElement>> list = new TherapiDiscoverer().elementsTaggedWith("test");
		List<AnnotatedElement> elements = list.stream().map(d -> d.discovery()).collect(Collectors.toList());
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
