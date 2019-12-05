/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops.logic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.type.logic.BitType;

import org.junit.Test;
import org.scijava.ops.core.builder.OpBuilder;

/**
 * Tests {@link BooleanTypeLogic}.
 * 
 * @author Leon Yang
 */
public class BooleanTypeLogicTest extends AbstractOpTest {

	@Test
	public void testAnd() {
		assertTrue(op("logic.and").input(new BitType(true), new BitType(true)).outType(BitType.class)
				.apply().get());
		assertFalse(op("logic.and").input(new BitType(true), new BitType(false)).outType(BitType.class)
				.apply().get());
		assertFalse(op("logic.and").input(new BitType(false), new BitType(true)).outType(BitType.class)
				.apply().get());
		assertFalse(op("logic.and").input(new BitType(false), new BitType(false)).outType(BitType.class)
				.apply().get());
	}

	@Test
	public void testComparableGreaterThan() {
		assertTrue(op("logic.greaterThan").input(5.0, 3.0).outType(BitType.class).apply().get());
		assertFalse(op("logic.greaterThan").input(5.0, 6.0).outType(BitType.class).apply().get());
	}

	@Test
	public void testComparableGreaterThanOrEqual() {
		assertTrue(op("logic.greaterThanOrEqual").input(5.0, 3.0).outType(BitType.class).apply().get());
		assertTrue(op("logic.greaterThanOrEqual").input(5.0, 5.0).outType(BitType.class).apply().get());
		assertFalse(
				op("logic.greaterThanOrEqual").input(5.0, 6.0).outType(BitType.class).apply().get());
	}

	@Test
	public void testComparableLessThan() {
		assertFalse(op("logic.lessThan").input(5.0, 3.0).outType(BitType.class).apply().get());
		assertTrue(op("logic.lessThan").input(5.0, 6.0).outType(BitType.class).apply().get());
	}

	@Test
	public void testComparableLessThanOrEqual() {
		assertFalse(op("logic.lessThanOrEqual").input(5.0, 3.0).outType(BitType.class).apply().get());
		assertTrue(op("logic.lessThanOrEqual").input(5.0, 6.0).outType(BitType.class).apply().get());
		assertTrue(op("logic.lessThanOrEqual").input(5.0, 5.0).outType(BitType.class).apply().get());
	}

	@Test
	public void testObjectsEqual() {
		assertFalse(op("logic.equal").input(2, 1).outType(BitType.class).apply().get());
		assertTrue(op("logic.equal").input(2, 2).outType(BitType.class).apply().get());
		assertFalse(op("logic.equal").input(2, 3).outType(BitType.class).apply().get());
	}

	@Test
	public void testObjectsNotEqual() {
		assertTrue(op("logic.notEqual").input(2, 1).outType(BitType.class).apply().get());
		assertFalse(op("logic.notEqual").input(2, 2).outType(BitType.class).apply().get());
		assertTrue(op("logic.notEqual").input(2, 3).outType(BitType.class).apply().get());
	}

	@Test
	public void testNot() {
		assertFalse(op("logic.not").input(new BitType(true)).outType(BitType.class).apply().get());
		assertTrue(op("logic.not").input(new BitType(false)).outType(BitType.class).apply().get());
	}

	@Test
	public void testOr() {
		assertTrue(op("logic.or").input(new BitType(true), new BitType(true)).outType(BitType.class)
				.apply().get());
		assertTrue(op("logic.or").input(new BitType(true), new BitType(false)).outType(BitType.class)
				.apply().get());
		assertTrue(op("logic.or").input(new BitType(false), new BitType(true)).outType(BitType.class)
				.apply().get());
		assertFalse(op("logic.or").input(new BitType(false), new BitType(false)).outType(BitType.class)
				.apply().get());
	}

	@Test
	public void testXor() {
		assertFalse(op("logic.xor").input(new BitType(true), new BitType(true)).outType(BitType.class)
				.apply().get());
		assertTrue(op("logic.xor").input(new BitType(true), new BitType(false)).outType(BitType.class)
				.apply().get());
		assertTrue(op("logic.xor").input(new BitType(false), new BitType(true)).outType(BitType.class)
				.apply().get());
		assertFalse(op("logic.xor").input(new BitType(false), new BitType(false)).outType(BitType.class)
				.apply().get());
	}
}
