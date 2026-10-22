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

package org.scijava.ops.image.image.quantileNormalize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Quantile normalization of a set of images (Bolstad et al. 2003), which
 * gives all images the same intensity distribution.
 * <p>
 * The pixels of each image (within its mask, if given) are sorted and split
 * into {@code q} quantile classes of (nearly) equal size: the pixel of rank
 * {@code r} (from 0) among {@code N} pixels is in class
 * {@code floor(r · q / N)}. For each class, the mean intensity of its pixels
 * in each image is computed, and the replacement value of the class is the
 * average of these means over all images in which the class is not empty.
 * Every pixel is then replaced by the replacement value of its class. Pixels
 * outside the masks keep their values. Equal intensities are ranked in flat
 * iteration order, so tied pixels at a class boundary may be split
 * deterministically between two classes.
 * </p>
 * <p>
 * With {@code q} equal to the (common) number of pixels, this is the classic
 * quantile normalization of Bolstad et al.: the k-th smallest value of each
 * image becomes the mean of the k-th smallest values. The images may have
 * different sizes and dimensions.
 * </p>
 * <p>
 * Implemented from the paper. For matching a single image to a reference
 * distribution, see {@code image.histogramMatch}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <B> mask pixel type
 * @see <a href="https://doi.org/10.1093/bioinformatics/19.2.185">B. M.
 *      Bolstad, R. A. Irizarry, M. Astrand and T. P. Speed, "A comparison of
 *      normalization methods for high density oligonucleotide array data
 *      based on variance and bias", Bioinformatics 19(2):185-193, 2003</a>
 * @implNote op names='image.quantileNormalize'
 */
public class QuantileNormalize<T extends RealType<T>, B extends BooleanType<B>>
	implements
	Functions.Arity3<List<RandomAccessibleInterval<T>>, Integer, List<RandomAccessibleInterval<B>>, List<RandomAccessibleInterval<DoubleType>>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	/**
	 * Normalizes the images.
	 *
	 * @param images the images to normalize
	 * @param quantiles the number of quantile classes (default: 256)
	 * @param masks a mask for each image, with the same dimensions, selecting
	 *          the pixels to normalize (default: all pixels)
	 * @return the normalized images, in the same order
	 */
	@Override
	public List<RandomAccessibleInterval<DoubleType>> apply(
		final List<RandomAccessibleInterval<T>> images,
		@Nullable final Integer quantiles,
		@Nullable final List<RandomAccessibleInterval<B>> masks)
	{
		if (images.isEmpty()) {
			throw new IllegalArgumentException("No images given");
		}
		final int q = quantiles == null ? 256 : quantiles;
		if (q < 1) {
			throw new IllegalArgumentException("Number of quantiles must be " +
				"positive: " + q);
		}
		if (masks != null && masks.size() != images.size()) {
			throw new IllegalArgumentException("Expected " + images.size() +
				" masks but got " + masks.size());
		}
		final var k = images.size();
		final var values = new ArrayList<double[]>(k);
		final var selected = new ArrayList<boolean[]>(k);
		final var classes = new ArrayList<int[]>(k);
		final var sums = new double[q];
		final var counts = new int[q];
		for (var i = 0; i < k; i++) {
			final var image = images.get(i);
			final var size = Intervals.numElements(image);
			if (size > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Image is too large: " + size +
					" pixels");
			}
			final var v = new double[(int) size];
			final var sel = new boolean[(int) size];
			var p = 0;
			for (final var t : Views.flatIterable(image))
				v[p++] = t.getRealDouble();
			if (masks == null) Arrays.fill(sel, true);
			else {
				final var mask = masks.get(i);
				if (!Intervals.equalDimensions(image, mask)) {
					throw new IllegalArgumentException(
						"Each mask must have the same dimensions as its image");
				}
				p = 0;
				for (final var b : Views.flatIterable(mask))
					sel[p++] = b.get();
			}

			// Rank the selected pixels.
			final var order = new ArrayList<Integer>();
			for (p = 0; p < v.length; p++)
				if (sel[p]) order.add(p);
			order.sort((a, b) -> Double.compare(v[a], v[b]));
			final var n = order.size();
			final var cls = new int[v.length];
			final var classSums = new double[q];
			final var classCounts = new int[q];
			for (var r = 0; r < n; r++) {
				final var c = (int) ((long) r * q / n);
				final int idx = order.get(r);
				cls[idx] = c;
				classSums[c] += v[idx];
				classCounts[c]++;
			}
			for (var c = 0; c < q; c++) {
				if (classCounts[c] > 0) {
					sums[c] += classSums[c] / classCounts[c];
					counts[c]++;
				}
			}
			values.add(v);
			selected.add(sel);
			classes.add(cls);
		}

		final var output = new ArrayList<RandomAccessibleInterval<DoubleType>>(k);
		for (var i = 0; i < k; i++) {
			final var v = values.get(i);
			final var sel = selected.get(i);
			final var cls = classes.get(i);
			final var out = createOp.apply(images.get(i), new DoubleType());
			var p = 0;
			for (final var t : Views.flatIterable(out)) {
				t.set(sel[p] ? sums[cls[p]] / counts[cls[p]] : v[p]);
				p++;
			}
			output.add(out);
		}
		return output;
	}
}
