/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.copy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;
import org.scijava.util.MersenneTwisterFast;

/**
 * Test {@link CopyImg}.
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
public class CopyImgTest extends AbstractOpTest {

	private Img<DoubleType> input;

	@BeforeEach
	public void createData() {
		input = new ArrayImgFactory<>(new DoubleType()).create(new int[] { 120,
			100 });

		final MersenneTwisterFast r = new MersenneTwisterFast(System
			.currentTimeMillis());

		final Cursor<DoubleType> inc = input.cursor();

		while (inc.hasNext()) {
			inc.next().set(r.nextDouble());
		}
	}

	@Test
	public void testCopyImgNoOutput() {
		final Img<DoubleType> inputCopy = input.factory().create(input, input
			.firstElement());
		copy(input, inputCopy);

		final RandomAccessibleInterval<DoubleType> output = ops.op("copy.img")
			.input(input).outType(new Nil<RandomAccessibleInterval<DoubleType>>()
			{}).apply();

		final Cursor<DoubleType> inc = input.localizingCursor();
		final RandomAccess<DoubleType> inCopyRA = inputCopy.randomAccess();
		final RandomAccess<DoubleType> outRA = output.randomAccess();

		while (inc.hasNext()) {
			inc.fwd();
			inCopyRA.setPosition(inc);
			outRA.setPosition(inc);
			assertEquals(inc.get().get(), outRA.get().get(), 0.0);
			assertEquals(inc.get().get(), inCopyRA.get().get(), 0.0);
		}
	}

	@Test
	public void testCopyImgWithOutput() {
		final Img<DoubleType> inputCopy = input.factory().create(input, input
			.firstElement());
		copy(input, inputCopy);

		final Img<DoubleType> output = input.factory().create(input, input
			.firstElement());

		ops.op("copy.img").input(input).output(output).compute();

		final Cursor<DoubleType> inc = input.cursor();
		final Cursor<DoubleType> inCopyc = inputCopy.cursor();
		final Cursor<DoubleType> outc = output.cursor();

		while (inc.hasNext()) {
			assertEquals(inc.next().get(), outc.next().get(), 0.0);
			assertEquals(inc.get().get(), inCopyc.next().get(), 0.0);
		}
	}

	private void copy(final Img<DoubleType> from, final Img<DoubleType> to) {
		final Cursor<DoubleType> fromc = from.cursor();
		final Cursor<DoubleType> toc = to.cursor();
		while (fromc.hasNext()) {
			toc.next().set(fromc.next().get());
		}
	}
}
