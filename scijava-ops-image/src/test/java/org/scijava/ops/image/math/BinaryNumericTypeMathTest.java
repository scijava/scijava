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

package org.scijava.ops.image.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link BinaryNumericTypeMath}.
 *
 * @author Mark Hiner
 */
public class BinaryNumericTypeMathTest extends AbstractOpTest {

	private static final IntType A = new IntType(12);
	private static final IntType B = new IntType(7);

	// TODO inplace would be nice
	private static String[] methods = new String[] { "function", "computer" };

	// ADD
	@Test
	public void testAdd() {
		IntType e = A.copy();
		e.add(B);
		test("math.add", e.get());
	}

	// SUB

	@Test
	public void testSub() {
		IntType e = A.copy();
		e.sub(B);
		test("math.sub", e.get());
	}

	// DIV

	@Test
	public void testDiv() {
		IntType e = A.copy();
		e.div(B);
		test("math.div", e.get());
	}

	// MUL

	@Test
	public void testMul() {
		IntType e = A.copy();
		e.mul(B);
		test("math.mul", e.get());
	}

	// POW

	@Test
	public void testPow() {
		IntType e = A.copy();
		e.pow(B);
		test("math.pow", e.get());
	}

	// -- Helpers --

	/**
	 * Helper method to test that the given Op name run on {@link #A} and
	 * {@link #B} produces the expected value when run using each method type in
	 * {@link #methods}
	 *
	 * @param opNames comma space-separated list of op names to test
	 * @param expectedValue Expected value from op invocation
	 */
	private void test(String opNames, int expectedValue) {
		for (String opName : opNames.split(", ")) {
			for (String m : methods) {
				IntType c = new IntType();
				if (m.equals("function")) c = ops.op(opName).input(A, B).outType(
					IntType.class).apply();
				else if (m.equals("computer")) ops.op(opName).input(A, B).output(c)
					.compute();
				assertEquals(expectedValue, c.get());
			}
		}
	}
}
