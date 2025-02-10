/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.features.tamura2d;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractFeatureTest;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;

/**
 * Test for {@Link Tamura2d}-Features
 *
 * @author Andreas Graumann (University of Konstanz)
 */
public class Tamura2dFeatureTest extends AbstractFeatureTest {

	@Test
	public void testContrastFeature() {
		assertEquals(63.7185, ops.op("features.tamura.contrast").input(random)
			.outType(DoubleType.class).apply().get(), 1e-3, "tamura.contrast");
	}

	@Test
	public void testDirectionalityFeature() {
		assertEquals(0.007819, ops.op("features.tamura.directionality").input(
			random, 16).outType(DoubleType.class).apply().get(), 1e-3,
			"tamura.directionality");
	}

	@Test
	public void testCoarsenessFeature() {
		assertEquals(43.614, ops.op("features.tamura.coarseness").input(random)
			.outType(DoubleType.class).apply().get(), 1e-3, "tamura.coarseness");

		// NB: according to the implementation, this 2x2 image should have exactly 0
		// coarseness.
		byte[] arr = new byte[] { 0, -1, 0, 0 };
		Img<ByteType> in = ArrayImgs.bytes(arr, 2, 2);
		assertEquals(0.0, ops.op("features.tamura.coarseness").input(in).outType(
			DoubleType.class).apply().get(), 0.0, "tamura.coarseness");
	}

}
