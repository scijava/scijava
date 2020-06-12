/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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
package net.imagej.ops2.image.watershed;

import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Functions;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

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
 * segments in the result. Sigma must have the same dimension as the input
 * image. It needs to be defined whether a neighborhood with eight- or
 * four-connectivity (respective to 2D) is used. A binary image can be set as
 * mask which defines the area where computation shall be done. It may make
 * sense to use the input as mask as well. If desired, the watersheds are drawn
 * and labeled as 0. Otherwise the watersheds will be labeled as one of their
 * neighbors.
 * </p>
 * <p>
 * Output is a labeling of the different catchment basins.
 * </p>
 *
 * @param <T>
 *            element type of input
 * @param <B>
 *            element type of mask
 *
 * @author Simon Schmid (University of Konstanz)
 */
@Plugin(type = Op.class, name = "image.watershed")
@Parameter(key = "input")
@Parameter(key = "useEightConnectivity")
@Parameter(key = "drawWatersheds")
@Parameter(key = "sigma")
@Parameter(key = "mask")
@Parameter(key = "executorService")
@Parameter(key = "outputLabeling", itemIO = ItemIO.BOTH)
public class WatershedBinary<T extends BooleanType<T>, B extends BooleanType<B>> implements
		Computers.Arity6<RandomAccessibleInterval<T>, Boolean, Boolean, double[], RandomAccessibleInterval<B>, ExecutorService, ImgLabeling<Integer, IntType>> {

	// @SuppressWarnings("rawtypes")
	// private UnaryFunctionOp<Interval, ImgLabeling> createOp;

	@OpDependency(name = "image.distanceTransform")
	private Computers.Arity2<RandomAccessibleInterval<T>, ExecutorService, RandomAccessibleInterval<FloatType>> distanceTransformer;
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> imgCreator;
	@OpDependency(name = "image.invert")
	private Computers.Arity1<IterableInterval<FloatType>, IterableInterval<FloatType>> imgInverter;
	@OpDependency(name = "filter.gauss")
	private Computers.Arity3<RandomAccessibleInterval<FloatType>, ExecutorService, double[], RandomAccessibleInterval<FloatType>> gaussOp;
	@OpDependency(name = "image.watershed")
	private Computers.Arity4<RandomAccessibleInterval<FloatType>, Boolean, Boolean, RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>> watershedOp;

	@Override
	public void compute(final RandomAccessibleInterval<T> in, final Boolean useEightConnectivity,
			final Boolean drawWatersheds, final double[] sigma, final RandomAccessibleInterval<B> mask,
			final ExecutorService es, final ImgLabeling<Integer, IntType> out) {

		// make sure that the params conform to the requirements of the op (copied from
		// the old implementation)
		boolean conformed = sigma.length >= in.numDimensions();
		for (int i = 0; i < sigma.length; i++) {
			conformed &= sigma[i] >= 0;
		}
		if (!conformed)
			throw new IllegalArgumentException("Only non-negative sigmas allowed!");
		if (mask != null) {
			conformed &= Intervals.equalDimensions(mask, in);
		}
		if (!conformed)
			throw new IllegalArgumentException("Mask must be the same size as the input!");

		// compute distance transform
		final RandomAccessibleInterval<FloatType> distMap = imgCreator.apply(in, new FloatType());
		distanceTransformer.compute(in, es, distMap);
		final RandomAccessibleInterval<FloatType> invertedDT = imgCreator.apply(in, new FloatType());
		imgInverter.compute(Views.iterable(distMap), Views.iterable(invertedDT));
		final RandomAccessibleInterval<FloatType> gauss = imgCreator.apply(in, new FloatType());
		gaussOp.compute(invertedDT, es, sigma, gauss);
		// run the default watershed
		watershedOp.compute(gauss, useEightConnectivity, drawWatersheds, mask, out);
	}

}

@Plugin(type = Op.class, name = "image.watershed")
@Parameter(key = "input")
@Parameter(key = "useEightConnectivity")
@Parameter(key = "drawWatersheds")
@Parameter(key = "sigma")
@Parameter(key = "executorService")
@Parameter(key = "outputLabeling", itemIO = ItemIO.BOTH)
class WatershedBinaryMaskless<T extends BooleanType<T>, B extends BooleanType<B>> implements
		Computers.Arity5<RandomAccessibleInterval<T>, Boolean, Boolean, double[], ExecutorService, ImgLabeling<Integer, IntType>> {

	@OpDependency(name = "image.watershed")
	private Computers.Arity6<RandomAccessibleInterval<T>, Boolean, Boolean, double[], RandomAccessibleInterval<B>, ExecutorService, ImgLabeling<Integer, IntType>> watershedOp;

	@Override
	public void compute(RandomAccessibleInterval<T> in, Boolean useEightConnectivity, Boolean drawWatersheds,
			double[] sigma, ExecutorService es, @Mutable ImgLabeling<Integer, IntType> outputLabeling) {
		watershedOp.compute(in, useEightConnectivity, drawWatersheds, sigma, null, es, outputLabeling);

	}
}

@Plugin(type = Op.class, name = "image.watershed")
@Parameter(key = "input")
@Parameter(key = "useEightConnectivity")
@Parameter(key = "drawWatersheds")
@Parameter(key = "sigma")
@Parameter(key = "mask")
@Parameter(key = "executorService")
@Parameter(key = "outputLabeling", itemIO = ItemIO.OUTPUT)
class WatershedBinaryFunction<T extends BooleanType<T>, B extends BooleanType<B>> implements
		Functions.Arity6<RandomAccessibleInterval<T>, Boolean, Boolean, double[], RandomAccessibleInterval<B>, ExecutorService, ImgLabeling<Integer, IntType>> {

	@OpDependency(name = "image.watershed")
	private Computers.Arity6<RandomAccessibleInterval<T>, Boolean, Boolean, double[], RandomAccessibleInterval<B>, ExecutorService, ImgLabeling<Integer, IntType>> watershedOp;
	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	@Override
	public ImgLabeling<Integer, IntType> apply(RandomAccessibleInterval<T> in, Boolean useEightConnectivity,
			Boolean drawWatersheds, double[] sigma, RandomAccessibleInterval<B> mask, ExecutorService es) {
		ImgLabeling<Integer, IntType> outputLabeling = labelingCreator.apply(in, new IntType());
		watershedOp.compute(in, useEightConnectivity, drawWatersheds, sigma, mask, es, outputLabeling);
		return outputLabeling;
	}
}

@Plugin(type = Op.class, name = "image.watershed")
@Parameter(key = "input")
@Parameter(key = "useEightConnectivity")
@Parameter(key = "drawWatersheds")
@Parameter(key = "sigma")
@Parameter(key = "executorService")
@Parameter(key = "outputLabeling", itemIO = ItemIO.OUTPUT)
class WatershedBinaryFunctionMaskless<T extends BooleanType<T>, B extends BooleanType<B>>
		implements Functions.Arity5<RandomAccessibleInterval<T>, Boolean, Boolean, double[], ExecutorService, ImgLabeling<Integer, IntType>> {

	@OpDependency(name = "image.watershed")
	private Computers.Arity5<RandomAccessibleInterval<T>, Boolean, Boolean, double[], ExecutorService, ImgLabeling<Integer, IntType>> watershedOp;
	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	@Override
	public ImgLabeling<Integer, IntType> apply(RandomAccessibleInterval<T> in, Boolean useEightConnectivity,
			Boolean drawWatersheds, double[] sigma, ExecutorService es) {
		ImgLabeling<Integer, IntType> outputLabeling = labelingCreator.apply(in, new IntType());
		watershedOp.compute(in, useEightConnectivity, drawWatersheds, sigma, es, outputLabeling);
		return outputLabeling;
	}
}
