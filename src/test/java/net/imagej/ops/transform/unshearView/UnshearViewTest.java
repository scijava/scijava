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
package net.imagej.ops.transform.unshearView;

import static org.junit.Assert.assertEquals;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.TransformView;
import net.imglib2.view.Views;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function4;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Functions;

/**
 * Tests {@link net.imagej.ops.Ops.Transform.UnshearView} ops.
 * <p>
 * This test only checks if the op call works with all parameters and that the
 * result is equal to that of the {@link Views} method call. It is not a
 * correctness test of {@link Views} itself.
 * </p>
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
public class UnshearViewTest extends AbstractTestEnvironment {
	@Test
	public void defaultUnshearTest() {

		Function3<RandomAccessible<DoubleType>, Integer, Integer, TransformView<DoubleType>> unshearFunc = Functions
				.ternary(ops, "transform.unshearView", new Nil<RandomAccessible<DoubleType>>() {
				}, new Nil<Integer>() {
				}, new Nil<Integer>() {
				}, new Nil<TransformView<DoubleType>>() {
				});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(new int[] { 2, 2 });
		Cursor<DoubleType> imgC = img.cursor();
		while (imgC.hasNext()) {
			imgC.next().set(1);
		}

		TransformView<DoubleType> il2 = Views.unshear(Views.shear(Views.extendZero(img), 0, 1), 0, 1);
		TransformView<DoubleType> opr = unshearFunc.apply(Views.shear(Views.extendZero(img), 0, 1), 0, 1);
		Cursor<DoubleType> il2C = Views.interval(il2, new FinalInterval(new long[] { 0, 0 }, new long[] { 3, 3 }))
				.cursor();
		RandomAccess<DoubleType> oprRA = Views
				.interval(opr, new FinalInterval(new long[] { 0, 0 }, new long[] { 3, 3 })).randomAccess();

		while (il2C.hasNext()) {
			il2C.next();
			oprRA.setPosition(il2C);
			assertEquals(il2C.get().get(), oprRA.get().get(), 1e-10);
		}
	}

	@Test
	public void UnshearIntervalTest() {

		Function4<RandomAccessible<DoubleType>, Interval, Integer, Integer, IntervalView<DoubleType>> unshearFunc = Functions
				.quaternary(ops, "transform.unshearView", new Nil<RandomAccessible<DoubleType>>() {
				}, new Nil<Interval>() {
				}, new Nil<Integer>() {
				}, new Nil<Integer>() {
				}, new Nil<IntervalView<DoubleType>>() {
				});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(new int[] { 2, 2 });
		Cursor<DoubleType> imgC = img.cursor();
		while (imgC.hasNext()) {
			imgC.next().set(1);
		}

		Cursor<DoubleType> il2 = Views
				.unshear(Views.shear(Views.extendZero(img), 0, 1), new FinalInterval(new long[] { 0, 0 }, new long[] { 3, 3 }), 0, 1)
				.cursor();
		RandomAccess<DoubleType> opr = unshearFunc.apply(Views.shear(Views.extendZero(img), 0, 1),
				new FinalInterval(new long[] { 0, 0 }, new long[] { 3, 3 }), 0, 1)
				.randomAccess();

		while (il2.hasNext()) {
			il2.next();
			opr.setPosition(il2);
			assertEquals(il2.get().get(), opr.get().get(), 1e-10);
		}
	}
}
