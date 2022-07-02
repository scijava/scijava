package org.scijava.ops.engine.simplify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PrimitiveListSimplifierTest {

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
