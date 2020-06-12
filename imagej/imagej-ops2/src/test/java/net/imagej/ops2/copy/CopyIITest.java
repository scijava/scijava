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
package net.imagej.ops2.copy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;
import org.scijava.util.MersenneTwisterFast;

/**
 * Test {@link CopyII}
 * 
 * @author Christian Dietz (University of Konstanz)
 */
public class CopyIITest extends AbstractOpTest {

	private Img<DoubleType> input;

	@BeforeEach
	public void createData() {
		input = new PlanarImgFactory<>(new DoubleType()).create(new int[] { 120, 100 });

		final MersenneTwisterFast r = new MersenneTwisterFast(System.currentTimeMillis());

		final Cursor<DoubleType> inc = input.cursor();

		while (inc.hasNext()) {
			inc.next().set(r.nextDouble());
		}
	}

	@Test
	public void copyRAINoOutputTest() {
		IterableInterval<DoubleType> output = ops.op("copy.iterableInterval").input(input)
				.outType(new Nil<IterableInterval<DoubleType>>() {}).apply();

		Cursor<DoubleType> inc = input.localizingCursor();
		Cursor<DoubleType> out = output.cursor();

		while (inc.hasNext()) {
			inc.fwd();
			out.fwd();
			assertEquals(inc.get().get(), out.get().get(), 0.0);
		}
	}

	@Test
	public void copyTypeTest() {
		Img<FloatType> inputFloat = new ArrayImgFactory<>(new FloatType()).create(new int[] { 120, 100 });

		Img<FloatType> output = ops.op("copy.iterableInterval").input(inputFloat)
				.outType(new Nil<Img<FloatType>>() {}).apply();

		assertTrue(output.firstElement() instanceof FloatType,
			"Should be FloatType.");
	}

	@Test
	public void copyRAIWithOutputTest() {
		Img<DoubleType> output = input.factory().create(input, input.firstElement());

		ops.op("copy.iterableInterval").input(input).output(output).compute();

		final Cursor<DoubleType> inc = input.cursor();
		final Cursor<DoubleType> outc = output.cursor();

		while (inc.hasNext()) {
			assertEquals(inc.next().get(), outc.next().get(), 0.0);
		}
	}
}
