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

package net.imagej.ops.filter.dog;

import java.util.function.Function;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Low-level difference of Gaussians (DoG) implementation which leans on other
 * ops to do the work.
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Curtis Rueden
 * @param <T>
 */
@Plugin(type = Op.class, name = "filter.DoG")
@Parameter(key = "input")
@Parameter(key = "gauss1")
@Parameter(key = "gauss2")
@Parameter(key = "output", type = ItemIO.BOTH)
public class DefaultDoG<T extends NumericType<T> & NativeType<T>> implements
		Computer3<RandomAccessibleInterval<T>, Computer<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>, Computer<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> {

	@OpDependency(name = "create.img")
	private Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> tmpCreator;
	
	@OpDependency(name = "math.subtract")
	private BiComputer<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> subtractor;

	@Override
	public void compute(final RandomAccessibleInterval<T> input,
			final Computer<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gauss1,
			final Computer<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gauss2,
			final RandomAccessibleInterval<T> output) {
		// input may potentially be translated
		final long[] translation = new long[input.numDimensions()];
		input.min(translation);

		final IntervalView<T> tmpInterval = Views
				.interval(Views.translate((RandomAccessible<T>) tmpCreator.apply(input), translation), output);

		gauss1.compute(input, tmpInterval);
		gauss2.compute(input, output);

		// TODO: is this safe?
		subtractor.compute(output, tmpInterval, output);
	}

}
