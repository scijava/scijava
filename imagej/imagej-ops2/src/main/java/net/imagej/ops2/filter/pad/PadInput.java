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

package net.imagej.ops2.filter.pad;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.Priority;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Op used to pad the image by extending the borders
 * 
 * @author bnorthan
 * @param <T>
 * @param <I>
 * @param <O>
 * @implNote op names='filter.padInput', priority='100.'
 */
public class PadInput<T extends ComplexType<T>, I extends RandomAccessibleInterval<T>, O extends RandomAccessibleInterval<T>>
		implements Functions.Arity3<I, Dimensions, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, O> {

	@OpDependency(name = "filter.padIntervalCentered")
	private BiFunction<I, Dimensions, O> paddingIntervalCentered;

	@Override
	@SuppressWarnings("unchecked")
	/**
	 * TODO
	 *
	 * @param input
	 * @param paddedDimensions
	 * @param outOfBoundsFactory The OutOfBoundsFactory used to extend the image
	 * @return the output
	 */
	public O apply(final I input, final Dimensions paddedDimensions,
			OutOfBoundsFactory<T, RandomAccessibleInterval<T>> obf) {

		if (obf == null) {
			obf = new OutOfBoundsConstantValueFactory<>(Util.getTypeFromInterval(input).createVariable());
		}

		Interval inputInterval = paddingIntervalCentered.apply(input, paddedDimensions);

		return (O) Views.interval(Views.extend(input, obf), inputInterval);
	}
}
