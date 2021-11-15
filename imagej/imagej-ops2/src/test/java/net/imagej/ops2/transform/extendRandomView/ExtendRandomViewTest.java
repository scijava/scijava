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
package net.imagej.ops2.transform.extendRandomView;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.function.Functions;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;

/**
 * Tests {@link net.imagej.ops2.Ops.Transform.ExtendRandomView} ops.
 * <p>
 * This test only checks if the op call works with all parameters and that the
 * result is equal to that of the {@link Views} method call. It is not a
 * correctness test of {@link Views} itself.
 * </p>
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
public class ExtendRandomViewTest extends AbstractOpTest {

	Nil<RandomAccessibleInterval<DoubleType>> raiNil = new Nil<RandomAccessibleInterval<DoubleType>>() {
	};
	Nil<Double> doubleNil = new Nil<Double>() {
	};

	@Test
	public void extendRandomTest() {
		Functions.Arity3<RandomAccessibleInterval<DoubleType>, Double, Double, RandomAccessible<DoubleType>> extendFunc = OpBuilder
				.matchFunction(ops.env(), "transform.extendRandomView", raiNil, doubleNil, doubleNil,
						new Nil<RandomAccessible<DoubleType>>() {
				});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(new int[] { 10, 10 });

		RandomAccess<DoubleType> il2 = Views.extendRandom(img, 0, 0).randomAccess();

		RandomAccess<DoubleType> opr = extendFunc.apply(img, 0.0, 0.0).randomAccess();

		il2.setPosition(new int[] { -1, -1 });
		opr.setPosition(new int[] { -1, -1 });

		assertEquals(il2.get().get(), opr.get().get(), 1e-10);

		il2.setPosition(new int[] { 11, 11 });
		opr.setPosition(new int[] { 11, 11 });

		assertEquals(il2.get().get(), opr.get().get(), 1e-10);
	}

}
