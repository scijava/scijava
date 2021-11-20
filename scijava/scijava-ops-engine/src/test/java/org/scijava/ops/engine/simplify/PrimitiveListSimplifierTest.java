package org.scijava.ops.engine.simplify;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.simplify.PrimitiveListSimplifier;

public class PrimitiveListSimplifierTest extends AbstractTestEnvironment {
	
	@Test
	public void testLinkedListSimplifier() {
		List<Long> list = new LinkedList<>();
		list.add(5l);
		PrimitiveListSimplifier<Long> simplifier = new PrimitiveListSimplifier<>();
		List<Number> newList = simplifier.apply(list);
		assertTrue(newList instanceof LinkedList);
		assertFalse(newList instanceof ArrayList);
		assertEquals(newList.get(0), 5l);
	}

	@Test
	public void testArrayListSimplifier() {
		List<Long> list = new ArrayList<>();
		list.add(5l);
		PrimitiveListSimplifier<Long> simplifier = new PrimitiveListSimplifier<>();
		List<Number> newList = simplifier.apply(list);
		assertFalse(newList instanceof LinkedList);
		assertTrue(newList instanceof ArrayList);
		assertEquals(newList.get(0), 5l);
	}

}
