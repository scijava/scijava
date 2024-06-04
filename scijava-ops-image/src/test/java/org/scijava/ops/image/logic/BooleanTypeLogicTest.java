/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.type.logic.BitType;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link BooleanTypeLogic}.
 *
 * @author Leon Yang
 */
public class BooleanTypeLogicTest extends AbstractOpTest {

	@Test
	public void testAnd() {
		assertTrue(ops.op("logic.and").input(new BitType(true), new BitType(true))
			.outType(BitType.class).apply().get());
		assertFalse(ops.op("logic.and").input(new BitType(true), new BitType(false))
			.outType(BitType.class).apply().get());
		assertFalse(ops.op("logic.and").input(new BitType(false), new BitType(true))
			.outType(BitType.class).apply().get());
		assertFalse(ops.op("logic.and").input(new BitType(false), new BitType(
			false)).outType(BitType.class).apply().get());
	}

	@Test
	public void testComparableGreaterThan() {
		assertTrue(ops.op("logic.greaterThan").input(5.0, 3.0).outType(
			BitType.class).apply().get());
		assertFalse(ops.op("logic.greaterThan").input(5.0, 6.0).outType(
			BitType.class).apply().get());
	}

	@Test
	public void testComparableGreaterThanOrEqual() {
		assertTrue(ops.op("logic.greaterThanOrEqual").input(5.0, 3.0).outType(
			BitType.class).apply().get());
		assertTrue(ops.op("logic.greaterThanOrEqual").input(5.0, 5.0).outType(
			BitType.class).apply().get());
		assertFalse(ops.op("logic.greaterThanOrEqual").input(5.0, 6.0).outType(
			BitType.class).apply().get());
	}

	@Test
	public void testComparableLessThan() {
		assertFalse(ops.op("logic.lessThan").input(5.0, 3.0).outType(BitType.class)
			.apply().get());
		assertTrue(ops.op("logic.lessThan").input(5.0, 6.0).outType(BitType.class)
			.apply().get());
	}

	@Test
	public void testComparableLessThanOrEqual() {
		assertFalse(ops.op("logic.lessThanOrEqual").input(5.0, 3.0).outType(
			BitType.class).apply().get());
		assertTrue(ops.op("logic.lessThanOrEqual").input(5.0, 6.0).outType(
			BitType.class).apply().get());
		assertTrue(ops.op("logic.lessThanOrEqual").input(5.0, 5.0).outType(
			BitType.class).apply().get());
	}

	@Test
	public void testObjectsEqual() {
		assertFalse(ops.op("logic.equal").input(2, 1).outType(BitType.class).apply()
			.get());
		assertTrue(ops.op("logic.equal").input(2, 2).outType(BitType.class).apply()
			.get());
		assertFalse(ops.op("logic.equal").input(2, 3).outType(BitType.class).apply()
			.get());
	}

	@Test
	public void testObjectsNotEqual() {
		assertTrue(ops.op("logic.notEqual").input(2, 1).outType(BitType.class)
			.apply().get());
		assertFalse(ops.op("logic.notEqual").input(2, 2).outType(BitType.class)
			.apply().get());
		assertTrue(ops.op("logic.notEqual").input(2, 3).outType(BitType.class)
			.apply().get());
	}

	@Test
	public void testNot() {
		assertFalse(ops.op("logic.not").input(new BitType(true)).outType(
			BitType.class).apply().get());
		assertTrue(ops.op("logic.not").input(new BitType(false)).outType(
			BitType.class).apply().get());
	}

	@Test
	public void testOr() {
		assertTrue(ops.op("logic.or").input(new BitType(true), new BitType(true))
			.outType(BitType.class).apply().get());
		assertTrue(ops.op("logic.or").input(new BitType(true), new BitType(false))
			.outType(BitType.class).apply().get());
		assertTrue(ops.op("logic.or").input(new BitType(false), new BitType(true))
			.outType(BitType.class).apply().get());
		assertFalse(ops.op("logic.or").input(new BitType(false), new BitType(false))
			.outType(BitType.class).apply().get());
	}

	@Test
	public void testXor() {
		assertFalse(ops.op("logic.xor").input(new BitType(true), new BitType(true))
			.outType(BitType.class).apply().get());
		assertTrue(ops.op("logic.xor").input(new BitType(true), new BitType(false))
			.outType(BitType.class).apply().get());
		assertTrue(ops.op("logic.xor").input(new BitType(false), new BitType(true))
			.outType(BitType.class).apply().get());
		assertFalse(ops.op("logic.xor").input(new BitType(false), new BitType(
			false)).outType(BitType.class).apply().get());
	}
}
