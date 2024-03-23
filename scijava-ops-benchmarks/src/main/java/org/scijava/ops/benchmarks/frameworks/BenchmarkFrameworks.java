
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
