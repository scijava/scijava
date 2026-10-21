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

package org.scijava.ops.image.features.haralick;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.image.cooccurrenceMatrix.MatrixOrientation2D;

/**
 * Tests the IBSI co-occurrence features added to {@code features.haralick}.
 * The expected values were computed with an independent Python script from
 * the IBSI formulas, on the horizontal co-occurrence matrix of a 4x4 image
 * with gray levels 0 to 3 (which the 4-level discretization maps to
 * themselves).
 *
 * @author Curtis Rueden
 */
public class IBSIHaralickFeatureTest extends AbstractOpTest {

	private static final double EPSILON = 1e-12;

	private double feature(final String name) {
		final var img = ArrayImgs.unsignedBytes(new byte[] { //
			0, 0, 1, 1, //
			0, 0, 1, 1, //
			0, 2, 2, 2, //
			2, 2, 3, 3 }, 4, 4);
		return ops.op("features.haralick." + name).input(img, 4, 1,
			MatrixOrientation2D.HORIZONTAL).outType(DoubleType.class).apply().get();
	}

	@Test
	public void testJointAverage() {
		assertEquals(2.0833333333333335, feature("jointAverage"), EPSILON);
	}

	@Test
	public void testAutocorrelation() {
		assertEquals(6.0, feature("autocorrelation"), EPSILON);
	}

	@Test
	public void testDissimilarity() {
		assertEquals(0.41666666666666663, feature("dissimilarity"), EPSILON);
	}

	@Test
	public void testInverseDifferenceNormalized() {
		assertEquals(0.9222222222222222, feature("inverseDifferenceNormalized"),
			EPSILON);
	}

	@Test
	public void testInverseDifferenceMomentNormalized() {
		assertEquals(0.9686274509803922, feature(
			"inverseDifferenceMomentNormalized"), EPSILON);
	}

	@Test
	public void testInverseVariance() {
		assertEquals(0.2708333333333333, feature("inverseVariance"), EPSILON);
	}

	@Test
	public void testClusterTendency() {
		assertEquals(3.576388888888889, feature("clusterTendency"), EPSILON);
	}
}
