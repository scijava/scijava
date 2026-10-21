/*-
 * #%L
 * LGPL-licensed image processing operations for SciJava Ops, translated from LGPL ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.lgpl.morphology.gradient;

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
import org.scijava.ops.image.lgpl.AbstractOpTest;

/**
 * Tests {@link MorphologicalGradients}.
 *
 * @author Curtis Rueden
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
