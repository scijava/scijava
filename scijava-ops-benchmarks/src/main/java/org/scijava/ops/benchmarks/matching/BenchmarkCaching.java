
package org.scijava.ops.benchmarks.matching;

import net.imglib2.img.array.ArrayImgs;
import org.openjdk.jmh.annotations.*;
import org.scijava.ops.api.Hints;

import static org.scijava.ops.benchmarks.matching.MatchingOpCollection.op;

/**
 * Benchmark showcasing the effects of Op caching
 *
 * @author Gabriel Selzer
 */
public class BenchmarkCaching {

	private static final Hints CACHE_HIT_HINTS = new Hints();
	private static final Hints CACHE_MISS_HINTS = new Hints("cache.IGNORE");

	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void runStatic(MatchingState state) {
		var out = ArrayImgs.doubles(state.in.dimensionsAsLongArray());
		op(state.in, 1.0, out);
	}

	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void runOp(final MatchingState state) {
		var out = ArrayImgs.doubles(state.in.dimensionsAsLongArray());
		state.env.binary("benchmark.match", CACHE_MISS_HINTS) //
			.input(state.in, 1.0) //
			.output(out) //
			.compute();
	}

	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void runOpCached(final MatchingState state) {
		var out = ArrayImgs.doubles(state.in.dimensionsAsLongArray());
		state.env.binary("benchmark.match", CACHE_HIT_HINTS) //
			.input(state.in, 1.0) //
			.output(out) //
			.compute();
	}
}
