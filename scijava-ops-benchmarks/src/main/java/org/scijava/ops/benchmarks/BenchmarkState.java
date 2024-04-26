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

import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;
import org.openjdk.jmh.annotations.*;
import org.scijava.Context;
import org.scijava.annotations.Index;
import org.scijava.annotations.IndexItem;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Plugin;

/**
 * {@link State} used in SciJava Ops framework benchmarks
 *
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
@State(Scope.Benchmark)
public class BenchmarkState {

	// Ops framework constructs.
	private Context ctx;
	public OpService ops;
	public OpEnvironment env;

	// Data containers for single numerical values.
	public byte[] theByte;
	public double[] theDouble;

	// Receptacles for storing calculation results.
	public byte[] byteResult;
	public double[] doubleResult;

	@Setup(Level.Trial)
	public void setUpInvocation() {
		// Set up ImageJ Ops.
		ctx = new Context(OpService.class);
		ops = ctx.service(OpService.class);
		// Set up SciJava Ops.
		env = OpEnvironment.build();
		// Allocate input data.
		theByte = new byte[1];
		theDouble = new double[1];
	}

	@TearDown(Level.Invocation)
	public void tearDownInvocation() {
		ctx.dispose();
	}
}
