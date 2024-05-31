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

package org.scijava.ops.image.transform.hyperSliceView;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.function.Functions;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;

/**
 * Tests {@link org.scijava.ops.image.Ops.Transform.HyperSliceView} ops.
 * <p>
 * This test only checks if the op call works with all parameters and that the
 * result is equal to that of the {@link Views} method call. It is not a
 * correctness test of {@link Views} itself.
 * </p>
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 * @author Gabe Selzer
 */
public class HyperSliceViewTest extends AbstractOpTest {

	Nil<RandomAccessibleInterval<DoubleType>> raiNil =
		new Nil<RandomAccessibleInterval<DoubleType>>()
		{};
	Nil<RandomAccessible<DoubleType>> raNil =
		new Nil<RandomAccessible<DoubleType>>()
		{};
	Nil<Integer> integerNil = new Nil<Integer>() {};
	Nil<Long> longNil = new Nil<Long>() {};

	public static <T> RandomAccessible<T> deinterval(
		RandomAccessibleInterval<T> input)
	{
		return Views.extendBorder(input);
	}

	@Test
	public void testDefaultHyperSlice() {
		Functions.Arity3<RandomAccessible<DoubleType>, Integer, Long, MixedTransformView<DoubleType>> hyperSliceFunc =
			OpBuilder.matchFunction(ops, "transform.hyperSliceView", raNil,
				integerNil, longNil, new Nil<MixedTransformView<DoubleType>>()
				{});

		final Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(
			new int[] { 10, 10, 10 });

		final MixedTransformView<DoubleType> il2 = Views.hyperSlice(
			(RandomAccessible<DoubleType>) img, 1, 8);
		final MixedTransformView<DoubleType> opr = hyperSliceFunc.apply(deinterval(
			img), 1, 8l);

		for (int i = 0; i < il2.getTransformToSource().getMatrix().length; i++) {
			for (int j = 0; j < il2.getTransformToSource()
				.getMatrix()[i].length; j++)
			{
				assertEquals(il2.getTransformToSource().getMatrix()[i][j], opr
					.getTransformToSource().getMatrix()[i][j], 1e-10);
			}
		}
	}

	@Test
	public void testIntervalHyperSlice() {
		Functions.Arity3<RandomAccessibleInterval<DoubleType>, Integer, Long, IntervalView<DoubleType>> hyperSliceFunc =
			OpBuilder.matchFunction(ops, "transform.hyperSliceView", raiNil,
				integerNil, longNil, new Nil<IntervalView<DoubleType>>()
				{});

		final Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(
			new int[] { 10, 10, 10 });

		final IntervalView<DoubleType> il2 = Views.hyperSlice(
			(RandomAccessibleInterval<DoubleType>) img, 1, 8);
		final IntervalView<DoubleType> opr = hyperSliceFunc.apply(img, 1, 8l);

		for (int i = 0; i < ((MixedTransformView<DoubleType>) il2.getSource())
			.getTransformToSource().getMatrix().length; i++)
		{
			for (int j = 0; j < ((MixedTransformView<DoubleType>) il2.getSource())
				.getTransformToSource().getMatrix()[i].length; j++)
			{
				assertEquals(((MixedTransformView<DoubleType>) il2.getSource())
					.getTransformToSource().getMatrix()[i][j],
					((MixedTransformView<DoubleType>) opr.getSource())
						.getTransformToSource().getMatrix()[i][j], 1e-10);
			}
		}

		assertEquals(img.numDimensions() - 1, opr.numDimensions());
	}
}
