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

package org.scijava.ops.benchmarks.frameworks;

import net.imagej.ops.Ops;
import net.imagej.ops.special.inplace.AbstractUnaryInplaceOp;
import org.openjdk.jmh.annotations.*;
import org.scijava.ops.api.Hints;
import org.scijava.plugin.Plugin;

/**
 * Tests relative performance of ImageJ Ops and SciJava Ops.
 *
 * @author Gabriel Selzer
 */
public class BenchmarkFrameworks {

	private static final Hints HINTS = new Hints("cache.IGNORE");

	/**
	 * Baseline benchmark - static code execution
	 *
	 * @param state the state between benchmarks
	 */
	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void runStatic(FrameworksState state) {
		invertRaw(state.in);
	}

	/**
	 * SciJava Ops benchmark
	 *
	 * @param state the state between benchmarks
	 */
	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void sciJavaOps(final FrameworksState state) {
		state.env.unary("benchmark.invert", HINTS).input(state.in).mutate();
	}

	/**
	 * ImageJ Ops benchmark
	 *
	 * @param state the state between benchmarks
	 */
	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void imageJOps(final FrameworksState state) {
		state.ops.run("image.invert", new Object[] { state.in });
	}

	// -- Ops -- //

	/**
	 * @param data the data to invert
	 * @implNote op name="benchmark.invert",type=Inplace1
	 */
	public static void invertRaw(final byte[] data) {
		for (int i = 0; i < data.length; i++) {
			final int value = data[i] & 0xff;
			final int result = 255 - value;
			data[i] = (byte) result;
		}
	}

	@Plugin(type = Ops.Image.Invert.class)
	public static class InvertOp extends AbstractUnaryInplaceOp<byte[]> implements
		Ops.Image.Invert
	{

		@Override
		public void mutate(byte[] o) {
			invertRaw(o);
		}
	}

}
