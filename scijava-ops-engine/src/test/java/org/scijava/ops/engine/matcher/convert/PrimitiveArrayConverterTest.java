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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.conversionLoss.impl.IdentityLossReporter;
import org.scijava.ops.engine.conversionLoss.impl.PrimitiveLossReporters;
import org.scijava.ops.engine.matcher.impl.LossReporterWrapper;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class PrimitiveArrayConverterTest extends AbstractTestEnvironment
	implements OpCollection
{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register( //
			new PrimitiveArrayConverterTest(), //
			new UtilityConverters(), //
			new PrimitiveLossReporters(), //
			new IdentityLossReporter<>(), //
			new IdentityCollection<>(), //
			new LossReporterWrapper<>(), //
			new PrimitiveArrayConverters<>() //
		);
	}

	@OpField(names = "test.byteArray")
	public final Function<Double[], Double[]> byteArray = in -> {
		Double[] d = new Double[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testByteArray() {
		Byte[] original = new Byte[] { 1, 2 };
		Byte[] actual = ops.op("test.byteArray").input(original).outType(
			Byte[].class).apply();
		Byte[] expected = new Byte[] { 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.bytePrimitiveArray")
	public final Function<Double[], Double[]> bytePrimitiveArray = in -> {
		Double[] d = new Double[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testBytePrimitiveArray() {
		byte[] original = new byte[] { 1, 2 };
		byte[] actual = ops.op("test.bytePrimitiveArray").input(original).outType(
			byte[].class).apply();
		byte[] expected = new byte[] { 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.shortArray")
	public final Function<Double[], Double[]> shortArray = in -> {
		Double[] d = new Double[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testShortArray() {
		Short[] original = new Short[] { 1, 2 };
		Short[] actual = ops.op("test.shortArray").input(original).outType(
			Short[].class).apply();
		Short[] expected = new Short[] { 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.shortPrimitiveArray")
	public final Function<Double[], Double[]> shortPrimitiveArray = in -> {
		Double[] d = new Double[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testShortPrimitiveArray() {
		short[] original = new short[] { 1, 2 };
		short[] actual = ops.op("test.shortPrimitiveArray").input(original).outType(
			short[].class).apply();
		short[] expected = new short[] { 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.intArray")
	public final Function<Double[], Double[]> intArray = in -> {
		Double[] d = new Double[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testIntegerArray() {
		Integer[] original = new Integer[] { 1, 2 };
		Integer[] actual = ops.op("test.intArray").input(original).outType(
			Integer[].class).apply();
		Integer[] expected = new Integer[] { 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.intPrimitiveArray")
	public final Function<Double[], Double[]> intPrimitiveArray = in -> {
		Double[] d = new Double[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testIntegerPrimitiveArray() {
		int[] original = new int[] { 1, 2 };
		int[] actual = ops.op("test.intPrimitiveArray").input(original).outType(
			int[].class).apply();
		int[] expected = new int[] { 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.longArray")
	public final Function<Double[], Double[]> longArray = in -> {
		Double[] d = new Double[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testLongArray() {
		Long[] original = new Long[] { 1L, 2L };
		Long[] actual = ops.op("test.longArray").input(original).outType(
			Long[].class).apply();
		Long[] expected = new Long[] { 2L, 3L };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.longPrimitiveArray")
	public final Function<Double[], Double[]> longPrimitiveArray = in -> {
		Double[] d = new Double[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testLongPrimitiveArray() {
		long[] original = new long[] { 1, 2 };
		long[] actual = ops.op("test.longPrimitiveArray").input(original).outType(
			long[].class).apply();
		long[] expected = new long[] { 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.floatArray")
	public final Function<Integer[], Integer[]> floatArray = in -> {
		Integer[] d = new Integer[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testFloatArray() {
		Float[] original = new Float[] { 1f, 2f };
		Float[] actual = ops.op("test.floatArray").input(original).outType(
			Float[].class).apply();
		Float[] expected = new Float[] { 2f, 3f };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.floatPrimitiveArray")
	public final Function<Integer[], Integer[]> floatPrimitiveArray = in -> {
		Integer[] d = new Integer[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testFloatPrimitiveArray() {
		float[] original = new float[] { 1, 2 };
		float[] actual = ops.op("test.floatPrimitiveArray").input(original).outType(
			float[].class).apply();
		float[] expected = new float[] { 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.doubleArray")
	public final Function<Integer[], Integer[]> doubleArray = in -> {
		Integer[] d = new Integer[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testDoubleArray() {
		Double[] original = new Double[] { 1d, 2d };
		Double[] actual = ops.op("test.doubleArray").input(original).outType(
			Double[].class).apply();
		Double[] expected = new Double[] { 2d, 3d };
		Assertions.assertArrayEquals(expected, actual);
	}

	@OpField(names = "test.doublePrimitiveArray")
	public final Function<Integer[], Integer[]> doublePrimitiveArray = in -> {
		Integer[] d = new Integer[in.length];
		for (int i = 0; i < in.length; i++)
			d[i] = in[i] + 1;
		return d;
	};

	@Test
	public void testDoublePrimitiveArray() {
		double[] original = new double[] { 1, 2 };
		double[] actual = ops.op("test.doublePrimitiveArray").input(original)
			.outType(double[].class).apply();
		double[] expected = new double[] { 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

}
