/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;
import org.scijava.util.MersenneTwisterFast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test copying {@link net.imglib2.RandomAccessibleInterval}s
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
public class CopyRAITest extends AbstractOpTest {

	private Img<UnsignedByteType> input;

	Img<UnsignedByteType> input2;
	Img<UnsignedByteType> inputPlanar;
	IntervalView<UnsignedByteType> view;
	IntervalView<UnsignedByteType> viewPlanar;

	int[] size1 = new int[] { 64, 64, 64 };
	int[] size2 = new int[] { 32, 32, 32 };

	double delta = 0.0000001;

	@BeforeEach
	public void createData() {
		input = new ArrayImgFactory<>(new UnsignedByteType()).create(new int[] { 120, 100 });

		final MersenneTwisterFast r = new MersenneTwisterFast(System.currentTimeMillis());

		final Cursor<UnsignedByteType> inc = input.cursor();

		while (inc.hasNext()) {
			inc.next().setReal(r.nextDouble() * 255);
		}

		// create
		final long[] start = new long[] { 16, 16, 16 };
		final long[] end = new long[] { 47, 47, 47 };

		input2 = ops.op("create.img").arity2().input(new FinalDimensions(size1), new UnsignedByteType())
				.outType(new Nil<Img<UnsignedByteType>>() {}).apply();

		// create the same input but force it to be a planar image
		inputPlanar = ops.op("create.img")
				.arity3().input(new FinalDimensions(size1), new UnsignedByteType(),
						new PlanarImgFactory<>(new UnsignedByteType()))
				.outType(new Nil<Img<UnsignedByteType>>() {}).apply();

		// get centered views
		view = Views.interval(input2, new FinalInterval(start, end));
		viewPlanar = Views.interval(inputPlanar, new FinalInterval(start, end));

		final Cursor<UnsignedByteType> cursor = view.cursor();
		final Cursor<UnsignedByteType> cursorPlanar = viewPlanar.cursor();

		// set every pixel in the view to 100
		while (cursor.hasNext()) {
			cursor.fwd();
			cursorPlanar.fwd();
			cursor.get().setReal(100.0);
			cursorPlanar.get().setReal(100.0);

		}
	}

	@Test
	public void copyRAINoOutputTest() {
		final RandomAccessibleInterval<UnsignedByteType> output = ops.op("copy.rai")
			.arity1().input(input).outType(
				new Nil<RandomAccessibleInterval<UnsignedByteType>>()
				{}).apply();

		final Cursor<UnsignedByteType> inc = input.localizingCursor();
		final RandomAccess<UnsignedByteType> outRA = output.randomAccess();

		while (inc.hasNext()) {
			inc.fwd();
			outRA.setPosition(inc);
			assertEquals(inc.get().get(), outRA.get().get());
		}
	}

	@Test
	public void copyRAIWithOutputTest() {
		final Img<UnsignedByteType> output = input.factory().create(input, input.firstElement());

		ops.op("copy.rai").arity1().input(input).output(output).compute();

		final Cursor<UnsignedByteType> inc = input.cursor();
		final Cursor<UnsignedByteType> outc = output.cursor();

		while (inc.hasNext()) {
			assertEquals(inc.next().get(), outc.next().get());
		}
	}

	@Test
	public void copyRAIDifferentSizeTest() {

		// create a copy op
		final Computers.Arity1<IntervalView<UnsignedByteType>, RandomAccessibleInterval<UnsignedByteType>> copy = OpBuilder
				.matchComputer(ops, "copy.rai", new Nil<IntervalView<UnsignedByteType>>() {},
						new Nil<RandomAccessibleInterval<UnsignedByteType>>() {});

		assertNotNull(copy);

		final Img<UnsignedByteType> out = ops.op("create.img").arity2().input(new FinalDimensions(size2), new UnsignedByteType()) //
				.outType(new Nil<Img<UnsignedByteType>>() {}) //
				.apply();

		// copy view to output and assert that is equal to the mean of the view
		copy.compute(view, out);
		DoubleType sum = new DoubleType();
		ops.op("stats.mean").arity1().input(out).output(sum).compute();
		assertEquals(sum.getRealDouble(), 100.0, delta);

		// also try with a planar image
		final Img<UnsignedByteType> outFromPlanar = ops.op("create.img")
				.arity2().input(new FinalDimensions(size2), new UnsignedByteType()).outType(new Nil<Img<UnsignedByteType>>() {})
				.apply();

		copy.compute(viewPlanar, outFromPlanar);
		DoubleType sumFromPlanar = new DoubleType();
		ops.op("stats.mean").arity1().input(outFromPlanar).output(sumFromPlanar).compute();
		assertEquals(sumFromPlanar.getRealDouble(), 100.0, delta);

	}
}
