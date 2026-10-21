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

package org.scijava.ops.image.segment.dog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.SubpixelLocalization;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.concurrent.Parallelization;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

/**
 * Detects blob-like spots using a Difference-of-Gaussian (DoG) detector,
 * wrapping ImgLib2's {@link DogDetection}.
 * <p>
 * The image is filtered with Gaussians of standard deviation {@code sigma}
 * and {@code sigma × 2^(1/4)}; their difference approximates the
 * scale-normalized Laplacian of Gaussian, which responds most strongly to
 * spots of radius about {@code sigma × √n} in {@code n} dimensions (i.e., to
 * Gaussian spots of standard deviation {@code sigma}). Local extrema of the
 * DoG (over the 3^n neighborhood) whose normalized response exceeds the
 * threshold are reported as spots, optionally refined to subpixel accuracy by
 * fitting a quadratic to the DoG around each extremum (ImgLib2's
 * {@link SubpixelLocalization}).
 * </p>
 * <p>
 * The normalized response is the DoG value divided by {@code 2^(1/4) - 1}, so
 * that it approximates the scale-normalized Laplacian of Gaussian and is
 * roughly in units of image intensity.
 * </p>
 * <p>
 * This op provides the core of the DoG detection in Stephan Preibisch's
 * <a href="https://github.com/fiji/Descriptor_based_registration">Descriptor
 * based registration</a> plugin and in TrackMate's DoG detector, both of
 * which build on ImgLib2's {@link DogDetection} (BSD-2-Clause, by Tobias
 * Pietzsch and Stephan Preibisch). See Lowe, D. G., "Distinctive image
 * features from scale-invariant keypoints", IJCV 60(2):91-110 (2004).
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> image type
 * @implNote op names='segment.detectSpotsDoG'
 */
public class DefaultDetectSpotsDoG<T extends RealType<T> & NativeType<T>>
	implements
	Functions.Arity6<RandomAccessibleInterval<T>, Double, double[], Double, Boolean, Boolean, List<RealPoint>>
{

	/** Ratio between the two Gaussian scales of the DoG. */
	private static final double K = Math.pow(2, 0.25);

	/**
	 * Detects spots.
	 *
	 * @param input the image in which to detect spots
	 * @param sigma the scale of the spots to detect: the standard deviation of
	 *          the smaller Gaussian, in calibrated units
	 * @param calibration the physical size of a pixel along each dimension;
	 *          defaults to 1 for every dimension
	 * @param threshold the minimum normalized DoG response of a spot (see the
	 *          class description); defaults to 0, i.e. any extremum with a
	 *          strictly positive response
	 * @param brightSpots {@code true} (default) to detect bright spots on a
	 *          dark background, {@code false} to detect dark spots on a bright
	 *          background
	 * @param subpixel {@code true} (default) to refine spot positions to
	 *          subpixel accuracy, {@code false} to return pixel positions
	 * @return the spot positions, sorted in flat iteration order of the
	 *         pixel each spot was detected at (first dimension fastest)
	 */
	@Override
	public List<RealPoint> apply(final RandomAccessibleInterval<T> input,
		final Double sigma, @Nullable double[] calibration,
		@Nullable Double threshold, @Nullable Boolean brightSpots,
		@Nullable Boolean subpixel)
	{
		final var n = input.numDimensions();
		if (sigma == null || !(sigma > 0)) throw new IllegalArgumentException(
			"sigma must be positive");
		if (calibration == null) {
			calibration = new double[n];
			Arrays.fill(calibration, 1);
		}
		if (calibration.length != n) throw new IllegalArgumentException(
			"calibration must have one entry per dimension (" + n + ")");
		if (threshold == null) threshold = 0.0;
		if (brightSpots == null) brightSpots = true;
		if (subpixel == null) subpixel = true;

		// Note: Bright blobs are minima of the DoG computed by DogDetection.
		final var extremaType = brightSpots ? DogDetection.ExtremaType.MINIMA
			: DogDetection.ExtremaType.MAXIMA;
		final var dog = new DogDetection<>(Views.extendMirrorSingle(input), input,
			calibration, sigma, sigma * K, extremaType, threshold, true);
		dog.setExecutorService(Parallelization.getExecutorService());
		dog.setKeepDoGImg(true);
		final List<Point> peaks = new ArrayList<>(dog.getPeaks());
		final var dogImg = dogImage(dog);

		// Keep only peaks whose response strictly exceeds the threshold, which
		// discards the spurious non-strict extrema of flat regions.
		final var sign = brightSpots ? -1 : 1;
		final var ra = dogImg.randomAccess();
		final var minResponse = threshold;
		peaks.removeIf(p -> {
			ra.setPosition(p);
			return sign * ra.get().getRealDouble() / (K - 1) <= minResponse;
		});
		peaks.sort(FLAT_ORDER);

		final List<RealPoint> spots = new ArrayList<>(peaks.size());
		if (subpixel) {
			final var refined = refine(peaks, dogImg);
			for (final RealLocalizable r : refined)
				spots.add(new RealPoint(r));
		}
		else {
			for (final var p : peaks)
				spots.add(new RealPoint(p));
		}
		return spots;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static RandomAccessibleInterval<? extends RealType<?>> dogImage(
		final DogDetection<?> dog)
	{
		return (RandomAccessibleInterval) dog.getTypedDogDetection().dogImg;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<? extends RealLocalizable> refine(
		final List<Point> peaks,
		final RandomAccessibleInterval<? extends RealType<?>> dogImg)
	{
		// Note: These are the settings used by DogDetection.getSubpixelPeaks().
		final var spl = new SubpixelLocalization(dogImg.numDimensions());
		spl.setAllowMaximaTolerance(true);
		spl.setMaxNumMoves(10);
		spl.setNumThreads(Runtime.getRuntime().availableProcessors());
		return spl.process(peaks, dogImg, dogImg);
	}

	private static final Comparator<Point> FLAT_ORDER = (p1, p2) -> {
		for (var d = p1.numDimensions() - 1; d >= 0; d--) {
			final var c = Long.compare(p1.getLongPosition(d), p2.getLongPosition(d));
			if (c != 0) return c;
		}
		return 0;
	};
}
