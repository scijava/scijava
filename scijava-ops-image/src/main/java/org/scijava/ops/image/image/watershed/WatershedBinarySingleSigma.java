/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.image.watershed;

import java.util.function.BiFunction;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;

/**
 * <p>
 * The Watershed algorithm segments and labels a grayscale image analogous to a
 * heightmap. In short, a drop of water following the gradient of an image flows
 * along a path to finally reach a local minimum.
 * </p>
 * <p>
 * Lee Vincent, Pierre Soille, Watersheds in digital spaces: An efficient
 * algorithm based on immersion simulations, IEEE Trans. Pattern Anal. Machine
 * Intell., 13(6) 583-598 (1991)
 * </p>
 * <p>
 * Input is a binary image with arbitrary number of dimensions. The heightmap is
 * calculated by an inverse distance transform, which can optionally be smoothed
 * with an gaussian filter with parameter sigma to prevent having many small
 * segments in the result. It needs to be defined whether a neighborhood with
 * eight- or four-connectivity (respective to 2D) is used. A binary image can be
 * set as mask which defines the area where computation shall be done. It may
 * make sense to use the input as mask as well. If desired, the watersheds are
 * drawn and labeled as 0. Otherwise the watersheds will be labeled as one of
 * their neighbors.
 * </p>
 * <p>
 * Output is a labeling of the different catchment basins.
 * </p>
 *
 * @param <T> element type of input
 * @param <B> element type of mask
 * @author Simon Schmid (University of Konstanz)
 * @implNote op names='image.watershed'
 */
public class WatershedBinarySingleSigma<T extends BooleanType<T>, B extends BooleanType<B>>
	implements
	Computers.Arity5<RandomAccessibleInterval<T>, Boolean, Boolean, Double, RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>>
{

	// @SuppressWarnings("rawtypes")
	// private UnaryFunctionOp<Interval, ImgLabeling> createOp;
	@OpDependency(name = "image.distanceTransform")
	private Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<FloatType>> distanceTransformer;
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> imgCreator;
	@OpDependency(name = "image.invert")
	private Computers.Arity1<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> imgInverter;
	@OpDependency(name = "filter.gauss")
	private Computers.Arity2<RandomAccessibleInterval<FloatType>, Double, RandomAccessibleInterval<FloatType>> gaussOp;
	@OpDependency(name = "image.watershed")
	private Computers.Arity4<RandomAccessibleInterval<FloatType>, Boolean, Boolean, RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>> watershedOp;

	/**
	 * TODO
	 *
	 * @param in
	 * @param useEightConnectivity
	 * @param drawWatersheds
	 * @param sigma
	 * @param mask
	 * @param outputLabeling
	 */
	@Override
	public void compute( //
		final RandomAccessibleInterval<T> in, //
		final Boolean useEightConnectivity, //
		final Boolean drawWatersheds, //
		final Double sigma, //
		@Nullable final RandomAccessibleInterval<B> mask, //
		final ImgLabeling<Integer, IntType> outputLabeling //
	) {
		// ensure the params satisfy the requirements of the op (taken from the old
		// implementation)
		boolean conformed = sigma >= 0;
		if (!conformed) throw new IllegalArgumentException(
			"sigma must be non-negative!");
		if (mask != null) {
			conformed &= Intervals.equalDimensions(mask, in);
		}
		if (!conformed) throw new IllegalArgumentException(
			"mask must be of the same dimensions as the input!");

		// compute distance transform
		final RandomAccessibleInterval<FloatType> distMap = imgCreator.apply(in,
			new FloatType());
		distanceTransformer.compute(in, distMap);
		final RandomAccessibleInterval<FloatType> invertedDT = imgCreator.apply(in,
			new FloatType());
		imgInverter.compute(distMap, invertedDT);
		final RandomAccessibleInterval<FloatType> gauss = imgCreator.apply(in,
			new FloatType());
		gaussOp.compute(invertedDT, sigma, gauss);
		// run the default watershed
		watershedOp.compute(gauss, useEightConnectivity, drawWatersheds, mask,
			outputLabeling);
	}

}
