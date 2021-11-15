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
package net.imagej.ops2.transform.collapseNumericView;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.NativeARGBDoubleType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.CompositeView;
import net.imglib2.view.composite.NumericComposite;

import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;

/**
 * Tests {@link net.imagej.ops2.Ops.Transform.CollapseNumericView} ops.
 * <p>
 * This test only checks if the op call works with all parameters and that the
 * result is equal to that of the {@link Views} method call. It is not a
 * correctness test of {@link Views} itself.
 * </p>
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
public class CollapseNumericViewTest extends AbstractOpTest {
	
	private static CompositeIntervalView<NativeARGBDoubleType, ? extends NumericComposite<NativeARGBDoubleType>> foo() {
		return null;
	}

	@Test
	public void defaultCollapseNumericTest() {

		Img<NativeARGBDoubleType> img = new ArrayImgFactory<>(new NativeARGBDoubleType()).create(new int[] { 10, 10 });
		
		Function<Img<NativeARGBDoubleType>, CompositeIntervalView<NativeARGBDoubleType, NumericComposite<NativeARGBDoubleType>>> collapseFunc = 
				ops.op("transform.collapseNumericView").input(img).outType(new Nil<CompositeIntervalView<NativeARGBDoubleType, NumericComposite<NativeARGBDoubleType>>>() {}).function();

		CompositeIntervalView<NativeARGBDoubleType, NumericComposite<NativeARGBDoubleType>> il2 = Views
				.collapseNumeric((RandomAccessibleInterval<NativeARGBDoubleType>) img);
		CompositeIntervalView<NativeARGBDoubleType, NumericComposite<NativeARGBDoubleType>> opr = collapseFunc
				.apply(img);

		assertEquals(il2.numDimensions(), opr.numDimensions());

		BiFunction<RandomAccessible<NativeARGBDoubleType>, Integer, CompositeView<NativeARGBDoubleType, NumericComposite<NativeARGBDoubleType>>> collapseFuncRA = OpBuilder
				.matchFunction(ops.env(), "transform.collapseNumericView",
						new Nil<RandomAccessible<NativeARGBDoubleType>>() {
				}, new Nil<Integer>() {
				},
						new Nil<CompositeView<NativeARGBDoubleType, NumericComposite<NativeARGBDoubleType>>>() {
				});

		CompositeView<NativeARGBDoubleType, NumericComposite<NativeARGBDoubleType>> il2_2 = Views
				.collapseNumeric((RandomAccessible<NativeARGBDoubleType>) img, 1);
		CompositeView<NativeARGBDoubleType, NumericComposite<NativeARGBDoubleType>> opr_2 = collapseFuncRA.apply(img,
				1);

		assertEquals(il2_2.numDimensions(), opr_2.numDimensions());
	}

}
