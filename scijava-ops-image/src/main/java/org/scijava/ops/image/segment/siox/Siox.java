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

package org.scijava.ops.image.segment.siox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Core implementation of SIOX (Simple Interactive Object Extraction), shared
 * by the {@code segment.siox} ops.
 * <p>
 * SIOX is described in G. Friedland, K. Jantz and R. Rojas, "SIOX: Simple
 * Interactive Object Extraction in Still Images", Proceedings of the Seventh
 * IEEE International Symposium on Multimedia (ISM 2005), pp. 253&ndash;260,
 * <a href="https://doi.org/10.1109/ISM.2005.106">doi:10.1109/ISM.2005.106</a>,
 * and in G. Friedland, K. Jantz, L. Knipping and R. Rojas, "Image
 * Segmentation by Uniform Color Clustering &ndash; Approach and Benchmark
 * Results", Technical Report B-05-07, Department of Computer Science, Freie
 * Universit&auml;t Berlin (2005). Given a color image and a confidence matrix
 * (trimap) marking known background, known foreground and unknown samples,
 * the known samples are converted to CIELAB and clustered into a background
 * and a foreground <em>color signature</em> with a two-stage k-d tree. Each
 * unknown sample is then classified by its nearest signature color, and the
 * result is post-processed by smoothing, erosion, retention of the large (or
 * user-marked) connected components, color region filling and dilation.
 * </p>
 * <p>
 * This class is a translation of {@code org.siox.SioxSegmentator},
 * {@code org.siox.ColorSignature} and {@code org.siox.util.Utils} of the
 * <a href="https://github.com/fiji/Siox_Segmentation">Fiji
 * Siox_Segmentation</a> project (SIOX reference implementation version 1.13,
 * with the user-marked component extension by Ignacio Arganda-Carreras),
 * Copyright 2005, 2006 by Gerald Friedland, Kristian Jantz and Lars Knipping,
 * licensed under the Apache License, Version 2.0
 * (<a href="https://www.apache.org/licenses/LICENSE-2.0">https://www.apache.org/licenses/LICENSE-2.0</a>).
 * Unless required by applicable law or agreed to in writing, that software is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 * </p>
 * <p>
 * Changes from the original: the post-processing (smoothing, erosion,
 * dilation and connected components) is generalized from 2D to n dimensions,
 * applying the original per-axis passes along every axis in turn; colors may
 * come from {@link ARGBType} samples or from three {@link RealType} channels;
 * a color signature whose samples all fall into a single cluster no longer
 * includes unused (black) entries of the sample buffer in its centroid; a
 * missing background is reported by an exception rather than a return value;
 * the interactive detail refinement and the video variants are omitted.
 * Otherwise, for 2D {@link ARGBType} images, the result is identical to that
 * of {@code SioxSegmentator.segmentate}.
 * </p>
 *
 * @author Gerald Friedland
 * @author Kristian Jantz
 * @author Lars Knipping
 * @author Ignacio Arganda-Carreras
 * @author Curtis Rueden
 */
public final class Siox {

	/** Default number of smoothing steps in the post-processing. */
	public static final int DEFAULT_SMOOTHNESS = 6;

	/** Default size factor: keep only components containing known foreground. */
	public static final double DEFAULT_SIZE_FACTOR = 0;

	/** Confidence of a certain foreground sample. */
	public static final float CERTAIN_FOREGROUND_CONFIDENCE = 1.0f;

	/** Minimum confidence of a known foreground sample. */
	public static final float FOREGROUND_CONFIDENCE = 0.8f;

	/** Confidence of a sample of unknown affiliation. */
	public static final float UNKNOWN_REGION_CONFIDENCE = 0.5f;

	/** Maximum confidence of a known background sample. */
	public static final float BACKGROUND_CONFIDENCE = 0.1f;

	/** Confidence of a certain background sample. */
	public static final float CERTAIN_BACKGROUND_CONFIDENCE = 0.0f;

	/** Cluster diameters along the L, a and b axes. */
	private static final float[] LIMITS = { 0.64f, 1.28f, 2.56f };

	private Siox() {
		// Prevent instantiation of utility class.
	}

	// -- Op entry points --

	/**
	 * Runs SIOX on an {@link ARGBType} image with a confidence matrix.
	 *
	 * @param image the color image
	 * @param trimap the confidence matrix, with the same dimensions as the image
	 * @param smoothness number of smoothing steps, or null for the default
	 * @param sizeFactorToKeep size factor of components to keep, or null for
	 *          the default
	 * @param output the output mask, with the same dimensions as the image
	 */
	static <R extends RealType<R>> void segment(
		final RandomAccessibleInterval<ARGBType> image,
		final RandomAccessibleInterval<R> trimap, final Integer smoothness,
		final Double sizeFactorToKeep, final RandomAccessibleInterval<BitType> output)
	{
		checkDims(image, trimap, "trimap");
		final float[] cm = confidences(trimap);
		run(argbToLab(image, cm.length), cm, image, smoothness, sizeFactorToKeep,
			output);
	}

	/**
	 * Runs SIOX on an {@link ARGBType} image with foreground and background
	 * masks.
	 *
	 * @param image the color image
	 * @param foreground mask of known foreground samples
	 * @param background mask of known background samples
	 * @param smoothness number of smoothing steps, or null for the default
	 * @param sizeFactorToKeep size factor of components to keep, or null for
	 *          the default
	 * @param output the output mask, with the same dimensions as the image
	 */
	static <B extends BooleanType<B>> void segment(
		final RandomAccessibleInterval<ARGBType> image,
		final RandomAccessibleInterval<B> foreground,
		final RandomAccessibleInterval<B> background, final Integer smoothness,
		final Double sizeFactorToKeep, final RandomAccessibleInterval<BitType> output)
	{
		checkDims(image, foreground, "foreground mask");
		checkDims(image, background, "background mask");
		final float[] cm = confidences(foreground, background);
		run(argbToLab(image, cm.length), cm, image, smoothness, sizeFactorToKeep,
			output);
	}

	/**
	 * Runs SIOX on a three-channel image with a confidence matrix.
	 *
	 * @param image the color image, with the channels along its last axis
	 * @param trimap the confidence matrix, with the spatial dimensions of the
	 *          image
	 * @param smoothness number of smoothing steps, or null for the default
	 * @param sizeFactorToKeep size factor of components to keep, or null for
	 *          the default
	 * @param output the output mask, with the spatial dimensions of the image
	 */
	static <T extends RealType<T>, R extends RealType<R>> void segmentChannels(
		final RandomAccessibleInterval<T> image,
		final RandomAccessibleInterval<R> trimap, final Integer smoothness,
		final Double sizeFactorToKeep, final RandomAccessibleInterval<BitType> output)
	{
		final Dimensions spatial = spatialDims(image);
		checkDims(spatial, trimap, "trimap");
		final float[] cm = confidences(trimap);
		run(channelsToLab(image, cm.length), cm, spatial, smoothness,
			sizeFactorToKeep, output);
	}

	/**
	 * Runs SIOX on a three-channel image with foreground and background masks.
	 *
	 * @param image the color image, with the channels along its last axis
	 * @param foreground mask of known foreground samples
	 * @param background mask of known background samples
	 * @param smoothness number of smoothing steps, or null for the default
	 * @param sizeFactorToKeep size factor of components to keep, or null for
	 *          the default
	 * @param output the output mask, with the spatial dimensions of the image
	 */
	static <T extends RealType<T>, B extends BooleanType<B>> void
		segmentChannels(final RandomAccessibleInterval<T> image,
			final RandomAccessibleInterval<B> foreground,
			final RandomAccessibleInterval<B> background, final Integer smoothness,
			final Double sizeFactorToKeep,
			final RandomAccessibleInterval<BitType> output)
	{
		final Dimensions spatial = spatialDims(image);
		checkDims(spatial, foreground, "foreground mask");
		checkDims(spatial, background, "background mask");
		final float[] cm = confidences(foreground, background);
		run(channelsToLab(image, cm.length), cm, spatial, smoothness,
			sizeFactorToKeep, output);
	}

	/**
	 * Gets the spatial dimensions of a three-channel image.
	 *
	 * @param image the image, with the channels along its last axis
	 * @return the dimensions of the image without its last axis
	 */
	static Dimensions spatialDims(final RandomAccessibleInterval<?> image) {
		final int n = image.numDimensions();
		if (n < 2 || image.dimension(n - 1) != 3) {
			throw new IllegalArgumentException(
				"Image must have three channels along its last axis");
		}
		final long[] dims = new long[n - 1];
		for (int d = 0; d < n - 1; d++)
			dims[d] = image.dimension(d);
		return new FinalDimensions(dims);
	}

	// -- Core algorithm --

	/**
	 * Segments an image with information from a confidence matrix, as in
	 * {@code SioxSegmentator.segmentate}.
	 * <p>
	 * Confidences of {@link #BACKGROUND_CONFIDENCE} or less mark known
	 * background, those of at least {@link #FOREGROUND_CONFIDENCE} mark known
	 * foreground, and any other value marks a sample of unknown affiliation.
	 * Known foreground samples whose confidence is exactly
	 * {@link #CERTAIN_FOREGROUND_CONFIDENCE} additionally mark the connected
	 * components which are always kept. On return, each entry of {@code cm} is
	 * either {@link #CERTAIN_FOREGROUND_CONFIDENCE} or
	 * {@link #CERTAIN_BACKGROUND_CONFIDENCE}.
	 * </p>
	 *
	 * @param lab CIELAB color of each sample, in flat iteration order
	 * @param cm confidence of each sample, in flat iteration order, which is
	 *          overwritten with the result
	 * @param dims dimensions of the image
	 * @param smoothness number of smoothing steps in the post-processing
	 * @param sizeFactorToKeep if zero, only components containing certain
	 *          foreground samples are kept; otherwise, the largest component,
	 *          those containing certain foreground samples, and every component
	 *          of size at least {@code largestSize / sizeFactorToKeep} are kept
	 * @throws IllegalArgumentException if there is no known background
	 * @throws IllegalStateException if there is no known foreground but some
	 *           samples of unknown affiliation
	 */
	public static void segmentate(final float[][] lab, final float[] cm,
		final long[] dims, final int smoothness, final double sizeFactorToKeep)
	{
		final int n = cm.length;
		final Grid grid = new Grid(dims, n);
		final boolean[] predefinedFg = new boolean[n];

		// Create color signatures.
		final float[][] knownBg = new float[n][];
		final float[][] knownFg = new float[n][];
		int knownBgCount = 0, knownFgCount = 0;
		for (int i = 0; i < n; i++) {
			if (cm[i] <= BACKGROUND_CONFIDENCE) {
				knownBg[knownBgCount++] = lab[i];
			}
			else if (cm[i] >= FOREGROUND_CONFIDENCE) {
				knownFg[knownFgCount++] = lab[i];
				if (cm[i] == CERTAIN_FOREGROUND_CONFIDENCE) predefinedFg[i] = true;
			}
		}
		final float[][] bgSignature = createSignature(knownBg, knownBgCount,
			LIMITS, BACKGROUND_CONFIDENCE);
		final float[][] fgSignature = createSignature(knownFg, knownFgCount,
			LIMITS, BACKGROUND_CONFIDENCE);
		if (bgSignature.length < 1) {
			throw new IllegalArgumentException(
				"Segmentation impossible: no known background");
		}

		// Classify using the color signatures, caching the result per color.
		final Map<ColorKey, float[]> cache = new HashMap<>();
		for (int i = 0; i < n; i++) {
			if (cm[i] >= FOREGROUND_CONFIDENCE) {
				cm[i] = CERTAIN_FOREGROUND_CONFIDENCE;
				continue;
			}
			if (cm[i] <= BACKGROUND_CONFIDENCE) {
				cm[i] = CERTAIN_BACKGROUND_CONFIDENCE;
				continue;
			}
			final ColorKey key = new ColorKey(lab[i]);
			final float[] minDists = cache.get(key);
			final boolean isBackground;
			if (minDists == null) {
				if (fgSignature.length == 0) {
					throw new IllegalStateException(
						"Foreground signature does not exist");
				}
				final float minBg = minSqrDist(lab[i], bgSignature);
				final float minFg = minSqrDist(lab[i], fgSignature);
				// Note: The original compares strictly for the first sample of a
				// color but non-strictly for its cached repeats; this is preserved.
				isBackground = minBg < minFg;
				cache.put(key, new float[] { minBg, minFg });
			}
			else {
				isBackground = minDists[0] <= minDists[1];
			}
			cm[i] = isBackground ? CERTAIN_BACKGROUND_CONFIDENCE
				: CERTAIN_FOREGROUND_CONFIDENCE;
		}

		// Post-processing.
		smooth(cm, grid, 0.33f, 0.33f, 0.33f);
		normalize(cm);
		morph(cm, grid, false);
		keepOnlyLargeComponents(cm, grid, UNKNOWN_REGION_CONFIDENCE,
			sizeFactorToKeep, predefinedFg);
		for (int i = 0; i < smoothness; i++) {
			smooth(cm, grid, 0.33f, 0.33f, 0.33f);
		}
		normalize(cm);
		for (int i = 0; i < n; i++) {
			cm[i] = cm[i] >= UNKNOWN_REGION_CONFIDENCE
				? CERTAIN_FOREGROUND_CONFIDENCE : CERTAIN_BACKGROUND_CONFIDENCE;
		}
		keepOnlyLargeComponents(cm, grid, UNKNOWN_REGION_CONFIDENCE,
			sizeFactorToKeep, predefinedFg);
		fillColorRegions(cm, lab, grid);
		morph(cm, grid, true);
	}

	/**
	 * Converts an sRGB color to CIELAB (reference white D65, 2 degree
	 * observer), as in {@code Utils.rgbToClab}.
	 *
	 * @param r red component in [0, 1]
	 * @param g green component in [0, 1]
	 * @param b blue component in [0, 1]
	 * @return the {L, a, b} triple
	 */
	public static float[] rgbToLab(final float r, final float g, final float b) {
		final float rl = linearize(r) * 100f;
		final float gl = linearize(g) * 100f;
		final float bl = linearize(b) * 100f;

		final float x = rl * 0.4124f + gl * 0.3576f + bl * 0.1805f;
		final float y = rl * 0.2126f + gl * 0.7152f + bl * 0.0722f;
		final float z = rl * 0.0193f + gl * 0.1192f + bl * 0.9505f;

		final float fx = labF(x / 95.047f);
		final float fy = labF(y / 100f);
		final float fz = labF(z / 108.883f);
		return new float[] { (116f * fy) - 16f, 500f * (fx - fy), 200f * (fy -
			fz) };
	}

	// -- Helper methods --

	private static void run(final float[][] lab, final float[] cm,
		final Dimensions dims, final Integer smoothness,
		final Double sizeFactorToKeep, final RandomAccessibleInterval<BitType> out)
	{
		checkDims(dims, out, "output");
		final int s = smoothness == null ? DEFAULT_SMOOTHNESS : smoothness;
		final double f = sizeFactorToKeep == null ? DEFAULT_SIZE_FACTOR
			: sizeFactorToKeep;
		if (s < 0) {
			throw new IllegalArgumentException("Smoothness must be non-negative");
		}
		if (f < 0) {
			throw new IllegalArgumentException("Size factor must be non-negative");
		}
		segmentate(lab, cm, dims.dimensionsAsLongArray(), s, f);
		final Cursor<BitType> c = Views.flatIterable(out).cursor();
		for (final float v : cm) {
			c.next().set(v >= UNKNOWN_REGION_CONFIDENCE);
		}
	}

	private static void checkDims(final Dimensions expected,
		final Dimensions actual, final String what)
	{
		if (!Intervals.equalDimensions(expected, actual)) {
			throw new IllegalArgumentException("Dimensions of " + what +
				" do not match those of the image");
		}
	}

	private static int size(final Dimensions dims) {
		final long n = Intervals.numElements(dims);
		if (n > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large");
		}
		return (int) n;
	}

	private static <R extends RealType<R>> float[] confidences(
		final RandomAccessibleInterval<R> trimap)
	{
		final float[] cm = new float[size(trimap)];
		int i = 0;
		for (final R t : Views.flatIterable(trimap)) {
			cm[i++] = t.getRealFloat();
		}
		return cm;
	}

	private static <B extends BooleanType<B>> float[] confidences(
		final RandomAccessibleInterval<B> foreground,
		final RandomAccessibleInterval<B> background)
	{
		final float[] cm = new float[size(foreground)];
		final Cursor<B> fg = Views.flatIterable(foreground).cursor();
		final Cursor<B> bg = Views.flatIterable(background).cursor();
		for (int i = 0; i < cm.length; i++) {
			final boolean isFg = fg.next().get();
			final boolean isBg = bg.next().get();
			if (isFg && isBg) {
				throw new IllegalArgumentException(
					"Foreground and background masks overlap");
			}
			cm[i] = isFg ? CERTAIN_FOREGROUND_CONFIDENCE : isBg
				? CERTAIN_BACKGROUND_CONFIDENCE : UNKNOWN_REGION_CONFIDENCE;
		}
		return cm;
	}

	private static float[][] argbToLab(
		final RandomAccessibleInterval<ARGBType> image, final int n)
	{
		final Map<Integer, float[]> cache = new HashMap<>();
		final float[][] lab = new float[n][];
		int i = 0;
		for (final ARGBType t : Views.flatIterable(image)) {
			final int rgb = t.get() & 0xffffff;
			lab[i++] = cache.computeIfAbsent(rgb, v -> rgbToLab( //
				ARGBType.red(v) / 255.0f, //
				ARGBType.green(v) / 255.0f, //
				ARGBType.blue(v) / 255.0f));
		}
		return lab;
	}

	private static <T extends RealType<T>> float[][] channelsToLab(
		final RandomAccessibleInterval<T> image, final int n)
	{
		final int c = image.numDimensions() - 1;
		final T type = image.getType();
		// Note: Integer samples are scaled by the maximum of their type, so
		// that e.g. 8-bit and 16-bit images map onto [0, 1]; real samples are
		// assumed to lie in [0, 1] already.
		final float white = type instanceof IntegerType ? (float) type
			.getMaxValue() : 1f;
		final List<Cursor<T>> cursors = new ArrayList<>();
		for (int ch = 0; ch < 3; ch++) {
			cursors.add(Views.flatIterable(Views.hyperSlice(image, c, image.min(c) +
				ch)).cursor());
		}
		final float[][] lab = new float[n][];
		final float[] rgb = new float[3];
		for (int i = 0; i < n; i++) {
			for (int ch = 0; ch < 3; ch++) {
				final float v = cursors.get(ch).next().getRealFloat() / white;
				rgb[ch] = Math.max(0f, Math.min(1f, v));
			}
			lab[i] = rgbToLab(rgb[0], rgb[1], rgb[2]);
		}
		return lab;
	}

	private static float linearize(final float v) {
		return v > 0.04045 ? (float) Math.pow((v + 0.055f) / 1.055f, 2.4) : v /
			12.92f;
	}

	private static float labF(final float v) {
		return v > 0.008856f ? (float) Math.pow(v, (1f / 3f)) : (7.787f * v) +
			(16f / 116f);
	}

	private static float sqrDist(final float[] p, final float[] q) {
		float sum = 0;
		for (int i = 0; i < p.length; i++) {
			sum += (p[i] - q[i]) * (p[i] - q[i]);
		}
		return sum;
	}

	private static float minSqrDist(final float[] p, final float[][] signature) {
		float min = Float.MAX_VALUE;
		for (final float[] q : signature) {
			final float d = sqrDist(p, q);
			if (d < min) min = d;
		}
		return min;
	}

	// -- Color signature (ColorSignature) --

	/**
	 * Creates a color signature by two-stage k-d tree clustering, as in
	 * {@code ColorSignature.createSignature}. Stage one splits the samples
	 * until each leaf is smaller than the cluster diameters; stage two
	 * recombines the leaf centroids the same way, and drops clusters holding
	 * less than {@code threshold} percent of the samples.
	 */
	private static float[][] createSignature(final float[][] input,
		final int length, final float[] limits, final float threshold)
	{
		final List<float[][]> clusters1 = new ArrayList<>();
		final List<float[]> clusters2 = new ArrayList<>();
		stageOne(input, 0, clusters1, limits, length);

		final float[][] centroids = new float[clusters1.size()][];
		for (int i = 0; i < clusters1.size(); i++) {
			final float[][] cluster = clusters1.get(i);
			final int dims = cluster[0].length;
			final float[] centroid = new float[dims + 1]; // +1 for cardinality.
			for (final float[] p : cluster) {
				for (int j = 0; j < dims; j++) {
					centroid[j] += p[j];
				}
			}
			for (int j = 0; j < dims; j++) {
				centroid[j] /= cluster.length;
			}
			centroid[dims] = cluster.length;
			centroids[i] = centroid;
		}
		stageTwo(centroids, 0, clusters2, limits, length, threshold);
		return clusters2.toArray(new float[0][]);
	}

	private static void stageOne(final float[][] points, final int depth,
		final List<float[][]> clusters, final float[] limits, final int length)
	{
		if (length < 1) return;
		final int dims = points[0].length;
		final int curdim = depth % dims;
		float min = points[0][curdim];
		float max = points[0][curdim];
		for (int i = 1; i < length; i++) {
			if (min > points[i][curdim]) min = points[i][curdim];
			if (max < points[i][curdim]) max = points[i][curdim];
		}
		if (max - min > limits[curdim]) {
			final float pivot = ((max - min) / 2.0f) + min;
			final float[][][] halves = split(points, length, curdim, pivot);
			stageOne(halves[0], depth + 1, clusters, limits, halves[0].length);
			stageOne(halves[1], depth + 1, clusters, limits, halves[1].length);
		}
		else {
			// Note: The original adds the whole buffer here, including its unused
			// entries, when all samples fall into one cluster at the top level.
			clusters.add(length == points.length ? points : Arrays.copyOf(points,
				length));
		}
	}

	private static void stageTwo(final float[][] points, final int depth,
		final List<float[]> clusters, final float[] limits, final int total,
		final float threshold)
	{
		if (points.length < 1) return;
		final int dims = points[0].length - 1; // Without cardinality.
		final int curdim = depth % dims;
		float min = points[0][curdim];
		float max = points[0][curdim];
		for (int i = 1; i < points.length; i++) {
			if (min > points[i][curdim]) min = points[i][curdim];
			if (max < points[i][curdim]) max = points[i][curdim];
		}
		if (max - min > limits[curdim]) {
			final float pivot = ((max - min) / 2.0f) + min;
			final float[][][] halves = split(points, points.length, curdim, pivot);
			stageTwo(halves[0], depth + 1, clusters, limits, total, threshold);
			stageTwo(halves[1], depth + 1, clusters, limits, total, threshold);
		}
		else {
			int sum = 0;
			for (final float[] p : points) {
				sum += p[p.length - 1];
			}
			if (((sum * 100.0) / total) >= threshold) {
				final float[] point = new float[points[0].length];
				for (final float[] p : points) {
					for (int j = 0; j < point.length; j++) {
						point[j] += p[j];
					}
				}
				for (int j = 0; j < point.length - 1; j++) {
					point[j] /= points.length;
				}
				clusters.add(point);
			}
		}
	}

	private static float[][][] split(final float[][] points, final int length,
		final int curdim, final float pivot)
	{
		int countSmaller = 0;
		for (int i = 0; i < length; i++) {
			if (points[i][curdim] <= pivot) countSmaller++;
		}
		final float[][] smaller = new float[countSmaller][];
		final float[][] bigger = new float[length - countSmaller][];
		int s = 0, b = 0;
		for (int i = 0; i < length; i++) {
			if (points[i][curdim] <= pivot) smaller[s++] = points[i];
			else bigger[b++] = points[i];
		}
		return new float[][][] { smaller, bigger };
	}

	// -- Post-processing (Utils and SioxSegmentator) --

	/**
	 * Blurs the confidence matrix in place with a three-sample kernel, running
	 * a forward and a backward pass along each axis, as in
	 * {@code Utils.smoothcm}.
	 */
	private static void smooth(final float[] cm, final Grid grid, final float f1,
		final float f2, final float f3)
	{
		for (int d = 0; d < grid.dims.length; d++) {
			final int len = (int) grid.dims[d];
			final int st = grid.strides[d];
			for (final int start : grid.lineStarts[d]) {
				for (int k = 0; k < len - 2; k++) {
					final int idx = start + k * st;
					cm[idx] = f1 * cm[idx] + f2 * cm[idx + st] + f3 * cm[idx + 2 * st];
				}
			}
			for (final int start : grid.lineStarts[d]) {
				for (int k = len - 1; k >= 2; k--) {
					final int idx = start + k * st;
					cm[idx] = f3 * cm[idx - 2 * st] + f2 * cm[idx - st] + f1 * cm[idx];
				}
			}
		}
	}

	/**
	 * Applies a morphological dilation (if {@code max}) or erosion with a box
	 * of three samples per axis in place, as in {@code Utils.dilate} and
	 * {@code Utils.erode}.
	 */
	private static void morph(final float[] cm, final Grid grid,
		final boolean max)
	{
		for (int d = 0; d < grid.dims.length; d++) {
			final int len = (int) grid.dims[d];
			final int st = grid.strides[d];
			for (final int start : grid.lineStarts[d]) {
				for (int k = 0; k < len - 1; k++) {
					final int idx = start + k * st;
					cm[idx] = max ? Math.max(cm[idx], cm[idx + st]) : Math.min(cm[idx],
						cm[idx + st]);
				}
			}
			for (final int start : grid.lineStarts[d]) {
				for (int k = len - 1; k >= 1; k--) {
					final int idx = start + k * st;
					cm[idx] = max ? Math.max(cm[idx - st], cm[idx]) : Math.min(cm[idx -
						st], cm[idx]);
				}
			}
		}
	}

	/** Scales the matrix so that its maximum becomes one. */
	private static void normalize(final float[] cm) {
		float max = 0.0f;
		for (final float v : cm) {
			if (max < v) max = v;
		}
		if (max <= 0.0 || max == 1.00) return;
		final float alpha = 1.00f / max;
		for (int i = 0; i < cm.length; i++) {
			cm[i] = alpha * cm[i];
		}
	}

	/**
	 * Clears the confidence matrix except for the components containing
	 * predefined foreground samples and, unless {@code sizeFactorToKeep} is
	 * zero, the largest component and every component with
	 * {@code size * sizeFactorToKeep >= largestSize}, as in
	 * {@code SioxSegmentator.keepOnlyLargeComponents}.
	 */
	private static void keepOnlyLargeComponents(final float[] cm,
		final Grid grid, final float threshold, final double sizeFactorToKeep,
		final boolean[] predefinedFg)
	{
		final int[] labelField = new int[cm.length];
		Arrays.fill(labelField, -1);
		final IntStack stack = new IntStack();
		final List<Integer> labelSizes = new ArrayList<>();
		final Set<Integer> predefinedLabels = new HashSet<>();
		int curLabel = 0;
		int maxRegion = 0;
		int maxBlob = 0;
		for (int i = 0; i < cm.length; i++) {
			int regionCount = 0;
			if (labelField[i] == -1 && cm[i] >= threshold) {
				regionCount = depthFirstSearch(cm, labelField, i, threshold,
					curLabel++, grid, stack);
				labelSizes.add(regionCount);
			}
			if (labelField[i] != -1 && predefinedFg[i]) {
				predefinedLabels.add(labelField[i]);
			}
			if (regionCount > maxRegion) {
				maxRegion = regionCount;
				maxBlob = curLabel - 1;
			}
		}

		for (int i = 0; i < cm.length; i++) {
			final int label = labelField[i];
			if (label == -1) continue;
			if (sizeFactorToKeep == 0) {
				cm[i] = predefinedLabels.contains(label)
					? CERTAIN_FOREGROUND_CONFIDENCE : CERTAIN_BACKGROUND_CONFIDENCE;
			}
			else if (labelSizes.get(label) * sizeFactorToKeep < maxRegion &&
				!predefinedLabels.contains(label))
			{
				cm[i] = CERTAIN_BACKGROUND_CONFIDENCE;
			}
			else if (label == maxBlob) {
				cm[i] = CERTAIN_FOREGROUND_CONFIDENCE;
			}
		}
	}

	/** Labels the face-connected component of samples at or above threshold. */
	private static int depthFirstSearch(final float[] cm, final int[] labelField,
		final int i, final float threshold, final int curLabel, final Grid grid,
		final IntStack stack)
	{
		int componentSize = 0;
		if (labelField[i] == -1 && cm[i] >= threshold) {
			labelField[i] = curLabel;
			++componentSize;
			stack.push(i);
		}
		while (!stack.isEmpty()) {
			final int pos = stack.pop();
			for (int d = 0; d < grid.dims.length; d++) {
				final int c = grid.coord(pos, d);
				for (int dir = -1; dir <= 1; dir += 2) {
					if (c + dir < 0 || c + dir >= grid.dims[d]) continue;
					final int nb = pos + dir * grid.strides[d];
					if (labelField[nb] == -1 && cm[nb] >= threshold) {
						labelField[nb] = curLabel;
						++componentSize;
						stack.push(nb);
					}
				}
			}
		}
		return componentSize;
	}

	/**
	 * Marks as foreground every sample connected to a foreground sample through
	 * samples of (almost) the same color as that sample, as in
	 * {@code SioxSegmentator.fillColorRegions}.
	 */
	private static void fillColorRegions(final float[] cm, final float[][] lab,
		final Grid grid)
	{
		final int[] labelField = new int[cm.length];
		Arrays.fill(labelField, -1);
		final IntStack stack = new IntStack();
		for (int i = 0; i < cm.length; i++) {
			if (labelField[i] != -1 || cm[i] < UNKNOWN_REGION_CONFIDENCE) continue;
			final float[] origColor = lab[i];
			final int curLabel = i + 1;
			labelField[i] = curLabel;
			cm[i] = CERTAIN_FOREGROUND_CONFIDENCE;
			stack.push(i);
			while (!stack.isEmpty()) {
				final int pos = stack.pop();
				for (int d = 0; d < grid.dims.length; d++) {
					final int c = grid.coord(pos, d);
					for (int dir = -1; dir <= 1; dir += 2) {
						if (c + dir < 0 || c + dir >= grid.dims[d]) continue;
						final int nb = pos + dir * grid.strides[d];
						if (labelField[nb] == -1 && (float) Math.sqrt(sqrDist(lab[nb],
							origColor)) < 1.0)
						{
							labelField[nb] = curLabel;
							cm[nb] = CERTAIN_FOREGROUND_CONFIDENCE;
							stack.push(nb);
						}
					}
				}
			}
		}
	}

	// -- Helper classes --

	/** Flat-indexing geometry of an n-dimensional image. */
	private static final class Grid {

		private final long[] dims;
		private final int[] strides;
		/** Flat index of the first sample of every line along each axis. */
		private final int[][] lineStarts;

		Grid(final long[] dims, final int n) {
			this.dims = dims;
			strides = new int[dims.length];
			long stride = 1;
			for (int d = 0; d < dims.length; d++) {
				strides[d] = (int) stride;
				stride *= dims[d];
			}
			if (stride != n) {
				throw new IllegalArgumentException(
					"Dimensions do not match data size");
			}
			lineStarts = new int[dims.length][];
			for (int d = 0; d < dims.length; d++) {
				final int[] starts = new int[dims[d] == 0 ? 0 : (int) (n / dims[d])];
				int k = 0;
				for (int i = 0; i < n; i++) {
					if (coord(i, d) == 0) starts[k++] = i;
				}
				lineStarts[d] = starts;
			}
		}

		int coord(final int pos, final int d) {
			return (int) ((pos / strides[d]) % dims[d]);
		}
	}

	/** Hash key identifying a CIELAB color exactly. */
	private static final class ColorKey {

		private final float l, a, b;

		ColorKey(final float[] lab) {
			l = lab[0];
			a = lab[1];
			b = lab[2];
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof ColorKey)) return false;
			final ColorKey k = (ColorKey) o;
			return Float.compare(l, k.l) == 0 && Float.compare(a, k.a) == 0 &&
				Float.compare(b, k.b) == 0;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(new float[] { l, a, b });
		}
	}

	/** Growable stack of {@code int} values. */
	private static final class IntStack {

		private int[] data = new int[64];
		private int size;

		void push(final int v) {
			if (size == data.length) data = Arrays.copyOf(data, size * 2);
			data[size++] = v;
		}

		int pop() {
			return data[--size];
		}

		boolean isEmpty() {
			return size == 0;
		}
	}
}
