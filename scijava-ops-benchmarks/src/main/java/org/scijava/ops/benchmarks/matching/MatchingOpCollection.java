/*-
 * #%L
 * Benchmarks for the SciJava Ops framework.
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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

package org.scijava.ops.benchmarks.matching;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.basictypeaccess.ByteAccess;
import net.imglib2.img.basictypeaccess.DoubleAccess;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Class containing Ops used in benchmarking
 *
 * @author Gabriel Selzer
 */
public class MatchingOpCollection {

	/**
	 * @param in the data to input to our function
	 * @param d the value to add to each element in the input
	 * @param out the preallocated storage buffer
	 * @implNote op name="benchmark.match",type=Computer
	 */
	public static void op( //
		final RandomAccessibleInterval<DoubleType> in, //
		final Double d, //
		final RandomAccessibleInterval<DoubleType> out //
	) {
		LoopBuilder.setImages(in, out).multiThreaded().forEachPixel((i, o) -> o.set(
			i.get() + d));
	}

	/**
	 * @param in the {@link RandomAccessibleInterval} containing {@link ByteType}s
	 * @return a {@link RandomAccessibleInterval} wrapping {@code in}.
	 * @implNote op name="engine.convert", type=Function, priority='1000.'
	 */
	public static RandomAccessibleInterval<DoubleType> toDoubleType(
		RandomAccessibleInterval<ByteType> in)
	{
		return Converters.convert(in, sampler -> new DoubleType(new DoubleAccess() {

			@Override
			public double getValue(int index) {
				return sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, double value) {
				sampler.get().setReal(value);
			}
		}));
	}

	/**
	 * @param in the {@link RandomAccessibleInterval} containing
	 *          {@link DoubleType}s
	 * @return a {@link RandomAccessibleInterval} wrapping {@code in}.
	 * @implNote op name="engine.convert", type=Function, priority='1000.'
	 */
	public static RandomAccessibleInterval<ByteType> toByteType(
		RandomAccessibleInterval<DoubleType> in)
	{
		return Converters.convert(in, sampler -> new ByteType(new ByteAccess() {

			@Override
			public byte getValue(int index) {
				return (byte) sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, byte value) {
				sampler.get().setReal(value);
			}
		}));
	}

}
