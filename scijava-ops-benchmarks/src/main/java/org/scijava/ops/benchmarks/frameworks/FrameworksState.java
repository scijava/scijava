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

import net.imagej.ops.OpService;
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
 */
@State(Scope.Benchmark)
public class FrameworksState {

	public OpEnvironment env;
	public OpService ops;
	public byte[] in;

	private Context ctx;

	@Setup(Level.Invocation)
	public void setUpInvocation() {
		// Set up ImageJ Ops
		ctx = new Context(OpService.class);
		ops = ctx.service(OpService.class);
		// Set up SciJava Ops
		env = OpEnvironment.build();
		// Create input data
		in = createRawData(4000, 4000);
	}

	@TearDown(Level.Invocation)
	public void tearDownInvocation() {
		ctx.dispose();
	}

	private byte[] createRawData(final int w, final int h) {
		final int size = w * h;
		final int max = w + h;
		final byte[] data = new byte[size];
		int index = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				data[index++] = (byte) (255 * (x + y) / max);
			}
		}
		return data;
	}
}
