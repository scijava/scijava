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

package org.scijava.ops.image.math.add;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.type.numeric.ARGBDoubleType;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link org.scijava.ops.image.math.BinaryNumericTypeMath#adder}.
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class AddNumericTypeBinaryMathAddTest extends AbstractOpTest {

	private final double DELTA = 0.00005;

	@Test
	public void testAdd() {
		final ARGBDoubleType a = new ARGBDoubleType(255, 128, 128, 128);
		final ARGBDoubleType b = new ARGBDoubleType(255, 75, 35, 45);
		final ARGBDoubleType c = new ARGBDoubleType();

		ops.op("math.add").input(a, b).output(c).compute();
		assertEquals(203.0, c.getR(), DELTA);
		assertEquals(163.0, c.getG(), DELTA);
		assertEquals(173.0, c.getB(), DELTA);
	}

}
