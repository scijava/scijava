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

package org.scijava.ops.image.features.tamura2d;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Implementation of Tamura's Directionality Feature
 *
 * @author Andreas Graumann (University of Konstanz)
 * @param <I>
 * @param <O>
 * @implNote op names='features.tamura.directionality'
 */
public class DefaultDirectionalityFeature<I extends RealType<I>, O extends RealType<O>>
	implements Computers.Arity2<RandomAccessibleInterval<I>, Integer, O>
{

	@OpDependency(name = "image.histogram")
	private BiFunction<Iterable<DoubleType>, Integer, Histogram1d<DoubleType>> histOp;
	@OpDependency(name = "stats.stdDev")
	private Function<Iterable<LongType>, DoubleType> stdOp;
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, I, Img<I>> imgCreator;

	@SuppressWarnings("unchecked")
	/**
	 * TODO
	 *
	 * @param input
	 * @param histogramSize
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> input,
		@Nullable Integer histogramSize, final O output)
	{
		if (input.numDimensions() != 2) throw new IllegalArgumentException(
			"Only 2 dimensional images allowed!");
		if (histogramSize == null) histogramSize = 16;

		// List to store all directions occurring within the image on borders
		ArrayList<DoubleType> dirList = new ArrayList<>();

		// Dimension of input region
		long[] dims = new long[input.numDimensions()];
		input.dimensions(dims);

		// create image for derivations in x and y direction
		I imgType = Views.iterable(input).firstElement();
		Img<I> derX = imgCreator.apply(input, imgType);
		Img<I> derY = imgCreator.apply(input, imgType);

		// calculate derivations in x and y direction
		PartialDerivative.gradientCentralDifference2(Views.extendMirrorSingle(
			input), derX, 0);
		PartialDerivative.gradientCentralDifference2(Views.extendMirrorSingle(
			input), derY, 1);

		// calculate theta at each position: theta = atan(dX/dY) + pi/2
		Cursor<I> cX = derX.cursor();
		Cursor<I> cY = derY.cursor();

		// for each position calculate magnitude and direction
		while (cX.hasNext()) {
			cX.next();
			cY.next();

			double dx = cX.get().getRealDouble();
			double dy = cY.get().getRealDouble();

			double dir = 0.0;
			double mag = 0.0;

			mag = Math.sqrt(dx * dx + dy * dy);

			if (dx != 0 && mag > 0.0) {
				dir = Math.atan(dy / dx) + Math.PI / 2;
				dirList.add(new DoubleType(dir));
			}
		}

		// No directions: output is zero
		if (dirList.isEmpty()) {
			output.setReal(0.0);
		}
		// Otherwise compute histogram over all occurring directions
		// and calculate inverse second moment on it as output
		else {
			Histogram1d<DoubleType> hist = histOp.apply(dirList, histogramSize);
			double std = stdOp.apply(hist).getRealDouble();
			output.setReal(1 / std);
		}
	}
}
