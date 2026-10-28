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

package org.scijava.ops.image.image.invert;

import java.math.BigInteger;

import org.scijava.ops.image.util.UnboundedIntegerType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.Unsigned128BitType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.util.Util;

import org.scijava.function.Computers;

public class Inverters<T extends RealType<T>, I extends IntegerType<I>> {

	/**
	 * @input input
	 * @input min
	 * @input max
	 * @container invertedOutput
	 * @implNote op names='image.invert'
	 */
	public final Computers.Arity3<RandomAccessibleInterval<T>, T, T, RandomAccessibleInterval<T>> delegatorInvert =
		(input, min, max, output) -> {

			// HACK: Some types are small enough that they can run the faster, double
			// math
			// invert.
			// Others (fortunately all in this category are IntegerTypes)
			// must use the slower BigInteger inverter.
			// TODO: Think of a better solution.
			final var copy = Util.getTypeFromInterval(input).createVariable();
            var typeTooBig = false;
			// if the type is an integer type that can handle Long.MAX_VALUE
			// then we have to run the slow version
			if (copy instanceof IntegerType) {
				((IntegerType) copy).setInteger(Long.MAX_VALUE);
				if (((IntegerType) copy).getIntegerLong() == Long.MAX_VALUE)
					typeTooBig = true;
			}

			if (typeTooBig) {

				computeIIInteger(input, min, max, output);

			}
			else {

				computeII(input, min, max, output);

			}
		};

	/**
	 * @input input
	 * @container invertedOutput
	 * @implNote op names='image.invert'
	 */
	public final Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> simpleInvert =
		(input, output) -> {
            var type = Util.getTypeFromInterval(input);
			delegatorInvert.compute(input, minValue(type), maxValue(type), output);
		};

	public void computeII(final RandomAccessibleInterval<T> input, final T min,
		final T max, final RandomAccessibleInterval<T> output)
	{
		final var minDouble = min.getRealDouble();
		final var maxDouble = max.getRealDouble();
		final var minMax = min.getRealDouble() + max.getRealDouble();

		LoopBuilder.setImages(input, output).multiThreaded().forEachPixel((in,
			out) -> {
			if (minMax - in.getRealDouble() <= out.getMinValue()) {
				out.setReal(out.getMinValue());
			}
			else if (minMax - in.getRealDouble() >= out.getMaxValue()) {
				out.setReal(out.getMaxValue());
			}
			else out.setReal(minMax - in.getRealDouble());
		});
	}

	// HACK: this will only be run when our image is of a type too big for default
	// inverts.
	// TODO: Think of a better solution.
	@SuppressWarnings("unchecked")
	public void computeIIInteger(final RandomAccessibleInterval<T> input,
		final T min, final T max, final RandomAccessibleInterval<T> output)
	{

		final var minValue = getBigInteger(min);
		final var maxValue = getBigInteger(max);
		final var minMax = minValue.add(maxValue);

		LoopBuilder.setImages(input, output).multiThreaded().forEachPixel((in,
			out) -> {
            var inverted = minMax.subtract(getBigInteger(in));

			if (inverted.compareTo(getBigInteger(minValue(out))) <= 0) out.set(
				minValue(out));
			else if (inverted.compareTo(getBigInteger(maxValue(out))) >= 0) out.set(
				maxValue(out));
			else setBigInteger(out, inverted);
		});

	}

	private BigInteger getBigInteger(T in) {
		if (in instanceof IntegerType) {
			return ((IntegerType) in).getBigInteger();
		}
		return BigInteger.valueOf((long) in.getRealDouble());
	}

	private void setBigInteger(T out, BigInteger bi) {
		if (out instanceof IntegerType) {
			((IntegerType) out).setBigInteger(bi);
			return;
		}
		out.setReal(bi.doubleValue());
		return;
	}

	public static <T extends RealType<T>> T minValue(T type) {
		// TODO: Consider making minValue an Op.
		final var min = type.createVariable();
		if (type instanceof UnboundedIntegerType) min.setReal(0);
		else min.setReal(min.getMinValue());
		return min;

	}

	public static <T extends RealType<T>> T maxValue(T type) {
		// TODO: Consider making maxValue an Op.
		final var max = type.createVariable();
		if (max instanceof Unsigned128BitType) {
			final var t = (Unsigned128BitType) max;
			t.set(t.getMaxBigIntegerValue());
		}
		else if (max instanceof UnsignedLongType) {
			final var t = (UnsignedLongType) max;
			t.set(t.getMaxBigIntegerValue());
		}
		else if (max instanceof UnboundedIntegerType) {
			max.setReal(0);
		}
		else {
			max.setReal(type.getMaxValue());
		}
		return max;
	}

}
