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

package net.imagej.ops.create;

import static org.junit.Assert.assertEquals;

import java.util.function.BiFunction;

import net.imagej.ops.create.kernel.DefaultCreateKernelGabor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.function.Functions;
import org.scijava.ops.types.Nil;
import org.scijava.ops.function.Functions;

/**
 * Tests {@link DefaultCreateKernelGabor} and its derivates.
 * 
 * @author Vladimír Ulman
 */
public class CreateKernelGaborTest extends AbstractTestEnvironment {

	@Test
	public <C extends ComplexType<C>> void testKernelGabor() {
		final double sigma = 3.0;
		final double[] sigmas = { 2.0 * sigma, sigma };
		final double[] period = { 4.0, 1.0 };

		// define functions used in the test
		Functions.Arity3<double[], double[], C, RandomAccessibleInterval<C>> createFunc = Functions.match(ops,
				"create.kernelGabor", new Nil<double[]>() {
				}, new Nil<double[]>() {
				}, new Nil<C>() {
				}, new Nil<RandomAccessibleInterval<C>>() {
				});
		BiFunction<Double, double[], RandomAccessibleInterval<DoubleType>> createFuncSingleSigma = Functions
				.binary(ops, "create.kernelGabor", new Nil<Double>() {
				}, new Nil<double[]>() {
				}, new Nil<RandomAccessibleInterval<DoubleType>>() {
				});
		BiFunction<double[], double[], RandomAccessibleInterval<DoubleType>> createFuncDouble = Functions.match(ops,
				"create.kernelGabor", new Nil<double[]>() {
				}, new Nil<double[]>() {
				}, new Nil<RandomAccessibleInterval<DoubleType>>() {
				});
		BiFunction<double[], double[], RandomAccessibleInterval<FloatType>> createFuncFloat = Functions.match(ops,
				"create.kernelGabor", new Nil<double[]>() {
				}, new Nil<double[]>() {
				}, new Nil<RandomAccessibleInterval<FloatType>>() {
				});
		BiFunction<double[], double[], RandomAccessibleInterval<ComplexDoubleType>> createFuncComplexDouble = Functions.match(ops,
				"create.kernelGabor", new Nil<double[]>() {
				}, new Nil<double[]>() {
				}, new Nil<RandomAccessibleInterval<ComplexDoubleType>>() {
				});
		
		// test the main convenience function:
		RandomAccessibleInterval<DoubleType> kernelD = createFuncDouble.apply(sigmas, period);

		// sizes are okay?
		assertEquals(kernelD.dimension(0), 37);
		assertEquals(kernelD.dimension(1), 19);

		// is 1.0 in the image centre?
		long[] position = { kernelD.dimension(0) / 2, kernelD.dimension(1) / 2 };
		RandomAccess<DoubleType> samplerD = kernelD.randomAccess();
		samplerD.setPosition(position);
		assertEquals(1.0, samplerD.get().getRealDouble(), 0.00001);

		// is consistency checking okay?
		int wasCaught = 0;
		final double[] shortSigmas = { 2.0 * sigma };
		try {
			kernelD = createFuncDouble.apply(shortSigmas, period);
		} catch (IllegalArgumentException e) {
			++wasCaught;
		}
		try {
			kernelD = createFuncSingleSigma.apply(-sigma, period);
		} catch (IllegalArgumentException e) {
			++wasCaught;
		}
		assertEquals(2, wasCaught);

		// does it work also for pure complex types?
		RandomAccessibleInterval<ComplexDoubleType> kernelCD = createFuncComplexDouble.apply(sigmas, period);
		RandomAccess<ComplexDoubleType> samplerCD = kernelCD.randomAccess();
		samplerCD.setPosition(position);
		assertEquals(samplerD.get().getRealDouble(), samplerCD.get().getRealDouble(), 0.00001);

		// imaginary part should be around 0.0 in the kernel centre, is it?
		assertEquals(0.0, samplerCD.get().getImaginaryDouble(), 0.001);

		// and also after one period?
		position[0] += period[0];
		position[1] += period[1];
		samplerCD.setPosition(position);
		assertEquals(0.0, samplerCD.get().getImaginaryDouble(), 0.001);

		// does the general kernel calculation work?
		kernelCD = (RandomAccessibleInterval<ComplexDoubleType>) createFunc.apply(sigmas, period, (C) new ComplexDoubleType());
		samplerCD = kernelCD.randomAccess();
		samplerCD.setPosition(position);
		assertEquals(0.0, samplerCD.get().getImaginaryDouble(), 0.001);

		// 3D kernel for just y-axis?
		final double[] sigmas3D = { 0.0, 5.0, 0.0 };
		final double[] period3D = { 0.0, 2.0, 0.0 };
		final RandomAccessibleInterval<FloatType> kernelF = createFuncFloat.apply(sigmas3D, period3D);
		RandomAccess<FloatType> samplerF = kernelF.randomAccess();

		// minimal size in x and z axes?
		assertEquals(3, kernelF.dimension(0));
		assertEquals(3, kernelF.dimension(2));

		// is zero anywhere off the y-axis?
		position = new long[3];
		position[0] = 0;
		position[1] = kernelF.dimension(1) / 2;
		position[2] = 1;
		samplerF.setPosition(position);
		assertEquals(0.f, samplerF.get().getRealFloat(), 0.001f);

		// is zero anywhere off the y-axis?
		position[0] = 1;
		position[2] = 0;
		samplerF.setPosition(position);
		assertEquals(0.f, samplerF.get().getRealFloat(), 0.001f);

		// is positive on the y-axis (in the image centre)?
		position[2] = 1;
		samplerF.setPosition(position);
		assertEquals(10.f, samplerF.get().getRealFloat(), 9.99f);
	}
}
