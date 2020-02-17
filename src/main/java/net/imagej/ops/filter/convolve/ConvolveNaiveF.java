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

package net.imagej.ops.filter.convolve;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Functions;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Convolves an image naively (no FFTs).
 */
@Plugin(type = Op.class, name = "filter.convolve", priority = Priority.HIGH + 1)
@Parameter(key = "input")
@Parameter(key = "kernel")
@Parameter(key = "outOfBoundsFactory")
@Parameter(key = "outType")
@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
public class ConvolveNaiveF<I extends RealType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K>>
		implements
		Functions.Arity4<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, O, RandomAccessibleInterval<O>> {

	@OpDependency(name = "filter.convolve")
	private Computers.Arity2<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>> convolver;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, RandomAccessibleInterval<O>> createOp;

	/**
	 * Create the output using the outFactory and outType if they exist. If these
	 * are null use a default factory and type
	 */
	@SuppressWarnings("unchecked")
	public RandomAccessibleInterval<O> createOutput(RandomAccessibleInterval<I> input,
			RandomAccessibleInterval<K> kernel, O outType) {

		// TODO can we remove this null check?
		if (outType == null) {

			// if the input type and kernel type are the same use this type
			if (Util.getTypeFromInterval(input).getClass() == Util.getTypeFromInterval(kernel).getClass()) {
				Object temp = Util.getTypeFromInterval(input).createVariable();
				outType = (O) temp;

			}
			// otherwise default to float
			else {
				Object temp = new FloatType();
				outType = (O) temp;
			}
		}

		return createOp.apply(input, outType.createVariable());
	}

	@Override
	public RandomAccessibleInterval<O> apply(final RandomAccessibleInterval<I> input,
			final RandomAccessibleInterval<K> kernel, OutOfBoundsFactory<I, RandomAccessibleInterval<I>> obf,
			final O outType) {

		// conforms only if the kernel is sufficiently small
		if (Intervals.numElements(kernel) <= 9)
			throw new IllegalArgumentException("The kernel is too small to perform computation!");

		RandomAccessibleInterval<O> out = createOutput(input, kernel, outType);

		// TODO can we remove this null check?
		if (obf == null) {
			obf = new OutOfBoundsConstantValueFactory<>(Util.getTypeFromInterval(input).createVariable());
		}

		// extend the input
		RandomAccessibleInterval<I> extendedIn = Views.interval(Views.extend(input, obf), input);

		OutOfBoundsFactory<O, RandomAccessibleInterval<O>> obfOutput = new OutOfBoundsConstantValueFactory<>(
				Util.getTypeFromInterval(out).createVariable());

		// extend the output
		RandomAccessibleInterval<O> extendedOut = Views.interval(Views.extend(out, obfOutput), out);

		// ops().filter().convolve(extendedOut, extendedIn, kernel);
		convolver.compute(extendedIn, kernel, extendedOut);

		return out;
	}

}

@Plugin(type = Op.class, name = "filter.convolve", priority = Priority.HIGH + 1)
@Parameter(key = "input")
@Parameter(key = "kernel")
@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
class SimpleConvolveNaiveF<I extends RealType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K>>
		implements
		BiFunction<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>> {
	
	@OpDependency(name = "filter.convolve")
	Functions.Arity4<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, O, RandomAccessibleInterval<O>> convolveOp;

	@Override
	public RandomAccessibleInterval<O> apply(RandomAccessibleInterval<I> t, RandomAccessibleInterval<K> u) {
		return convolveOp.apply(t, u, null, null);
	}
}
