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

import net.imagej.ops.Op;
import net.imagej.ops.special.inplace.AbstractUnaryInplaceOp;

import org.scijava.function.Computers;
import org.scijava.function.Producer;
import org.scijava.plugin.Plugin;

import java.util.function.Function;

/**
 * Class containing Ops used in benchmarking.
 *
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
public class OpsToBenchmark {

	/**
	 * Increments a byte value.
	 *
	 * @param data array containing the byte to increment
	 * @implNote op name="benchmark.increment", type=Inplace1
	 */
	public static void incrementByte(final byte[] data) {
		data[0]++;
	}

	/** Increment Op wrapper for ImageJ Ops. */
	@Plugin(type = Op.class, name = "benchmark.increment")
	public static class IncrementByteOp extends AbstractUnaryInplaceOp<byte[]>
		implements Op
	{

		@Override
		public void mutate(byte[] o) {
			incrementByte(o);
		}
	}

	/**
	 * Creates a byte value container.
	 *
	 * @output a newly allocated {@code byte} array of length 1
	 * @implNote op name="engine.create"
	 */
	public final Producer<byte[]> createByte = () -> new byte[1];

	/**
	 * Creates a byte value container.
	 *
	 * @input Template object from which to glean attributes (length) for the new
	 *        container.
	 * @output a newly allocated {@code byte} array of length 1
	 * @implNote op name="engine.create"
	 */
	public final Function<byte[], byte[]> createByteFromByte =
		template -> new byte[template.length];

	/**
	 * Converts a byte to a single double.
	 *
	 * @param b a single-element array containing a {@code byte}.
	 * @return a single-element {@code double} array with the casted value.
	 * @implNote op name="engine.convert", priority='1000.'
	 */
	public static double[] toDouble(byte[] b) {
		return new double[] { b[0] };
	}

	/**
	 * Converts a double to a byte.
	 *
	 * @param d a single-element array containing a {@code double}.
	 * @return a single-element {@code byte} array with the casted value.
	 * @implNote op name="engine.convert", priority='1000.'
	 */
	public static byte[] toByte(double[] d) {
		return new byte[] { (byte) d[0] };
	}

}
