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

package org.scijava.ops.image.threshold;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests the histograms of the local histogram-based threshold methods (such as
 * {@code threshold.otsu(image, shape)}).
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LocalHistogramThresholdTest extends AbstractOpTest {

	/**
	 * Neighborhoods spanning at most 256 grey levels get one histogram bin per
	 * grey level, whatever the type, so a 16-bit image with an offset gives the
	 * same mask as the 8-bit image.
	 */
	@Test
	public void testOneBinPerLevel() {
		final var bytes = AutoThresholdParityTest.blobs8();
		final var shorts = ArrayImgs.unsignedShorts(bytes.dimensionsAsLongArray());
		final var c = bytes.cursor();
		for (final var t : shorts)
			t.set(c.next().get() + 1000);
		final var shape = new HyperSphereShape(3);
		for (final var method : new String[] { "threshold.otsu", "threshold.huang",
			"threshold.li" })
		{
			final Img<BitType> expected = ArrayImgs.bits(bytes
				.dimensionsAsLongArray());
			ops.op(method).input(bytes, shape).output(expected).compute();
			final Img<BitType> actual = ArrayImgs.bits(bytes.dimensionsAsLongArray());
			ops.op(method).input(shorts, shape).output(actual).compute();
			final var ec = expected.cursor();
			for (final var b : actual)
				assertEquals(ec.next().get(), b.get(), method);
		}
	}

	/**
	 * Neighborhoods spanning more than 256 grey levels get 256 equal bins over
	 * their range. Before, a 16-bit neighborhood got one bin per grey level, up
	 * to 65536 bins per pixel, which made local thresholds of 16-bit images
	 * very slow.
	 */
	@Test
	public void testWideRange() {
		final var rnd = new Random(4);
		final var img = ArrayImgs.unsignedShorts(96, 96);
		img.forEach(t -> t.set(rnd.nextInt(65536)));
		final var shape = new HyperSphereShape(2);
		final Img<BitType> out = ArrayImgs.bits(img.dimensionsAsLongArray());
		ops.op("threshold.otsu").input(img, shape).output(out).compute();

		final var neighborhoods = shape.neighborhoodsRandomAccessible(Views
			.extendBorder(img)).randomAccess();
		final var oc = out.localizingCursor();
		final var threshold = new UnsignedShortType();
		while (oc.hasNext()) {
			final var b = oc.next().get();
			final var nb = neighborhoods.setPositionAndGet(oc);
			var min = Double.POSITIVE_INFINITY;
			var max = Double.NEGATIVE_INFINITY;
			for (final var t : nb) {
				min = Math.min(min, t.get());
				max = Math.max(max, t.get());
			}
			final var histogram = new Histogram1d<UnsignedShortType>(
				new Real1dBinMapper<>(min, max, 256, false));
			histogram.countData(nb);
			ops.op("threshold.otsu").input(histogram).output(threshold).compute();
			assertEquals(img.getAt(oc).get() > threshold.get(), b);
		}
	}
}
