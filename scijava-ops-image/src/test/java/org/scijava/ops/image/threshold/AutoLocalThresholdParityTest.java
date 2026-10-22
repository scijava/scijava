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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Regression tests pinning the local threshold ops to the masks of Fiji's
 * Auto_Local_Threshold plugin.
 * <p>
 * The expected masks in {@code fiji-local-masks.txt} were obtained by running
 * {@code Auto_Local_Threshold.exec} (fiji/Auto_Local_Threshold at
 * 1.11.1-SNAPSHOT, with ImageJ 1.54p) with default parameters and white
 * objects on the 8-bit images of {@link AutoThresholdParityTest}. Fiji was run
 * with the radius {@code sqrt(r * r - 0.5)}, for which its circular kernel
 * contains exactly the pixels of {@code HyperSphereShape(r)}; with an integer
 * radius, ImageJ kernels also include the pixels at squared distance
 * {@code r * r + 1}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class AutoLocalThresholdParityTest extends AbstractOpTest {

	@Test
	public void testContrast() throws IOException {
		assertFiji("Contrast");
	}

	@Test
	public void testMean() throws IOException {
		assertFiji("Mean");
	}

	@Test
	public void testMedian() throws IOException {
		assertFiji("Median");
	}

	@Test
	public void testMidGrey() throws IOException {
		assertFiji("MidGrey");
	}

	/**
	 * Sauvola with Fiji parameters k = 0.5 and r = 128. A few pixels differ
	 * because ops computes the sample standard deviation, where ImageJ
	 * RankFilters computes the population standard deviation.
	 */
	@Test
	public void testSauvola() throws IOException {
		assertFiji("Sauvola", 10);
	}

	/**
	 * Phansalkar with Fiji parameters k = 0.25 and r = 0.5, on the raw 8-bit
	 * image, which the op stretches to [0, 1] as Fiji does. Before the op
	 * stretched its input, all pixels were background (1490 and 1324 of 4096
	 * pixels differed on blobs8, 2445 and 2205 on narrow8). Now 1, 0, 26 and 36
	 * pixels differ, because ImageJ rounds the stretched image down to 8-bit
	 * values and computes the population rather than the sample standard
	 * deviation; emulating both gives identical masks on narrow8.
	 */
	@Test
	public void testPhansalkar() throws IOException {
		assertFiji("Phansalkar", 40);
	}

	/**
	 * Bernsen with Fiji parameters (contrast threshold 15, and 128 as the mid
	 * grey of low-contrast regions). Fiji rounds the local mid grey
	 * {@code (min + max) / 2} down to an integer before comparing pixels to it
	 * with {@code >=}. Before the op did so too, it missed the pixels half a
	 * grey level below the mid grey (47 and 15 pixels on blobs8, 86 and 46 on
	 * narrow8).
	 */
	@Test
	public void testBernsen() throws IOException {
		assertFiji("Bernsen");
	}

	/**
	 * Niblack with Fiji parameters k = 0.2 and c = 0. Before the op used the
	 * population standard deviation, as ImageJ RankFilters do, rather than the
	 * sample standard deviation, a few pixels differed (7 and 0 on blobs8, 6 and
	 * 1 on narrow8). That Fiji truncates the threshold to an integer makes no
	 * difference, since pixels must be strictly above it.
	 */
	@Test
	public void testNiblack() throws IOException {
		assertFiji("Niblack");
	}

	private void assertFiji(final String method) throws IOException {
		assertFiji(method, 0);
	}

	private void assertFiji(final String method, final int maxDiff)
		throws IOException
	{
		var checked = 0;
		try (var in = new BufferedReader(new InputStreamReader(getClass()
			.getResourceAsStream("fiji-local-masks.txt"), StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = in.readLine()) != null) {
				if (line.startsWith("#")) continue;
				final var f = line.split(" ");
				if (!f[1].equals(method)) continue;
				final var img = image(f[0]);
				final var r = Integer.parseInt(f[2]);
				final Img<BitType> actual = ArrayImgs.bits(img.dimensionsAsLongArray());
				compute(method, img, new HyperSphereShape(r), actual);
				var diff = 0;
				var i = 0;
				for (final var b : actual) {
					final var nibble = Character.digit(f[3].charAt(i / 4), 16);
					final var expected = (nibble & (8 >> (i % 4))) != 0;
					if (expected != b.get()) diff++;
					i++;
				}
				final var msg = method + " on " + f[0] + " with radius " + r + ": " +
					diff + " differing pixels";
				assertTrue(diff <= maxDiff, msg);
				checked++;
			}
		}
		assertEquals(4, checked, "number of " + method + " masks");
	}

	private static Img<UnsignedByteType> image(final String name) {
		if (name.equals("blobs8")) return AutoThresholdParityTest.blobs8();
		if (name.equals("narrow8")) return AutoThresholdParityTest.bytes(
			AutoThresholdParityTest.blobs(2, 90, 6, 130, 6, 70, 150, false));
		throw new IllegalArgumentException(name);
	}

	private static void compute(final String method,
		final Img<UnsignedByteType> img, final HyperSphereShape shape,
		final Img<BitType> out)
	{
		switch (method) {
			case "Contrast":
				ops.op("threshold.localContrast").input(img, shape).output(out)
					.compute();
				break;
			case "Mean":
				ops.op("threshold.localMean").input(img, shape, 0.0).output(out)
					.compute();
				break;
			case "Median":
				ops.op("threshold.localMedian").input(img, shape, 0.0).output(out)
					.compute();
				break;
			case "MidGrey":
				ops.op("threshold.localMidGrey").input(img, shape, 0.0).output(out)
					.compute();
				break;
			case "Bernsen":
				ops.op("threshold.localBernsen").input(img, shape, 15.0, 128.0)
					.output(out).compute();
				break;
			case "Niblack":
				ops.op("threshold.localNiblack").input(img, shape, 0.0, 0.2)
					.output(out).compute();
				break;
			case "Phansalkar":
				ops.op("threshold.localPhansalkar").input(img, shape, 0.25, 0.5)
					.output(out).compute();
				break;
			case "Sauvola":
				ops.op("threshold.localSauvola").input(img, shape, 0.5, 128.0).output(
					out).compute();
				break;
			default:
				throw new IllegalArgumentException(method);
		}
	}
}
