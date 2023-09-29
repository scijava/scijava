/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

package net.imagej.ops2.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link BinaryRealTypeMath}.
 *
 * @author Mark Hiner
 */
public class BinaryRealTypeMathTest extends AbstractOpTest {

	@Test
	public void testAddF() {
		IntType a = new IntType(25);
		IntType b = new IntType(45);
		IntType c = ops.binary("math.add").input(a, b).outType(IntType.class).apply();
		assertEquals(c.get(), a.get() + b.get());
	}

	@Test
	public void testAddC() {
		IntType a = new IntType(25);
		IntType b = new IntType(45);
		IntType c = new IntType();
		ops.binary("math.add").input(a, b).output(c).compute();
		assertEquals(c.get(), a.get() + b.get());
	}

// FIXME: currently not working
//	@Test
//	public void testAddI() {
//		IntType a = new IntType(25);
//		IntType b = new IntType(45);
//		int expected = a.get() + b.get();
//		ops.binary("math.add").input(a, b).mutate1();
//		assertEquals(a.get(), expected);
//	}
}
