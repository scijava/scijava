
package org.scijava.ops.engine.matcher.simplify;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.matcher.impl.LossReporterWrapper;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class PrimitiveArraySimplifierTest extends AbstractTestEnvironment
	implements OpCollection
{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register( //
			new PrimitiveArraySimplifierTest(), //
			new PrimitiveLossReporters(), //
			new IdentityLossReporter<>(), //
			new IdentityCollection<>(), //
			new LossReporterWrapper<>(), //
			new PrimitiveArraySimplifiers<>() //
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
		Byte[] actual = ops.unary("test.byteArray").input(original).outType(
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
		byte[] actual = ops.unary("test.bytePrimitiveArray").input(original)
			.outType(byte[].class).apply();
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
		Short[] actual = ops.unary("test.shortArray").input(original).outType(
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
		short[] actual = ops.unary("test.shortPrimitiveArray").input(original)
			.outType(short[].class).apply();
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
		Integer[] actual = ops.unary("test.intArray").input(original).outType(
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
		int[] actual = ops.unary("test.intPrimitiveArray").input(original).outType(
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
		Long[] actual = ops.unary("test.longArray").input(original).outType(
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
		long[] actual = ops.unary("test.longPrimitiveArray").input(original)
			.outType(long[].class).apply();
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
		Float[] actual = ops.unary("test.floatArray").input(original).outType(
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
		float[] actual = ops.unary("test.floatPrimitiveArray").input(original)
			.outType(float[].class).apply();
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
		Double[] actual = ops.unary("test.doubleArray").input(original).outType(
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
		double[] actual = ops.unary("test.doublePrimitiveArray").input(original)
			.outType(double[].class).apply();
		double[] expected = new double[] { 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

}
