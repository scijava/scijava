/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

package net.imagej.ops2.filter.mean;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Optional;

/**
 * Filters an input image, taking the mean of all pixels in a neighborhood
 * around each sample in the input data.
 * 
 * @author Jonathan Hale (University of Konstanz)
 * @author Gabriel Selzer
 * @param <T>
 *            type
 *@implNote op names='filter.mean'
 */
public class DefaultMeanFilter<T, V> implements
		Computers.Arity3<RandomAccessibleInterval<T>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<V>> {

	@OpDependency(name = "stats.mean")
	private Computers.Arity1<Iterable<T>, V> statsOp;

	@OpDependency(name = "map.neighborhood")
	private Computers.Arity3<RandomAccessibleInterval<T>, Shape, Computers.Arity1<Iterable<T>, V>, RandomAccessibleInterval<V>> mapper;

	/**
	 * TODO
	 *
	 * @param input the input image
	 * @param inputNeighborhoodShape the shape of the {@link Neighborhood} that
	 *                               each mean will be computed from
	 * @param outOfBoundsFactory defines the values used in the
	 *                           computation at locations outside the image (required = false)
	 * @param output buffer image used to store computation output
	 */
	@Override
	public void compute( //
		final RandomAccessibleInterval<T> input, //
		final Shape inputNeighborhoodShape, //
		@Optional OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory, //
		final RandomAccessibleInterval<V> output //
	) {
		if (outOfBoundsFactory == null)
			outOfBoundsFactory = new OutOfBoundsMirrorFactory<>(
					OutOfBoundsMirrorFactory.Boundary.SINGLE);
		RandomAccessibleInterval<T> extended = Views.interval((Views.extend(input,
				outOfBoundsFactory)), input);
		mapper.compute(extended, inputNeighborhoodShape, statsOp, output);
	}
}
