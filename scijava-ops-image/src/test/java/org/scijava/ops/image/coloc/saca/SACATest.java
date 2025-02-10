/*-
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

package org.scijava.ops.image.coloc.saca;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.scijava.types.Nil;
import org.scijava.ops.image.AbstractColocalisationTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@code coloc.saca} ops.
 *
 * @author Edward Evans
 */

public class SACATest extends AbstractColocalisationTest {

	// XY positions to sample
	private final static int[] xPositions = { 30, 79, 77, 104, 7, 52, 164, 88,
		119, 65 };
	private final static int[] yPositions = { 30, 36, 80, 79, 139, 102, 77, 41,
		142, 118 };

	// image containers
	private static Img<DoubleType> zscore;

	@BeforeAll
	public static void setUpTest() {
		// green and red colocalization images
		Img<UnsignedByteType> green = getPositiveCorrelationImageCh1();
		Img<UnsignedByteType> red = getPositiveCorrelationImageCh2();

		// get slice 15 from colocalization data
		var gs = ops.op("transform.hyperSliceView").input(green, 2, 15).apply();
		var rs = ops.op("transform.hyperSliceView").input(red, 2, 15).apply();

		// create image container
		zscore = ops.op("create.img").input(gs, new DoubleType()).outType(
			new Nil<Img<DoubleType>>()
			{}).apply();

		// run SACA heatmap Z score op
		ops.op("coloc.saca.heatmapZScore").input(gs, rs).output(zscore).compute();
	}

	@Test
	public void testSACAHeatmapZScore() {
		final double[] zscoreExpected = { 0.0, 6.117364936585281, 0.0,
			-1.282447034877343, 0.0, 6.642396454955293, 0.0, -1.6567255788972388, 0.0,
			3.5385003044434877 };

		// get random access and assert results are equal
		final RandomAccess<DoubleType> zRA = zscore.randomAccess();
		for (int i = 0; i < xPositions.length; i++) {
			zRA.setPosition(xPositions[i], 0);
			zRA.setPosition(yPositions[i], 1);
			assertEquals(zscoreExpected[i], zRA.get().getRealDouble());
		}
	}

	@Test
	public void testSACASigMask() {
		final double[] sigExpected = { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
			0.0 };

		// create image container
		Img<BitType> sigMask = ops.op("create.img").input(zscore, new BitType())
			.outType(new Nil<Img<BitType>>()
			{}).apply();

		// run SACA significant pixel mask op
		ops.op("coloc.saca.sigMask").input(zscore).output(sigMask).compute();

		// get random access and assert results are equal
		final RandomAccess<BitType> sRA = sigMask.randomAccess();
		for (int i = 0; i < xPositions.length; i++) {
			sRA.setPosition(xPositions[i], 0);
			sRA.setPosition(yPositions[i], 1);
			assertEquals(sigExpected[i], sRA.get().getRealDouble());
		}
	}
}
