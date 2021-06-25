/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.filter.addNoise;

import java.util.Random;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Contains Ops designed to add noise to populated images.
 * 
 * @author Gabriel Selzer
 *
 * @param <I>
 *            type of input
 * @param <O>
 *            type of output
 */
@Plugin(type = OpCollection.class)
public class NoiseAdders<I extends RealType<I>, O extends RealType<O>> {

	/**
	 * Sets the real component of an output real number to the addition of the real
	 * component of an input real number with an amount of Gaussian noise.
	 * 
	 * Note that this Op has changed relative to the older implementations; before
	 * it operated on RealTypes, we now only provide the operation on
	 * Iterable<RealType>s. This is due to the nature of {@link Random}: The old
	 * implementation saved a {@link Random} and used it on each {@link RealType}
	 * passed to the Op. This provided no deterministic output, as the same input
	 * would yield two different outputs if called in succession. Thus in this
	 * iteration of the Op we make it a requirement that the input must be an
	 * {@link Iterable}. Since the {@link Random} is created upon every call of the
	 * Op it ensures that given the same seed and input data the output will always
	 * be the same.
	 */
	@OpField(names = "filter.addNoise", params = "input, rangeMin, rangeMax, rangeStdDev, seed, output")
	public final Computers.Arity5<RandomAccessibleInterval<I>, Double, Double, Double, Long, RandomAccessibleInterval<O>> addNoiseInterval = (
			input, rangeMin, rangeMax, rangeStdDev, seed, output) -> {
		addNoise(input, output, rangeMin, rangeMax, rangeStdDev, new Random(seed));
	};

	/**
	 * Convenience Op for when the user does not pass through a seed (default seed
	 * taken from past implementation).
	 */
	@OpField(names = "filter.addNoise", params = "input, rangeMin, rangeMax, rangeStdDev, output")
	public final Computers.Arity4<RandomAccessibleInterval<I>, Double, Double, Double, RandomAccessibleInterval<O>> addNoiseIntervalSeedless = (
			input, rangeMin, rangeMax, rangeStdDev,
			output) -> addNoiseInterval.compute(input, rangeMin, rangeMax, rangeStdDev, 0xabcdef1234567890L, output);

	// -- Static utility methods --

	// Runs the below method on every element of the input iterables.
	public static <I extends RealType<I>, O extends RealType<O>> void addNoise(
		final RandomAccessibleInterval<I> input,
		final RandomAccessibleInterval<O> output, final double rangeMin,
		final double rangeMax, final double rangeStdDev, final Random rng)
	{
		LoopBuilder.setImages(input, output).multiThreaded().forEachPixel((in,
			out) -> {
			addNoise(in, out, rangeMin, rangeMax, rangeStdDev, rng);
		});
	}

	// Copied from the previous implementation of addNoise
	public static <I extends RealType<I>, O extends RealType<O>> void addNoise(final I input, final O output,
			final double rangeMin, final double rangeMax, final double rangeStdDev, final Random rng) {
		int i = 0;
		do {
			final double newVal = input.getRealDouble() + rng.nextGaussian() * rangeStdDev;
			if (rangeMin <= newVal && newVal <= rangeMax) {
				output.setReal(newVal);
				return;
			}
			if (i++ > 100) {
				throw new IllegalArgumentException("noise function failing to terminate. probably misconfigured.");
			}
		} while (true);
	}

	// -- POISSON NOISE -- //

	/**
	 * Sets the real component of an output real number to a real number sampled
	 * from a Poisson distribution with lambda of the input real number.
	 * <p>
	 * Implementation according to:
	 * </p>
	 * <p>
	 * D. E. Knuth. Art of Computer Programming, Volume 2: Seminumerical Algorithms
	 * (3rd Edition). Addison-Wesley Professional, November 1997
	 * </p>
	 * 
	 * @author Jan Eglinger
	 * 
	 *         Note that this Op has changed relative to the older implementations;
	 *         before it operated on RealTypes, we now only provide the operation on
	 *         Iterable<RealType>s. This is due to the nature of {@link Random}: The
	 *         old implementation saved a {@link Random} and used it on each
	 *         {@link RealType} passed to the Op. This provided no deterministic
	 *         output, as the same input would yield two different outputs if called
	 *         in succession. Thus in this iteration of the Op we make it a
	 *         requirement that the input must be an {@link Iterable}. Since the
	 *         {@link Random} is created upon every call of the Op it ensures that
	 *         given the same seed and input data the output will always be the
	 *         same.
	 */
	@OpField(names = "filter.addPoissonNoise", params = "input, seed, output")
	public final Computers.Arity2<RandomAccessibleInterval<I>, Long, RandomAccessibleInterval<O>> addPoissonNoiseInterval = (input, seed,
			output) -> addPoissonNoise(input, new Random(seed), output);

	/**
	 * Convenience Op for when the user does not pass through a seed (default seed
	 * taken from past implementation).
	 */
	@OpField(names = "filter.addPoissonNoise", params = "input, output")
	public final Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> addPoissonNoiseIntervalSeedless = (input,
			output) -> addPoissonNoise(input, new Random(0xabcdef1234567890L), output);

	// -- Static utility methods --

	// Runs the below method on every element of the input iterables.
	public static <I extends RealType<I>, O extends RealType<O>> void
		addPoissonNoise(final RandomAccessibleInterval<I> input, final Random rng,
			final RandomAccessibleInterval<O> output)
	{
		LoopBuilder.setImages(input, output).multiThreaded().forEachPixel((in,
			out) -> {
			addPoissonNoise(in, rng, out);
		});
	}

	// Copied from the previous implementation of addNoise
	public static <I extends RealType<I>, O extends RealType<O>> void addPoissonNoise(final I input, final Random rng,
			final O output) {
		double l = Math.exp(-input.getRealDouble());
		int k = 0;
		double p = 1;
		do {
			k++;
			p *= rng.nextDouble();
		} while (p >= l);
		output.setReal(k - 1);
		return;
	}

}
