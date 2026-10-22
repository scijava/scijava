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

package org.scijava.ops.image.image.piv;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DefaultPIV}.
 * <p>
 * The test frames are synthetic particle images: Gaussian particles at random
 * real positions, rendered exactly, which move by a known displacement field
 * from the first frame to the second.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultPIVTest extends AbstractOpTest {

	@Test
	public void testUniformIntegerDisplacement() {
		final double[] u = { 5, -3 };
		final var frames = frames(new long[] { 128, 128 }, 0.02, x -> u, 1);
		final var result = piv(frames, new int[] { 32 }, new int[] { 16 }, null,
			null, null);
		assertArrayEquals(new long[] { 7, 7 }, result.getGridSize());
		forEachGridPoint(result, g -> {
			assertArrayEquals(u, result.getDisplacement(g), 0.05);
			assertTrue(result.getCorrelation(g) > 0.95);
			assertTrue(result.isValid(g));
		});
	}

	@Test
	public void testUniformIntegerDisplacementWithoutSubpixel() {
		final double[] u = { -7, 4 };
		final var frames = frames(new long[] { 96, 96 }, 0.02, x -> u, 2);
		final var result = piv(frames, new int[] { 32 }, null, null, false, null);
		assertArrayEquals(new long[] { 3, 3 }, result.getGridSize());
		forEachGridPoint(result, g -> assertArrayEquals(u, result.getDisplacement(
			g), 0));
	}

	@Test
	public void testUniformSubpixelDisplacement() {
		final double[] u = { 2.3, -1.6 };
		final var frames = frames(new long[] { 128, 128 }, 0.02, x -> u, 3);
		final var result = piv(frames, new int[] { 32 }, new int[] { 16 }, null,
			null, null);
		forEachGridPoint(result, g -> assertArrayEquals(u, result.getDisplacement(
			g), 0.1));
	}

	@Test
	public void testShear() {
		// Horizontal flow increasing linearly with y, from -3 to +3 pixels.
		final var gamma = 6.0 / 128;
		final var frames = frames(new long[] { 128, 128 }, 0.02, x -> new double[] {
			gamma * (x[1] - 64), 0 }, 4);
		final var result = piv(frames, new int[] { 24 }, new int[] { 12 },
			new int[] { 6 }, null, null);
		// Note: the velocity gradient within a window broadens the correlation
		// peak, which biases the subpixel fit, so individual vectors may be off
		// by a few tenths of a pixel.
		final var sumSq = new double[1];
		forEachGridPoint(result, g -> {
			final var y = result.getPosition(g)[1];
			final var u = result.getDisplacement(g);
			assertEquals(gamma * (y - 64), u[0], 0.3);
			assertEquals(0, u[1], 0.3);
			sumSq[0] += Math.pow(u[0] - gamma * (y - 64), 2) + u[1] * u[1];
		});
		final var size = result.getGridSize();
		final var rms = Math.sqrt(sumSq[0] / (size[0] * size[1]));
		assertTrue(rms < 0.15, "RMS error " + rms);
	}

	@Test
	public void testPositions() {
		final var frames = frames(new long[] { 70, 50 }, 0.03, x -> new double[] {
			1, 1 }, 5);
		final var translated = new RandomAccessibleInterval[] { Views.translate(
			frames[0], 10, 20), Views.translate(frames[1], 10, 20) };
		final var result = piv(translated, new int[] { 16, 8 }, new int[] { 10,
			12 }, new int[] { 4 }, null, null);
		// (70 - 16) / 10 + 1 = 6 by (50 - 8) / 12 + 1 = 4.
		assertArrayEquals(new long[] { 6, 4 }, result.getGridSize());
		assertArrayEquals(new double[] { 10 + 7.5, 20 + 3.5 }, result.getPosition(
			0, 0), 0);
		assertArrayEquals(new double[] { 10 + 50 + 7.5, 20 + 36 + 3.5 }, result
			.getPosition(5, 3), 0);
		final var positions = result.getPositions();
		assertArrayEquals(new long[] { 6, 4, 2 }, positions.dimensionsAsLongArray());
		assertEquals(10 + 30 + 7.5, positions.getAt(3, 2, 0).get(), 0);
		assertEquals(20 + 24 + 3.5, positions.getAt(3, 2, 1).get(), 0);
		final var displacements = result.getDisplacements();
		assertEquals(result.getDisplacement(3, 2)[1], displacements.getAt(3, 2, 1)
			.get(), 0);
	}

	/**
	 * In one region of the second frame, the particles are scrambled, so that
	 * the window covering it gets a spurious vector. The median test must flag
	 * and replace it.
	 */
	@Test
	public void testMedianValidation() {
		final double[] u = { 3, 2 };
		final var frames = frames(new long[] { 128, 128 }, 0.02, x -> u, 6);
		final var scrambled = frames(new long[] { 128, 128 }, 0.02, x -> u, 7)[1];
		final var region = Views.interval(scrambled, new long[] { 60, 60 },
			new long[] { 99, 99 });
		final var target = Views.interval(frames[1], region);
		final var c = target.cursor();
		final var s = region.cursor();
		while (c.hasNext())
			c.next().set(s.next());
		final int[] window = { 32 };

		final var raw = piv(frames, window, null, null, null, null);
		final var spurious = raw.getDisplacement(2, 2);
		assertFalse(Math.abs(spurious[0] - u[0]) < 1 && Math.abs(spurious[1] -
			u[1]) < 1, "The scrambled window should give a spurious vector.");

		final var validated = piv(frames, window, null, null, null, 2.0);
		assertFalse(validated.isValid(2, 2));
		assertArrayEquals(u, validated.getDisplacement(2, 2), 0.1);
		var invalid = 0;
		for (final var v : Views.iterable(validated.getValid()))
			if (!v.get()) invalid++;
		assertEquals(1, invalid);
		assertArrayEquals(u, validated.getDisplacement(0, 0), 0.05);
	}

	@Test
	public void testUniformSubpixelDisplacement3D() {
		final double[] u = { 1.4, -2.0, 0.7 };
		final var frames = frames(new long[] { 32, 32, 32 }, 0.01, x -> u, 8);
		final var result = piv(frames, new int[] { 16 }, null, new int[] { 4 },
			null, null);
		assertArrayEquals(new long[] { 2, 2, 2 }, result.getGridSize());
		forEachGridPoint(result, g -> assertArrayEquals(u, result.getDisplacement(
			g), 0.15));
	}

	@Test
	public void testFlatWindow() {
		final var frame = ArrayImgs.floats(32, 32);
		final var result = piv(new RandomAccessibleInterval[] { frame, frame },
			new int[] { 16 }, null, null, null, null);
		assertTrue(Double.isNaN(result.getDisplacement(0, 0)[0]));
		assertTrue(Double.isNaN(result.getCorrelation(1, 1)));
		assertFalse(result.isValid(1, 0));
	}

	private PIVResult piv(final RandomAccessibleInterval<?>[] frames,
		final int[] windowSize, final int[] step, final int[] searchRange,
		final Boolean subpixel, final Double medianThreshold)
	{
		return ops.op("image.piv").input(frames[0], frames[1], windowSize,
			step, searchRange, subpixel, medianThreshold).outType(PIVResult.class)
			.apply();
	}

	private static void forEachGridPoint(final PIVResult result,
		final Consumer<long[]> action)
	{
		final var size = result.getGridSize();
		final var g = new long[size.length];
		while (true) {
			action.accept(g.clone());
			var d = 0;
			for (; d < g.length; d++) {
				if (++g[d] < size[d]) break;
				g[d] = 0;
			}
			if (d == g.length) return;
		}
	}

	/**
	 * Renders two particle images: particles at uniformly random positions,
	 * which are displaced by the given field, evaluated at their positions in
	 * the first frame.
	 */
	@SuppressWarnings("unchecked")
	private static RandomAccessibleInterval<FloatType>[] frames(
		final long[] dims, final double density,
		final Function<double[], double[]> field, final long seed)
	{
		final var rnd = new Random(seed);
		final var n = dims.length;
		var volume = 1.0;
		for (final var d : dims)
			volume *= d + 20;
		final var count = (int) (density * volume);
		final var first = new double[count][n];
		final var second = new double[count][n];
		final var amplitudes = new double[count];
		for (var i = 0; i < count; i++) {
			// Note: particles also lie around the frame, so that some move in.
			for (var d = 0; d < n; d++)
				first[i][d] = (dims[d] + 20) * rnd.nextDouble() - 10;
			final var u = field.apply(first[i]);
			for (var d = 0; d < n; d++)
				second[i][d] = first[i][d] + u[d];
			amplitudes[i] = 0.5 + rnd.nextDouble();
		}
		return new RandomAccessibleInterval[] { render(dims, first, amplitudes),
			render(dims, second, amplitudes) };
	}

	private static RandomAccessibleInterval<FloatType> render(final long[] dims,
		final double[][] particles, final double[] amplitudes)
	{
		final var sigma = 1.2;
		final var img = ArrayImgs.floats(dims);
		final var cursor = img.localizingCursor();
		final var x = new double[dims.length];
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.localize(x);
			var sum = 0.0;
			for (var i = 0; i < particles.length; i++) {
				var r2 = 0.0;
				for (var d = 0; d < x.length && r2 < 50; d++) {
					final var dx = x[d] - particles[i][d];
					r2 += dx * dx;
				}
				if (r2 < 50) sum += amplitudes[i] * Math.exp(-r2 / (2 * sigma *
					sigma));
			}
			cursor.get().setReal(sum);
		}
		return img;
	}
}
