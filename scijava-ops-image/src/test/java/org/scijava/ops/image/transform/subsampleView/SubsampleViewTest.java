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

package org.scijava.ops.image.transform.subsampleView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.SubsampleIntervalView;
import net.imglib2.view.SubsampleView;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;
import org.scijava.util.MersenneTwisterFast;

/**
 * Tests {@link org.scijava.ops.image.Ops.Transform.SubsampleView} ops.
 * <p>
 * This test only checks if the op call works with all parameters and that the
 * result is equal to that of the {@link Views} method call. It is not a
 * correctness test of {@link Views} itself.
 * </p>
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
public class SubsampleViewTest extends AbstractOpTest {

	private static final long SEED = 0x12345678;

	@Test
	public void defaultSubsampleTest() {

		BiFunction<Img<DoubleType>, Long, SubsampleView<DoubleType>> subsampleFunc =
			OpBuilder.matchFunction(ops, "transform.subsampleView",
				new Nil<Img<DoubleType>>()
				{}, new Nil<Long>() {}, new Nil<SubsampleView<DoubleType>>() {});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(
			new int[] { 10, 10 });
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		for (DoubleType d : img) {
			d.set(r.nextDouble());
		}

		SubsampleView<DoubleType> il2 = Views.subsample(
			(RandomAccessible<DoubleType>) img, 2);
		SubsampleView<DoubleType> opr = subsampleFunc.apply(img, 2l);

		Cursor<DoubleType> il2C = Views.interval(il2, new long[] { 0, 0 },
			new long[] { 4, 4 }).localizingCursor();
		RandomAccess<DoubleType> oprRA = opr.randomAccess();

		while (il2C.hasNext()) {
			il2C.next();
			oprRA.setPosition(il2C);
			assertEquals(il2C.get().get(), oprRA.get().get(), 1e-10);
		}
	}

	@Test
	public void defaultSubsampleStepsTest() {

		BiFunction<Img<DoubleType>, long[], SubsampleView<DoubleType>> subsampleFunc =
			OpBuilder.matchFunction(ops, "transform.subsampleView",
				new Nil<Img<DoubleType>>()
				{}, new Nil<long[]>() {}, new Nil<SubsampleView<DoubleType>>() {});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(
			new int[] { 10, 10 });
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		for (DoubleType d : img) {
			d.set(r.nextDouble());
		}

		SubsampleView<DoubleType> il2 = Views.subsample(
			(RandomAccessible<DoubleType>) img, 2, 1);
		SubsampleView<DoubleType> opr = subsampleFunc.apply(img, new long[] { 2,
			1 });

		Cursor<DoubleType> il2C = Views.interval(il2, new long[] { 0, 0 },
			new long[] { 4, 9 }).localizingCursor();
		RandomAccess<DoubleType> oprRA = opr.randomAccess();

		while (il2C.hasNext()) {
			il2C.next();
			oprRA.setPosition(il2C);
			assertEquals(il2C.get().get(), oprRA.get().get(), 1e-10);
		}
	}

	@Test
	public void testIntervalSubsample() {

		BiFunction<Img<DoubleType>, Long, SubsampleIntervalView<DoubleType>> subsampleFunc =
			OpBuilder.matchFunction(ops, "transform.subsampleView",
				new Nil<Img<DoubleType>>()
				{}, new Nil<Long>() {},
				new Nil<SubsampleIntervalView<DoubleType>>()
				{});

		Img<DoubleType> img = ArrayImgs.doubles(10, 10);
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		for (DoubleType d : img) {
			d.set(r.nextDouble());
		}

		SubsampleIntervalView<DoubleType> expected = Views.subsample(
			(RandomAccessibleInterval<DoubleType>) img, 2);
		SubsampleIntervalView<DoubleType> actual = subsampleFunc.apply(img, 2l);

		Cursor<DoubleType> il2C = Views.interval(expected, new long[] { 0, 0 },
			new long[] { 4, 4 }).localizingCursor();
		RandomAccess<DoubleType> oprRA = actual.randomAccess();

		while (il2C.hasNext()) {
			il2C.next();
			oprRA.setPosition(il2C);
			assertEquals(il2C.get().get(), oprRA.get().get(), 1e-10);
		}

		assertTrue(Intervals.equals(expected, actual));
	}

	@Test
	public void testIntervalSubsampleSteps() {

		BiFunction<Img<DoubleType>, long[], SubsampleIntervalView<DoubleType>> subsampleFunc =
			OpBuilder.matchFunction(ops, "transform.subsampleView",
				new Nil<Img<DoubleType>>()
				{}, new Nil<long[]>() {},
				new Nil<SubsampleIntervalView<DoubleType>>()
				{});

		Img<DoubleType> img = ArrayImgs.doubles(10, 10);
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		for (DoubleType d : img) {
			d.set(r.nextDouble());
		}

		SubsampleIntervalView<DoubleType> expected = Views.subsample(
			(RandomAccessibleInterval<DoubleType>) img, 2, 1);
		SubsampleIntervalView<DoubleType> actual = subsampleFunc.apply(img,
			new long[] { 2, 1 });

		Cursor<DoubleType> il2C = Views.interval(expected, new long[] { 0, 0 },
			new long[] { 4, 9 }).localizingCursor();
		RandomAccess<DoubleType> oprRA = actual.randomAccess();

		while (il2C.hasNext()) {
			il2C.next();
			oprRA.setPosition(il2C);
			assertEquals(il2C.get().get(), oprRA.get().get(), 1e-10);
		}

		assertTrue(Intervals.equals(expected, actual));
	}
}
