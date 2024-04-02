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
import net.imglib2.converter.readwrite.SamplerConverter;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.DoubleAccess;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;
import org.openjdk.jmh.annotations.*;
import org.scijava.ops.api.Hints;

import static org.scijava.ops.benchmarks.matching.MatchingOpCollection.op;

/**
 * Benchmark showcasing the performance of Op parameter conversion
 *
 * @author Gabriel Selzer
 */
public class BenchmarkConversion {

	private static final Hints HINTS = new Hints("cache.IGNORE");

	private static final SamplerConverter<? super ByteType, DoubleType> CONVERTER //
		= sampler -> new DoubleType(new DoubleAccess() {

			@Override
			public double getValue(int index) {
				return sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, double value) {
				sampler.get().setReal(value);
			}
		});

	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void runStatic(MatchingState state) {
		// manual conversion of simple input
		var in = Converters.convert( //
			(RandomAccessibleInterval<ByteType>) state.simpleIn, //
			CONVERTER //
		);
		// manual creation of simple output
		var out = ArrayImgs.doubles(in.dimensionsAsLongArray());

		// invoke Op statically
		op(in, 1.0, out);
	}

	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void runOpConverted(final MatchingState state) {
		var out = ArrayImgs.bytes(state.simpleIn.dimensionsAsLongArray());
		state.env.binary("benchmark.match", HINTS) //
			.input(state.simpleIn, (byte) 1) //
			.output(out) //
			.compute();
	}

	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void runOpConvertedAdapted(final MatchingState state) {
		state.env.binary("benchmark.match", HINTS) //
			.input(state.simpleIn, (byte) 1.0) //
			.apply();
	}

}
