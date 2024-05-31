/*
 * #%L
 * Java collections for efficient primitive storage.
 * %%
 * Copyright (C) 2022 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.collections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link BoolArray}.
 * 
 * @author Mark Hiner
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class BoolArrayTest extends PrimitiveArrayTest {

	/** Tests {@link BoolArray#BoolArray()}. */
	@Test
	public void testConstructorNoArgs() {
		final BoolArray array = new BoolArray();
		assertEquals(0, array.size());
		assertEquals(0, array.copyArray().length);
	}

	/** Tests {@link BoolArray#BoolArray(int)}. */
	@Test
	public void testConstructorSize() {
		final int size = 24;
		final BoolArray array = new BoolArray(size);
		assertEquals(size, array.size());
		assertEquals(size, array.copyArray().length);
	}

	/** Tests {@link BoolArray#BoolArray(boolean[])}. */
	@Test
	public void testConstructorArray() {
		final boolean[] raw = { true, false };
		final BoolArray array = new BoolArray(raw);
		assertSame(raw, array.getArray());
		assertEquals(raw.length, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals(raw[i], array.getValue(i), "@" + i);
			assertEquals(raw[i], array.copyArray()[i]);
		}
	}

	/** Tests {@link BoolArray#addValue(boolean)}. */
	@Test
	public void testAddValue() {
		final boolean[] raw = { true, false };
		final BoolArray array = new BoolArray(raw.clone());
		final boolean e0 = true, e1 = false;
		array.addValue(e0);
		array.addValue(e1);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals(raw[i], array.getValue(i), "@" + i);
		}
		assertEquals(e0, array.getValue(0));
		assertEquals(e1, array.getValue(1));
	}

	/** Tests {@link BoolArray#removeValue(boolean)}. */
	@Test
	public void testRemoveValue() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.removeValue(true); // TFFTF -> FFTF
		assertEquals(raw.length - 1, array.size());
		array.removeValue(false); // FFTF -> FTF
		assertEquals(raw.length - 2, array.size());
		array.removeValue(false); // FTF -> TF
		assertEquals(raw.length - 3, array.size());
		assertTrue(array.getValue(0));
		assertFalse(array.getValue(1));
	}

	/** Tests {@link BoolArray#getValue(int)}. */
	@Test
	public void testGetValue() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals(raw[i], array.getValue(i), "@" + i);
		}
	}

	/** Tests {@link BoolArray#setValue(int, boolean)}. */
	@Test
	public void testSetValue() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		final boolean e0 = false, e2 = true, e4 = false;
		array.setValue(0, e0);
		array.setValue(2, e2);
		array.setValue(4, e4);
		assertEquals(raw.length, array.size());
		assertEquals(e0, array.getValue(0));
		assertEquals(raw[1], array.getValue(1));
		assertEquals(e2, array.getValue(2));
		assertEquals(raw[3], array.getValue(3));
		assertEquals(e4, array.getValue(4));
	}

	/** Tests {@link BoolArray#addValue(int, boolean)}. */
	@Test
	public void testAddValueIndex() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		final boolean e0 = true, e4 = false, e7 = true;
		array.addValue(0, e0);
		array.addValue(4, e4);
		array.addValue(7, e7);
		assertEquals(raw.length + 3, array.size());
		assertEquals(e0, array.getValue(0));
		assertEquals(raw[0], array.getValue(1));
		assertEquals(raw[1], array.getValue(2));
		assertEquals(raw[2], array.getValue(3));
		assertEquals(e4, array.getValue(4));
		assertEquals(raw[3], array.getValue(5));
		assertEquals(raw[4], array.getValue(6));
		assertEquals(e7, array.getValue(7));
	}

	/** Tests {@link BoolArray#remove(int)}. */
	public void testRemoveIndex() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.remove(0);
		assertEquals(raw.length - 1, array.size());
		array.remove(2);
		assertEquals(raw.length - 2, array.size());
		array.remove(4);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

	/** Tests {@link BoolArray#indexOf(boolean)}. */
	@Test
	public void testIndexOf() {
		final boolean[] raw = { true, false };
		final BoolArray array = new BoolArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals(i, array.indexOf(raw[i]), "@" + i);
		}
	}

	/** Tests {@link BoolArray#lastIndexOf(boolean)}. */
	@Test
	public void testLastIndexOf() {
		final boolean[] raw = { true, false };
		final BoolArray array = new BoolArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals(i, array.lastIndexOf(raw[i]), "@" + i);
		}
	}

	/** Tests {@link BoolArray#contains(boolean)}. */
	@Test
	public void testContains() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue(array.contains(raw[i]), "@" + i);
		}
	}

	/**
	 * Tests {@link BoolArray#getArray()} and
	 * {@link BoolArray#setArray(boolean[])}.
	 */
	@Test
	public void testSetArray() {
		final BoolArray array = new BoolArray();
		final boolean[] raw = { true, false, false, true, false };
		array.setArray(raw);
		assertSame(raw, array.getArray());
	}

	/** Tests {@link BoolArray#insert(int, int)}. */
	@Test
	public void testInsert() {
		final boolean[] raw = { true, false, false, true, false };
		testInsert(new BoolArray(raw));
	}

	/** Tests {@link BoolArray#delete(int, int)}. */
	@Test
	public void testDelete() {
		final boolean[] raw = { true, false, false, true, false };
		testDelete(new BoolArray(raw));
	}

	/** Tests {@link BoolArray#get(int)}. */
	@Test
	public void testGet() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals(raw[i], array.get(i).booleanValue(), "@" + i);
		}
	}

	/** Tests {@link BoolArray#set(int, Boolean)}. */
	@Test
	public void testSet() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		final Boolean e0 = false, e2 = true, e4 = false;
		array.set(0, e0);
		array.set(2, e2);
		array.set(4, e4);
		assertEquals(raw.length, array.size());
		assertEquals(e0, array.get(0));
		assertEquals(raw[1], array.getValue(1));
		assertEquals(e2, array.get(2));
		assertEquals(raw[3], array.getValue(3));
		assertEquals(e4, array.get(4));
	}

	/** Tests {@link BoolArray#add(int, Boolean)}. */
	@Test
	public void testAdd() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		final Boolean e6 = true, e7 = false;
		array.add(e6);
		array.add(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals(raw[i], array.getValue(i), "@" + i);
		}
		assertEquals(e6, array.get(5));
		assertEquals(e7, array.get(6));
	}

	/** Tests {@link BoolArray#indexOf(Object)}. */
	@Test
	public void testIndexOfBoxed() {
		final boolean[] raw = { true, false };
		final BoolArray array = new BoolArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals(i, array.indexOf(new Boolean(raw[i])), "@" + i);
		}
		assertEquals(-1, array.indexOf(null));
		assertEquals(-1, array.indexOf("Not a bool"));
	}

	/** Tests {@link BoolArray#lastIndexOf(Object)}. */
	@Test
	public void testLastIndexOfBoxed() {
		final boolean[] raw = { false, true };
		final BoolArray array = new BoolArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals(i, array.lastIndexOf(new Boolean(raw[i])), "@" + i);
		}
		assertEquals(-1, array.lastIndexOf(null));
		assertEquals(-1, array.lastIndexOf("Not a bool"));
	}

	/** Tests {@link BoolArray#contains(Object)}. */
	@Test
	public void testContainsBoxed() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue(array.contains(new Boolean(raw[i])), "@" + i);
		}
		assertFalse(array.contains(null));
		assertFalse(array.contains("Not a bool"));
	}

	/** Tests {@link BoolArray#remove(Object)}. */
	@Test
	public void testRemove() {
		final boolean[] raw = { true, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.remove(new Boolean(raw[0]));
		assertEquals(raw.length - 1, array.size());
		array.remove(new Boolean(raw[2]));
		assertEquals(raw.length - 2, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

	/** Tests {@link BoolArray#containsAll}. */
	@Test
	public void testContainsAll() {
		final boolean[] raw = { true, true };
		final BoolArray array = new BoolArray(raw.clone());

		final ArrayList<Boolean> list = new ArrayList<>();
		assertTrue(array.containsAll(list));
		list.add(true);
		assertTrue(array.containsAll(list));
		list.add(false);
		assertFalse(array.containsAll(list));

		final BoolArray yes = new BoolArray(new boolean[] { true });
		assertTrue(array.containsAll(yes));

		final BoolArray no = new BoolArray(new boolean[] { false, true });
		assertFalse(array.containsAll(no));
	}

	/** Tests {@link BoolArray#addAll(int, java.util.Collection)}. */
	@Test
	public void testAddAll() {
		final boolean[] raw = { true, false, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		final boolean[] add = { true, false };
		final BoolArray toAdd = new BoolArray(add.clone());
		final int index = 3;
		array.addAll(index, toAdd);
		for (int i = 0; i < index; i++) {
			assertEquals(raw[i], array.getValue(i));
		}
		for (int i = index; i < index + add.length; i++) {
			assertEquals(add[i - index], array.getValue(i));
		}
		for (int i = index + add.length; i < raw.length + add.length; i++) {
			assertEquals(raw[i - add.length], array.getValue(i));
		}
	}

	/** Tests {@link BoolArray#removeAll}. */
	@Test
	public void testRemoveAll() {
		final boolean[] raw = { true, false, true, false };
		final BoolArray array = new BoolArray(raw.clone());
		final BoolArray toRemove = new BoolArray(new boolean[] { true, true });
		assertEquals(raw.length, array.size());
		array.removeAll(toRemove);
		assertEquals(raw.length - 2, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}
}
