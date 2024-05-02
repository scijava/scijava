/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.filter.addNoise;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import org.scijava.common3.MersenneTwisterFast;
import org.scijava.ops.spi.Nullable;

/**
 * Contains Ops designed to add noise to populated images.
 *
 * @author Gabriel Selzer
 */
public class NoiseAdders {

	private static final Long defaultSeed = 0xabcdef1234567890L;

	/**
	 * Sets the real component of an output real number to the addition of the
	 * real component of an input real number with an amount of Gaussian noise.
	 * <p>
	 * Note that this Op has changed relative to the older implementations; before
	 * it operated on RealTypes, we now only provide the operation on
	 * Iterable<RealType>s. This is due to the nature of {@link Random}: The old
	 * implementation saved a {@link Random} and used it on each {@link RealType}
	 * passed to the Op. This provided no deterministic output, as the same input
	 * would yield two different outputs if called in succession. Thus in this
	 * iteration of the Op we make it a requirement that the input must be an
	 * {@link Iterable}. Since the {@link Random} is created upon every call of
	 * the Op it ensures that given the same seed and input data the output will
	 * always be the same.
	 *
	 * @param input
	 * @param rangeMin
	 * @param rangeMax
	 * @param rangeStdDev
	 * @param seed
	 * @param output
	 * @implNote op names='filter.addNoise', type=Computer
	 */
	public static <I extends RealType<I>, O extends RealType<O>> void
		addNoiseInterval( //
			final RandomAccessibleInterval<I> input, //
			final Double rangeMin, //
			final Double rangeMax, //
			final Double rangeStdDev, //
			@Nullable Long seed, //
			final RandomAccessibleInterval<O> output //
	) {
		if (seed == null) {
			seed = defaultSeed;
		}
		Random rng = new Random(seed);
		LoopBuilder.setImages(input, output).multiThreaded() //
			.forEachPixel((in, out) -> {
				addNoise(in, out, rangeMin, rangeMax, rangeStdDev, rng);
			});
	};

	// Copied from the previous implementation of addNoise
	public static <I extends RealType<I>, O extends RealType<O>> void addNoise(
		final I input, final O output, final double rangeMin, final double rangeMax,
		final double rangeStdDev, final Random rng)
	{
		int i = 0;
		do {
			final double newVal = input.getRealDouble() + rng.nextGaussian() *
				rangeStdDev;
			if (rangeMin <= newVal && newVal <= rangeMax) {
				output.setReal(newVal);
				return;
			}
			if (i++ > 100) {
				throw new IllegalArgumentException(
					"noise function failing to terminate. probably misconfigured.");
			}
		}
		while (true);
	}

	// -- POISSON NOISE -- //

	/**
	 * Sets the real component of an output real number to a real number sampled
	 * from a Poisson distribution with lambda of the input real number.
	 * <p>
	 * Implementation according to:
	 * </p>
	 * <p>
	 * D. E. Knuth. Art of Computer Programming, Volume 2: Seminumerical
	 * Algorithms (3rd Edition). Addison-Wesley Professional, November 1997
	 * </p>
	 *
	 * @author Jan Eglinger
	 *         <p>
	 *         Note that this Op has changed relative to the older
	 *         implementations; before it operated on RealTypes, we now only
	 *         provide the operation on Iterable<RealType>s. This is due to the
	 *         nature of {@link Random}: The old implementation saved a
	 *         {@link Random} and used it on each {@link RealType} passed to the
	 *         Op. This provided no deterministic output, as the same input would
	 *         yield two different outputs if called in succession. Thus in this
	 *         iteration of the Op we make it a requirement that the input must be
	 *         an {@link Iterable}. Since the {@link Random} is created upon every
	 *         call of the Op it ensures that given the same seed and input data
	 *         the output will always be the same.
	 * @param input
	 * @param seed
	 * @param output
	 * @implNote op names='filter.addPoissonNoise', type=Computer
	 */
	public static <I extends RealType<I>, O extends RealType<O>> void
		addPoissonNoiseInterval( //
			final RandomAccessibleInterval<I> input, //
			@Nullable Long seed, //
			final RandomAccessibleInterval<O> output //
	) {
		if (seed == null) {
			seed = defaultSeed;
		}
		Random rng = new Random(seed);
		LoopBuilder.setImages(input, output).multiThreaded() //
			.forEachPixel((in, out) -> {
				addPoissonNoise(in, rng, out);
			});
	}

	// -- Static utility methods --

	// Copied from the previous implementation of addNoise
	public static <I extends RealType<I>, O extends RealType<O>> void
		addPoissonNoise(final I input, final Random rng, final O output)
	{
		double l = Math.exp(-input.getRealDouble());
		int k = 0;
		double p = 1;
		do {
			k++;
			p *= rng.nextDouble();
		}
		while (p >= l);
		output.setReal(k - 1);
	}

	// -- UNIFORM NOISE -- //

	/**
	 * Sets the real component of an output real number to the addition of the
	 * real component of an input real number with an amount of uniform noise.
	 *
	 * @param input the input {@link RandomAccessibleInterval}
	 * @param rangeMin the "most negative" value that can be added to each element
	 * @param rangeMax the "most positive" value that can be added to each element
	 * @param seed the seed to the random number generator
	 * @param output the output {@link RandomAccessibleInterval}
	 * @implNote op names='filter.addUniformNoise', type=Computer
	 */
	public static <I extends RealType<I>> void addUniformNoise( //
		RandomAccessibleInterval<I> input, //
		I rangeMin, //
		I rangeMax, //
		@Nullable Long seed, //
		RandomAccessibleInterval<I> output //
	) {
		// Set seed to default if necessary
		if (seed == null) {
			seed = 0xabcdef1234567890L;
		}
		// Construct the Random Number Generator
		MersenneTwisterFast rng = new MersenneTwisterFast(seed);
		// Find the range
		I range = rangeMax.createVariable();
		range.set(rangeMax);
		range.sub(rangeMin);

		// Loop over the images
		LoopBuilder.setImages(input, output).forEachPixel((i, o) -> {
			// Random value = next double * range
			o.set(range);
			o.mul(rng.nextDouble(true, true));
			// Add the range minimum
			o.add(rangeMin);
			// Add the original value
			o.add(i);
		});

	}

}
