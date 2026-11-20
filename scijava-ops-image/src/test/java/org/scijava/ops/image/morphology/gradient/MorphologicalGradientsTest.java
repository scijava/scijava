/* #%L
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

package org.scijava.ops.image.morphology.gradient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.morphology.Erosion;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link MorphologicalGradients}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class MorphologicalGradientsTest extends AbstractOpTest {

	@Test
	public void testStepEdge() {
		final var img = ArrayImgs.unsignedBytes(8, 5);
		Views.interval(img, new long[] { 4, 0 }, new long[] { 7, 4 }).forEach(
			p -> p.set(10));
		final Shape shape = new RectangleShape(1, false);
		final var gradient = apply("morphology.gradient", img, shape);
		final var internal = apply("morphology.internalGradient", img, shape);
		final var external = apply("morphology.externalGradient", img, shape);
		final var laplacian = apply("morphology.laplacian", img, shape);
		final var g = gradient.randomAccess();
		final var i = internal.randomAccess();
		final var e = external.randomAccess();
		final var l = laplacian.randomAccess();
		for (var y = 0; y < 5; y++) {
			for (var x = 0; x < 8; x++) {
				final var edge = x == 3 || x == 4;
				assertEquals(edge ? 10 : 0, g.setPositionAndGet(x, y).get());
				assertEquals(x == 4 ? 10 : 0, i.setPositionAndGet(x, y).get());
				assertEquals(x == 3 ? 10 : 0, e.setPositionAndGet(x, y).get());
				assertEquals(x == 3 ? 10 : x == 4 ? -10 : 0, l.setPositionAndGet(x, y)
					.get());
			}
		}
	}

	@Test
	public void testRandom() {
		final var img = ArrayImgs.unsignedBytes(13, 11, 4);
		final var random = new Random(42);
		img.forEach(p -> p.set(random.nextInt(256)));
		final Shape shape = new HyperSphereShape(2);
		// Reference dilation and erosion, ignoring the pixels outside the image.
		final Img<UnsignedByteType> dil = Dilation.dilate(img, shape, 1);
		final Img<UnsignedByteType> ero = Erosion.erode(img, shape, 1);
		final var gradient = apply("morphology.gradient", img, shape);
		final var internal = apply("morphology.internalGradient", img, shape);
		final var external = apply("morphology.externalGradient", img, shape);
		final var laplacian = apply("morphology.laplacian", img, shape);
		final var c = img.localizingCursor();
		final var d = dil.randomAccess();
		final var e = ero.randomAccess();
		final var g = gradient.randomAccess();
		final var in = internal.randomAccess();
		final var ex = external.randomAccess();
		final var l = laplacian.randomAccess();
		while (c.hasNext()) {
			final var f = c.next().get();
			final var dv = d.setPositionAndGet(c).get();
			final var ev = e.setPositionAndGet(c).get();
			assertEquals(dv - ev, g.setPositionAndGet(c).get());
			assertEquals(f - ev, in.setPositionAndGet(c).get());
			assertEquals(dv - f, ex.setPositionAndGet(c).get());
			assertEquals(dv + ev - 2 * f, l.setPositionAndGet(c).get());
		}
	}

	private RandomAccessibleInterval<FloatType> apply(final String name,
		final Img<UnsignedByteType> img, final Shape shape)
	{
		final var out = ArrayImgs.floats(img.dimensionsAsLongArray());
		ops.op(name).input(img, shape).output(out).compute();
		return out;
	}
}
