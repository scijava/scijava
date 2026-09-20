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

package org.scijava.ops.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.scijava.ops.api.Hints;

import java.util.concurrent.TimeUnit;

import static org.scijava.ops.benchmarks.OpsToBenchmark.*;

/**
 * Tests relative performance of ImageJ Ops and SciJava Ops.
 *
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class Benchmarks {

	private static final Hints NO_CACHE = new Hints("cache.IGNORE");

	// -- Benchmarks -- //

	/**
	 * Baseline benchmark: static code execution without Ops framework.
	 *
	 * @param state the state between benchmarks
	 */
	@Benchmark
	public void noOps(BenchmarkState state) {
		incrementByte(state.theByte);
	}

	/**
	 * Static code execution (no Ops framework), adapting inplace operation to a
	 * function.
	 *
	 * @param state the state between benchmarks
	 */
	@Benchmark
	public void noOpsAdapted(BenchmarkState state) {
		// Copy input data into result object, which will be mutated.
		byte[] result = { state.theByte[0] };

		// Mutate the result.
		incrementByte(result);

		// Store the result into the designated receptacle.
		state.byteResult = result;
	}

	/**
	 * Static code execution (no Ops framework), converting input data between
	 * double and byte.
	 *
	 * @param state the state between benchmarks
	 */
	@Benchmark
	public void noOpsConverted(BenchmarkState state) {
		// Convert double value to byte.
		byte[] convertedByte = toByte(state.theDouble);

		// Call static method on converted byte.
		incrementByte(convertedByte);

		// Convert byte back to double.
		double[] reconvertedDouble = toDouble(convertedByte);

		// Copy result back to the original double receptacle.
		state.theDouble[0] = reconvertedDouble[0];
	}

	/**
	 * Static code execution (no Ops framework), doing both adaptation as
	 * {@link #noOpsAdapted} and conversion as {@link #noOpsConverted}.
	 *
	 * @param state the state between benchmarks
	 */
	@Benchmark
	public void noOpsAdaptedAndConverted(BenchmarkState state) {
		// Convert double value to byte.
		byte[] convertedByte = toByte(state.theDouble);

		// Mutate the converted object. We can do this directly because it is a
		// copy, not a wrapper/view.
		incrementByte(convertedByte);

		// Convert byte back to double, and store the result into the designated
		// receptacle.
		state.doubleResult = toDouble(convertedByte);
	}

	/**
	 * ImageJ Ops.
	 *
	 * @param state the state between benchmarks
	 */
	@Benchmark
	public void ijOps(final BenchmarkState state) {
		state.ops.run("benchmark.increment", state.theByte);
	}

	/**
	 * SciJava Ops, default behavior (caching included).
	 *
	 * @param state the state between benchmarks
	 */
	@Benchmark
	public void sjOpsWithCache(final BenchmarkState state) {
		state.env.op("benchmark.increment").input(state.theByte).mutate();
	}

	/**
	 * SciJava Ops, caching disabled.
	 *
	 * @param state the state between benchmarks
	 */
	@Benchmark
	public void sjOps(final BenchmarkState state) {
		state.env.op("benchmark.increment", NO_CACHE).input(state.theByte)
			.mutate();
	}

	/**
	 * SciJava Ops, adapting inplace Op to a function, caching disabled.
	 *
	 * @param state the state between benchmarks
	 */
	@Benchmark
	public void sjOpsAdapted(final BenchmarkState state) {
		state.byteResult = state.env.op("benchmark.increment", NO_CACHE).input(
			state.theByte).outType(byte[].class).apply();
	}

	/**
	 * SciJava Ops, converting input data between double and byte, caching
	 * disabled.
	 *
	 * @param state the state between benchmarks
	 */
	@Benchmark
	public void sjOpsConverted(final BenchmarkState state) {
		state.env.op("benchmark.increment", NO_CACHE).input(state.theDouble)
			.mutate();
	}

	/**
	 * SciJava Ops, doing both adaptation as {@link #sjOpsAdapted} and conversion
	 * as {@link #sjOpsConverted}.
	 *
	 * @param state the state between benchmarks
	 */
	@Benchmark
	public void sjOpsConvertedAndAdapted(final BenchmarkState state) {
		state.doubleResult = state.env.op("benchmark.increment", NO_CACHE).input(
			state.theDouble).outType(double[].class).apply();
	}
}
