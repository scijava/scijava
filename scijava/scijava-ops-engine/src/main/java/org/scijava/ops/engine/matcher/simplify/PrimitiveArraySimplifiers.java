/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.matcher.simplify;

import java.util.function.Function;

import org.scijava.collections.ObjectArray;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpHints;

/**
 * A collection of Ops for simplifying and focusing primitive arrays
 *
 * @author Gabriel Selzer
 */
public class PrimitiveArraySimplifiers<N extends Number> implements
	OpCollection
{

	// -- Object simplifiers -- //
		@OpHints(hints = { Simplification.FORBIDDEN })
		@OpField(names = "simplify")
		public final Function<N[], ObjectArray<Number>> byteArrSimplifier = arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			// TODO: Why doesn't System.arraycopy work?
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	// -- Primitive simplifiers -- //

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "simplify")
	public final Function<byte[], ObjectArray<Number>> bytePrimitiveArraySimplifier =
		arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			// TODO: Why doesn't System.arraycopy work?
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "simplify")
	public final Function<short[], ObjectArray<Number>> shortPrimitiveArraySimplifier =
		arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			// TODO: Why doesn't System.arraycopy work?
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "simplify")
	public final Function<int[], ObjectArray<Number>> intPrimitiveArraySimplifier = arr -> {
		var oa = new ObjectArray<>(Number.class, arr.length);
		// TODO: Why doesn't System.arraycopy work?
		for (int i = 0; i < arr.length; i++) {
			oa.set(i, arr[i]);
		}
		return oa;
	};

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "simplify")
	public final Function<long[], ObjectArray<Number>> longPrimitiveArraySimplifier =
		arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			// TODO: Why doesn't System.arraycopy work?
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "simplify")
	public final Function<float[], ObjectArray<Number>> floatPrimitiveArraySimplifier =
		arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			// TODO: Why doesn't System.arraycopy work?
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "simplify")
	public final Function<double[], ObjectArray<Number>> doublePrimitiveArraySimplifier =
		arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			// TODO: Why doesn't System.arraycopy work?
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	// -- Object Focusers -- //

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Byte[]> byteArrFocuser = o -> o
		.stream().map(b -> b == null ? null : b.byteValue()).toArray(Byte[]::new);

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Integer[]> intArrFocuser = o -> o
		.stream().map(i -> i == null ? null : i.intValue()).toArray(Integer[]::new);

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Short[]> shortArrFocuser = o -> o
		.stream().map(s -> s == null ? null : s.shortValue()).toArray(Short[]::new);

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Long[]> longArrFocuser = o -> o
		.stream().map(l -> l == null ? null : l.longValue()).toArray(Long[]::new);

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Float[]> floatArrFocuser = o -> o
		.stream().map(f -> f == null ? null : f.floatValue()).toArray(Float[]::new);

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Double[]> doubleArrFocuser = o -> o
		.stream().map(d -> d == null ? null : d.doubleValue()).toArray(
			Double[]::new);

	// -- Primitive Focusers -- //

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, byte[]> bytePrimitiveArrFocuser =
		o -> {
			byte[] arr = new byte[o.size()];
			for (int i = 0; i < o.size(); i++) {
				arr[i] = o.get(i).byteValue();
			}
			return arr;
		};

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, short[]> shortPrimitiveArrFocuser =
		o -> {
			short[] arr = new short[o.size()];
			for (int i = 0; i < o.size(); i++) {
				arr[i] = o.get(i).shortValue();
			}
			return arr;
		};

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, int[]> intPrimitiveArrFocuser =
		o -> {
			int[] arr = new int[o.size()];
			for (int i = 0; i < o.size(); i++) {
				arr[i] = o.get(i).intValue();
			}
			return arr;
		};

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, long[]> longPrimitiveArrFocuser =
		o -> {
			long[] arr = new long[o.size()];
			for (int i = 0; i < o.size(); i++) {
				arr[i] = o.get(i).longValue();
			}
			return arr;
		};

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, float[]> floatPrimitiveArrFocuser =
		o -> {
			float[] arr = new float[o.size()];
			for (int i = 0; i < o.size(); i++) {
				arr[i] = o.get(i).floatValue();
			}
			return arr;
		};

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, double[]> doublePrimitiveArrFocuser =
		o -> {
			double[] arr = new double[o.size()];
			for (int i = 0; i < o.size(); i++) {
				arr[i] = o.get(i).doubleValue();
			}
			return arr;
		};

}
