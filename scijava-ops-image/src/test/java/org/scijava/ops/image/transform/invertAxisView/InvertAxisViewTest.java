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

package org.scijava.ops.image.transform.invertAxisView;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiFunction;

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
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;

/**
 * Tests {@link org.scijava.ops.image.Ops.Transform.InvertAxisView} ops.
 * <p>
 * This test only checks if the op call works with all parameters and that the
 * result is equal to that of the {@link Views} method call. It is not a
 * correctness test of {@link Views} itself.
 * </p>
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 * @author Gabriel Selzer
 */
public class InvertAxisViewTest extends AbstractOpTest {

	public static <T> RandomAccessible<T> deinterval(
		RandomAccessibleInterval<T> input)
	{
		return Views.extendBorder(input);
	}

	@Test
	public void testDefaultInvertAxis() {
		BiFunction<RandomAccessible<DoubleType>, Integer, MixedTransformView<DoubleType>> invertFunc =
			OpBuilder.matchFunction(ops, "transform.invertAxisView",
				new Nil<RandomAccessible<DoubleType>>()
				{}, new Nil<Integer>() {},
				new Nil<MixedTransformView<DoubleType>>()
				{});

		final Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(
			new int[] { 10, 10 });

		final MixedTransformView<DoubleType> il2 = Views.invertAxis(
			(RandomAccessible<DoubleType>) img, 1);
		final MixedTransformView<DoubleType> opr = invertFunc.apply(deinterval(img),
			1);

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
	public void testIntervalInvertAxis() {
		BiFunction<RandomAccessibleInterval<DoubleType>, Integer, IntervalView<DoubleType>> invertFunc =
			OpBuilder.matchFunction(ops, "transform.invertAxisView",
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}, new Nil<Integer>() {}, new Nil<IntervalView<DoubleType>>() {});

		final Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(
			new int[] { 10, 10 });

		final IntervalView<DoubleType> il2 = Views.invertAxis(img, 1);
		final IntervalView<DoubleType> opr = invertFunc.apply(img, 1);

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
	}
}
