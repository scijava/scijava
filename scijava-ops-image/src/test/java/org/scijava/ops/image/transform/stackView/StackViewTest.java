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

package org.scijava.ops.image.transform.stackView;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.StackView.StackAccessMode;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;

/**
 * Tests {@link org.scijava.ops.image.Ops.Transform.StackView} ops.
 * <p>
 * This test only checks if the op call works with all parameters and that the
 * result is equal to that of the {@link Views} method call. It is not a
 * correctness test of {@link Views} itself.
 * </p>
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
public class StackViewTest extends AbstractOpTest {

	@Test
	public void defaultStackTest() {
		Function<List<Img<DoubleType>>, RandomAccessibleInterval<DoubleType>> stackFunc =
			OpBuilder.matchFunction(ops, "transform.stackView",
				new Nil<List<Img<DoubleType>>>()
				{}, new Nil<RandomAccessibleInterval<DoubleType>>() {});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(
			new int[] { 10, 10 });

		List<Img<DoubleType>> list = new ArrayList<>();
		list.add(img);
		list.add(img);

		RandomAccessibleInterval<DoubleType> il2 = Views.stack(list);
		RandomAccessibleInterval<DoubleType> opr = stackFunc.apply(list);

		assertEquals(il2.dimension(2), opr.dimension(2));
	}

	@Test
	public void stackWithAccessModeTest() {

		BiFunction<StackAccessMode, List<Img<DoubleType>>, RandomAccessibleInterval<DoubleType>> stackFunc =
			OpBuilder.matchFunction(ops, "transform.stackView",
				new Nil<StackAccessMode>()
				{}, new Nil<List<Img<DoubleType>>>() {},
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(
			new int[] { 10, 10 });

		List<Img<DoubleType>> list = new ArrayList<>();
		list.add(img);
		list.add(img);

		RandomAccessibleInterval<DoubleType> il2 = Views.stack(
			StackAccessMode.DEFAULT, list);
		RandomAccessibleInterval<DoubleType> opr = stackFunc.apply(
			StackAccessMode.DEFAULT, list);

		assertEquals(il2.dimension(2), opr.dimension(2));
	}

}
