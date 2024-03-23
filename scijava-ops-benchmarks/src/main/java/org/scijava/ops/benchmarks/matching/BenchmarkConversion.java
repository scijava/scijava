
package org.scijava.ops.benchmarks.matching;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.readwrite.SamplerConverter;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.DoubleAccess;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;
import org.openjdk.jmh.annotations.*;
import org.scijava.ops.api.Hints;

import static org.scijava.ops.benchmarks.matching.MatchingOpCollection.op;

/**
 * Benchmark showcasing the performance of Op parameter conversion
 *
 * @author Gabriel Selzer
 */
public class BenchmarkConversion {

	private static final Hints HINTS = new Hints("cache.IGNORE");

	private static final SamplerConverter<? super ByteType, DoubleType> CONVERTER //
		= sampler -> new DoubleType(new DoubleAccess() {

			@Override
			public double getValue(int index) {
				return sampler.get().getRealDouble();
			}

			@Override
			public void setValue(int index, double value) {
				sampler.get().setReal(value);
			}
		});

	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void runStatic(MatchingState state) {
		// manual conversion of simple input
		var in = Converters.convert( //
			(RandomAccessibleInterval<ByteType>) state.simpleIn, //
			CONVERTER //
		);
		// manual creation of simple output
		var out = ArrayImgs.doubles(in.dimensionsAsLongArray());

		// invoke Op statically
		op(in, 1.0, out);
	}

	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void runOpConverted(final MatchingState state) {
		var out = ArrayImgs.bytes(state.simpleIn.dimensionsAsLongArray());
		state.env.binary("benchmark.match", HINTS) //
			.input(state.simpleIn, (byte) 1) //
			.output(out) //
			.compute();
	}

	@Fork(value = 1, warmups = 2)
	@Warmup(iterations = 2)
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	public void runOpConvertedAdapted(final MatchingState state) {
		state.env.binary("benchmark.match", HINTS) //
			.input(state.simpleIn, (byte) 1.0) //
			.apply();
	}

}
