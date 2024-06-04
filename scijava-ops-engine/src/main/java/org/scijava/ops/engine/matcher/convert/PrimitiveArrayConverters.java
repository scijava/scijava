/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.matcher.convert;

import java.util.function.Function;

import org.scijava.collections.ObjectArray;
import org.scijava.ops.engine.BaseOpHints.Conversion;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpHints;

/**
 * A collection of Ops for converting primitive arrays
 *
 * @author Gabriel Selzer
 */
public class PrimitiveArrayConverters<N extends Number> implements
	OpCollection
{

	// -- Object converters -- //
	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<N[], ObjectArray<Number>> fromNumber = arr -> {
		var oa = new ObjectArray<>(Number.class, arr.length);
		for (int i = 0; i < arr.length; i++) {
			oa.set(i, arr[i]);
		}
		return oa;
	};

	// -- Primitive converters -- //

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<byte[], ObjectArray<Number>> fromPrimitiveByte =
		arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<short[], ObjectArray<Number>> fromPrimitiveShort =
		arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<int[], ObjectArray<Number>> fromPrimitiveInt = arr -> {
		var oa = new ObjectArray<>(Number.class, arr.length);
		for (int i = 0; i < arr.length; i++) {
			oa.set(i, arr[i]);
		}
		return oa;
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<long[], ObjectArray<Number>> fromPrimitiveLong =
		arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<float[], ObjectArray<Number>> fromPrimitiveFloat =
		arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<double[], ObjectArray<Number>> fromPrimitiveDouble =
		arr -> {
			var oa = new ObjectArray<>(Number.class, arr.length);
			for (int i = 0; i < arr.length; i++) {
				oa.set(i, arr[i]);
			}
			return oa;
		};

	// -- Object Converters -- //

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, Byte[]> toByte = o -> o.stream()
		.map(b -> b == null ? null : b.byteValue()).toArray(Byte[]::new);

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, Integer[]> toInteger = o -> o
		.stream().map(i -> i == null ? null : i.intValue()).toArray(Integer[]::new);

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, Short[]> toShort = o -> o.stream()
		.map(s -> s == null ? null : s.shortValue()).toArray(Short[]::new);

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, Long[]> toLong = o -> o.stream()
		.map(l -> l == null ? null : l.longValue()).toArray(Long[]::new);

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, Float[]> toFloat = o -> o.stream()
		.map(f -> f == null ? null : f.floatValue()).toArray(Float[]::new);

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, Double[]> toDouble = o -> o
		.stream().map(d -> d == null ? null : d.doubleValue()).toArray(
			Double[]::new);

	// -- Primitive Converters -- //

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, byte[]> toPrimitiveByte = o -> {
		byte[] arr = new byte[o.size()];
		for (int i = 0; i < o.size(); i++) {
			arr[i] = o.get(i).byteValue();
		}
		return arr;
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, short[]> toPrimitiveShort = o -> {
		short[] arr = new short[o.size()];
		for (int i = 0; i < o.size(); i++) {
			arr[i] = o.get(i).shortValue();
		}
		return arr;
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, int[]> toPrimitiveInt = o -> {
		int[] arr = new int[o.size()];
		for (int i = 0; i < o.size(); i++) {
			arr[i] = o.get(i).intValue();
		}
		return arr;
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, long[]> toPrimitiveLong = o -> {
		long[] arr = new long[o.size()];
		for (int i = 0; i < o.size(); i++) {
			arr[i] = o.get(i).longValue();
		}
		return arr;
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, float[]> toPrimitiveFloat = o -> {
		float[] arr = new float[o.size()];
		for (int i = 0; i < o.size(); i++) {
			arr[i] = o.get(i).floatValue();
		}
		return arr;
	};

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.convert")
	public final Function<ObjectArray<Number>, double[]> toPrimitiveDouble =
		o -> {
			double[] arr = new double[o.size()];
			for (int i = 0; i < o.size(); i++) {
				arr[i] = o.get(i).doubleValue();
			}
			return arr;
		};

}
