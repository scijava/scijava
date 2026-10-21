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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Regression tests pinning the global threshold ops to the thresholds of
 * Fiji's Auto_Threshold plugin.
 * <p>
 * The expected thresholds were obtained by running {@code Auto_Threshold.exec}
 * (fiji/Auto_Threshold at 1.18.1-SNAPSHOT, with ImageJ 1.54p) on the images
 * produced by {@link #blobs}, with "white objects on black background" and no
 * stack histogram. Fiji marks pixels strictly above the threshold as
 * foreground, so each op must produce the mask {@code pixel > threshold}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class AutoThresholdParityTest extends AbstractOpTest {

	private static final int W = 64, H = 64;

	private static final String[] OPS = { "ij1", "intermodes", "isoData", "li",
		"maxEntropy", "mean", "minError", "minimum", "moments", "otsu",
		"percentile", "renyiEntropy", "shanbhag", "triangle", "yen" };

	/** An 8-bit image spanning the full range, including 0 and 255. */
	@Test
	public void testBlobs8() {
		assertFiji(blobs8(), 112, 119, 112, 93, 71, 69, 69, 155, 107, 112, 45, 72,
			156, 73, 67);
	}

	/** An 8-bit image whose values lie in [70, 150]. */
	@Test
	public void testNarrow8() {
		assertFiji(bytes(blobs(2, 90, 6, 130, 6, 70, 150, false)), 109, 119, 108,
			105, 103, 99, 103, 112, 103, 108, 93, 102, 114, 108, 101);
	}

	/** A low-contrast 8-bit image whose values lie in [3, 60]. */
	@Test
	public void testLowContrast8() {
		assertFiji(bytes(blobs(5, 20, 4, 34, 5, 3, 60, false)), 22, 36, 24, 24, 27,
			22, 22, 50, 24, 25, 21, 27, 25, 29, 27);
	}

	/** A 16-bit image, thresholded with one histogram bin per grey level. */
	@Test
	public void testImg16() {
		assertFiji(shorts(blobs(3, 1200, 150, 3000, 300, 0, 65535, false)), 2047,
			2047, 2047, 1842, 1533, 1575, 1584, 2001, 927, 2040, 1268, 1492, 2097,
			1225, 1470);
	}

	private <T extends RealType<T>> void assertFiji(final Img<T> img,
		final int... expected)
	{
		final List<Executable> checks = new ArrayList<>();
		for (var i = 0; i < OPS.length; i++) {
			final var op = "threshold." + OPS[i];
			final var t = expected[i];
			checks.add(() -> assertMask(img, op, t));
		}
		assertAll(checks);
	}

	static <T extends RealType<T>> void assertMask(final Img<T> img,
		final String op, final double threshold, final Object... args)
	{
		final Img<BitType> actual = ArrayImgs.bits(W, H);
		if (args.length == 0) ops.op(op).input(img).output(actual).compute();
		else ops.op(op).input(img, args[0], args[1]).output(actual).compute();
		final var mismatches = new long[1];
		LoopBuilder.setImages(img, actual).forEachPixel((p, b) -> {
			if (b.get() != p.getRealDouble() > threshold) mismatches[0]++;
		});
		assertEquals(0, mismatches[0], op + ": pixels differing from mask > " +
			threshold);
	}

	static Img<UnsignedByteType> blobs8() {
		return bytes(blobs(1, 40, 12, 200, 15, 0, 255, true));
	}

	static Img<UnsignedByteType> bytes(final int[] px) {
		final var img = ArrayImgs.unsignedBytes(W, H);
		var i = 0;
		for (final var t : img)
			t.set(px[i++]);
		return img;
	}

	static Img<UnsignedShortType> shorts(final int[] px) {
		final var img = ArrayImgs.unsignedShorts(W, H);
		var i = 0;
		for (final var t : img)
			t.set(px[i++]);
		return img;
	}

	/**
	 * Generates a deterministic test image: nine discs on a background, with
	 * Gaussian noise and soft edges, clamped to {@code [lo, hi]}. With zero
	 * noise, the image has exactly two grey levels.
	 */
	static int[] blobs(final long seed, final double bg, final double bgSd,
		final double fg, final double fgSd, final int lo, final int hi,
		final boolean extremes)
	{
		final var r = new Random(seed);
		var mask = new double[W * H];
		for (var k = 0; k < 9; k++) {
			final var cx = 6 + r.nextDouble() * (W - 12);
			final var cy = 6 + r.nextDouble() * (H - 12);
			final var rad = 3 + r.nextDouble() * 5;
			for (var y = 0; y < H; y++) {
				for (var x = 0; x < W; x++) {
					if ((x - cx) * (x - cx) + (y - cy) * (y - cy) <= rad * rad) {
						mask[y * W + x] = 1;
					}
				}
			}
		}
		if (bgSd != 0 || fgSd != 0) {
			final var b = new double[W * H];
			for (var y = 0; y < H; y++) {
				for (var x = 0; x < W; x++) {
					double s = 0;
					var n = 0;
					for (var dy = -1; dy <= 1; dy++) {
						for (var dx = -1; dx <= 1; dx++) {
							final var xx = x + dx;
							final var yy = y + dy;
							if (xx < 0 || yy < 0 || xx >= W || yy >= H) continue;
							s += mask[yy * W + xx];
							n++;
						}
					}
					b[y * W + x] = s / n;
				}
			}
			mask = b;
		}
		final var px = new int[W * H];
		for (var i = 0; i < px.length; i++) {
			final var m = mask[i];
			final var v = bg * (1 - m) + fg * m + r.nextGaussian() * (bgSd * (1 -
				m) + fgSd * m);
			px[i] = (int) Math.max(lo, Math.min(hi, Math.round(v)));
		}
		if (extremes) {
			for (var k = 0; k < 15; k++) {
				px[r.nextInt(px.length)] = 0;
				px[r.nextInt(px.length)] = 255;
			}
		}
		return px;
	}
}
