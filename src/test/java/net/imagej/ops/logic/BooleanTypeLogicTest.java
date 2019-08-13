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

/**
 * Tests {@link BooleanTypeLogic}.
 * 
 * @author Leon Yang
 */
public class BooleanTypeLogicTest extends AbstractOpTest {

	@Test
	public void testAnd() {
		assertTrue(((BitType) ops.run("logic.and", new BitType(true), new BitType(true))).get());
		assertFalse(((BitType) ops.run("logic.and", new BitType(true), new BitType(false))).get());
		assertFalse(((BitType) ops.run("logic.and", new BitType(false), new BitType(true))).get());
		assertFalse(((BitType) ops.run("logic.and", new BitType(false), new BitType(false))).get());
	}

	@Test
	public void testComparableGreaterThan() {
		assertTrue(((BitType) ops.run("logic.greaterThan", 5.0, 3.0)).get());
		assertFalse(((BitType) ops.run("logic.greaterThan", 5.0, 6.0)).get());
	}

	@Test
	public void testComparableGreaterThanOrEqual() {
		assertTrue(((BitType) ops.run("logic.greaterThanOrEqual", 5.0, 3.0)).get());
		assertTrue(((BitType) ops.run("logic.greaterThanOrEqual", 5.0, 5.0)).get());
		assertFalse(((BitType) ops.run("logic.greaterThanOrEqual", 5.0, 6.0)).get());
	}

	@Test
	public void testComparableLessThan() {
		assertFalse(((BitType) ops.run("logic.lessThan", 5.0, 3.0)).get());
		assertTrue(((BitType) ops.run("logic.lessThan", 5.0, 6.0)).get());
	}

	@Test
	public void testComparableLessThanOrEqual() {
		assertFalse(((BitType) ops.run("logic.lessThanOrEqual", 5.0, 3.0)).get());
		assertTrue(((BitType) ops.run("logic.lessThanOrEqual", 5.0, 6.0)).get());
		assertTrue(((BitType) ops.run("logic.lessThanOrEqual", 5.0, 5.0)).get());
	}

	@Test
	public void testObjectsEqual() {
		assertFalse(((BitType) ops.run("logic.equal", 2, 1)).get());
		assertTrue(((BitType) ops.run("logic.equal", 2, 2)).get());
		assertFalse(((BitType) ops.run("logic.equal", 2, 3)).get());
	}

	@Test
	public void testObjectsNotEqual() {
		assertTrue(((BitType) ops.run("logic.notEqual", 2, 1)).get());
		assertFalse(((BitType) ops.run("logic.notEqual", 2, 2)).get());
		assertTrue(((BitType) ops.run("logic.notEqual", 2, 3)).get());
	}

	@Test
	public void testNot() {
		assertFalse(((BitType) ops.run("logic.not", new BitType(true))).get());
		assertTrue(((BitType) ops.run("logic.not", new BitType(false))).get());
	}

	@Test
	public void testOr() {
		assertTrue(((BitType) ops.run("logic.or", new BitType(true), new BitType(true))).get());
		assertTrue(((BitType) ops.run("logic.or", new BitType(true), new BitType(false))).get());
		assertTrue(((BitType) ops.run("logic.or", new BitType(false), new BitType(true))).get());
		assertFalse(((BitType) ops.run("logic.or", new BitType(false), new BitType(false))).get());
	}

	@Test
	public void testXor() {
		assertFalse(((BitType) ops.run("logic.xor", new BitType(true), new BitType(true))).get());
		assertTrue(((BitType) ops.run("logic.xor", new BitType(true), new BitType(false))).get());
		assertTrue(((BitType) ops.run("logic.xor", new BitType(false), new BitType(true))).get());
		assertFalse(((BitType) ops.run("logic.xor", new BitType(false), new BitType(false))).get());
	}
}
