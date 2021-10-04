package org.scijava.ops.engine.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

public class TherapiDiscovererTest extends TherapiDiscoverer {

	@Test
	public void discoverClass() {
		List<AnnotatedElement> list = new TherapiDiscoverer().elementsTaggedWith("test");
		Assert.assertTrue(list.contains(ClassTest.class));
	}

	@Test
	public void discoverField() throws NoSuchFieldException, SecurityException {
		List<AnnotatedElement> list = new TherapiDiscoverer().elementsTaggedWith("test");
		Assert.assertTrue(list.contains(this.getClass().getDeclaredField("fieldTest")));
	}

	@Test
	public void discoverMethod() throws NoSuchMethodException, SecurityException {
		List<AnnotatedElement> list = new TherapiDiscoverer().elementsTaggedWith("test");
		Assert.assertTrue(list.contains(this.getClass().getDeclaredMethod("methodTest")));
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
