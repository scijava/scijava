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

package org.scijava.ops.image.transform.interpolateView;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiFunction;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.interpolation.randomaccess.FloorInterpolatorFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;
import org.scijava.util.MersenneTwisterFast;

/**
 * Tests {@link org.scijava.ops.image.Ops.Transform.InterpolateView} ops.
 * <p>
 * This test only checks if the op call works with all parameters and that the
 * result is equal to that of the {@link Views} method call. It is not a
 * correctness test of {@link Views} itself.
 * </p>
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
public class InterpolateViewTest extends AbstractOpTest {

	private static final long SEED = 0x12345678;

	@Test
	public void defaultInterpolateTest() {

		BiFunction<RandomAccessible<DoubleType>, FloorInterpolatorFactory<DoubleType>, RealRandomAccessible<DoubleType>> interpolateFunc =
			OpBuilder.matchFunction(ops, "transform.interpolateView",
				new Nil<RandomAccessible<DoubleType>>()
				{}, new Nil<FloorInterpolatorFactory<DoubleType>>() {},
				new Nil<RealRandomAccessible<DoubleType>>()
				{});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(
			new int[] { 10, 10 });
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		for (DoubleType d : img) {
			d.set(r.nextDouble());
		}

		RealRandomAccess<DoubleType> il2 = Views.interpolate(img,
			new FloorInterpolatorFactory<DoubleType>()).realRandomAccess();
		RealRandomAccess<DoubleType> opr = interpolateFunc.apply(img,
			new FloorInterpolatorFactory<DoubleType>()).realRandomAccess();

		il2.setPosition(new double[] { 1.75, 5.34 });
		opr.setPosition(new double[] { 1.75, 5.34 });
		assertEquals(il2.get().get(), opr.get().get(), 1e-10);

		il2.setPosition(new double[] { 3, 7 });
		opr.setPosition(new double[] { 3, 7 });
		assertEquals(il2.get().get(), opr.get().get(), 1e-10);

		il2.setPosition(new double[] { 8.37, 3.97 });
		opr.setPosition(new double[] { 8.37, 3.97 });
		assertEquals(il2.get().get(), opr.get().get(), 1e-10);
	}
}
