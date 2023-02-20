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
//
package net.imagej.ops2.filter.convolve;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

import java.util.concurrent.ExecutorService;

/**
 * Tests involving convolvers.
 */
public class ConvolveTest extends AbstractOpTest {

	@Test
	public void testConvolve() {
		// Verify we can get a ConvolveFFTF op
		var o = ops.op("filter.convolve").inType(new Nil<RandomAccessibleInterval<FloatType>>() {}, new Nil<RandomAccessibleInterval<FloatType>> () {}, Nil.of(FloatType.class
		), Nil.of(ComplexFloatType.class), Nil.of(ExecutorService.class), new Nil<long[]>(){}, new Nil<OutOfBoundsFactory<FloatType, RandomAccessibleInterval<FloatType>>>() {}).outType(new Nil<RandomAccessibleInterval<FloatType>>() {}).computer();
	}
}
//
//	/** Tests that the correct convolver is selected when using a small kernel. */
//	@Test
//	public void testConvolveMethodSelection() {
//
//		final Img<ByteType> in = new ArrayImgFactory<>(new ByteType()).create(new int[] {
//			20, 20 });
//
//		// use a small kernel
//		int[] kernelSize = new int[] { 3, 3 };
//		Img<FloatType> kernel = new ArrayImgFactory<>(new FloatType()).create(kernelSize);
//
////		Op op = ops.op(Ops.Filter.Convolve.class, in, kernel);
//		
//
//		// we should get ConvolveNaive
////		assertSame(ConvolveNaiveF.class, op.getClass());
//
//		// make sure it runs
//		@SuppressWarnings("unchecked")
//		final Img<FloatType> out1 = (Img<FloatType>) ops.op("filter.convolve").input(
//			in, kernel);
//
//		assertEquals(out1.dimension(0), 20);
//
//		// use a bigger kernel
//		kernelSize = new int[] { 30, 30 };
//		kernel = new ArrayImgFactory<>(new FloatType()).create(kernelSize);
//
////		op = ops.op(Ops.Filter.Convolve.class, in, kernel);
//
//		// this time we should get ConvolveFFT
////		assertSame(ConvolveFFTF.class, op.getClass());
//
//		// make sure it runs
//		@SuppressWarnings("unchecked")
//		final Img<FloatType> out2 = (Img<FloatType>) ops.op("filter.convolve").input(in,
//			kernel);
//
//		assertEquals(out2.dimension(0), 20);
//
//	}
//
//	/** tests fft based convolve */
//	@Test
//	public void testConvolve() {
//		ExecutorService es = createContext().getService(ThreadService.class).getExecutorService();
//
//		float delta = 0.0001f;
//
//		int[] size = new int[] { 225, 167 };
//		int[] kernelSize = new int[] { 27, 39 };
//
//		long[] borderSize = new long[] { 10, 10 };
//
//		// create an input with a small sphere at the center
//		Img<FloatType> in = new ArrayImgFactory<>(new FloatType()).create(size);
//		placeSphereInCenter(in);
//
//		// create a kernel with a small sphere in the center
//		Img<FloatType> kernel = new ArrayImgFactory<>(new FloatType()).create(kernelSize);
//		placeSphereInCenter(kernel);
//
//		// create variables to hold the image sums
//		FloatType inSum = new FloatType();
//		FloatType kernelSum = new FloatType();
//		FloatType outSum = new FloatType();
//		FloatType outSum2 = new FloatType();
//		FloatType outSum3 = new FloatType();
//
//		// calculate sum of input and kernel
//		ops.op("stats.sum").input(in, inSum).apply();
//		ops.op("stats.sum").input(kernel, kernelSum).apply();
//
//		// convolve and calculate the sum of output
//		@SuppressWarnings("unchecked")
//		// should match to ConvolveFFTF
//		final Img<FloatType> out = (Img<FloatType>) ops.op("filter.convolve").input(in,
//			kernel, borderSize);
//
//		// create an output for the next test
//		Img<FloatType> out2 = new ArrayImgFactory<>(new FloatType()).create(size);
//
//		// create an output for the next test
//		Img<FloatType> out3 = new ArrayImgFactory<>(new FloatType()).create(size);
//
//		// Op used to pad the input
//		final BinaryFunctionOp<RandomAccessibleInterval<FloatType>, Dimensions, RandomAccessibleInterval<FloatType>> padOp =
//			(BinaryFunctionOp) OpBuilder.matchFunction(ops, PadInputFFTMethods.class,
//				RandomAccessibleInterval.class, RandomAccessibleInterval.class,
//				Dimensions.class, true);
//
//		// Op used to pad the kernel
//		final BinaryFunctionOp<RandomAccessibleInterval<FloatType>, Dimensions, RandomAccessibleInterval<FloatType>> padKernelOp =
//			(BinaryFunctionOp) OpBuilder.matchFunction(ops, PadShiftKernelFFTMethods.class,
//				RandomAccessibleInterval.class, RandomAccessibleInterval.class,
//				Dimensions.class, true);
//
//		// Op used to create the complex FFTs
//		UnaryFunctionOp<Dimensions, RandomAccessibleInterval<ComplexFloatType>> createOp =
//			(UnaryFunctionOp) OpBuilder.matchFunction(ops, CreateOutputFFTMethods.class,
//				RandomAccessibleInterval.class, Dimensions.class,
//				new ComplexFloatType(), true);
//
//		final int numDimensions = in.numDimensions();
//
//		// 1. Calculate desired extended size of the image
//
//		final long[] paddedSize = new long[numDimensions];
//
//		// if no getBorderSize() was passed in, then extend based on kernel size
//		for (int d = 0; d < numDimensions; ++d) {
//			paddedSize[d] = (int) in.dimension(d) + (int) kernel.dimension(d) - 1;
//		}
//
//		RandomAccessibleInterval<FloatType> paddedInput = padOp.calculate(in,
//			new FinalDimensions(paddedSize));
//
//		RandomAccessibleInterval<FloatType> paddedKernel = padKernelOp.calculate(
//			kernel, new FinalDimensions(paddedSize));
//
//		RandomAccessibleInterval<ComplexFloatType> fftImage = createOp.calculate(
//			new FinalDimensions(paddedSize));
//
//		RandomAccessibleInterval<ComplexFloatType> fftKernel = createOp.calculate(
//			new FinalDimensions(paddedSize));
//
//		// run convolve using the rai version with the memory created above
//		ops.op("filter.convolve").input(paddedInput, paddedKernel, fftImage,
//			fftKernel, out2);
//
//		ops.op("filter.convolve").input(paddedInput, paddedKernel, fftImage,
//			fftKernel, true, false, es, out3);
//
//		ops.op("stats.sum").input(Views.iterable(out), outSum).apply();
//		ops.op("stats.sum").input(out2, outSum2).apply();
//		ops.op("stats.sum").input(out3, outSum3).apply();
//
//		// multiply input sum by kernelSum and assert it is the same as outSum
//		inSum.mul(kernelSum);
//
//		assertEquals(inSum.get(), outSum.get(), delta);
//		assertEquals(inSum.get(), outSum2.get(), delta);
//		assertEquals(inSum.get(), outSum3.get(), delta);
//
//		assertEquals(size[0], out.dimension(0));
//		assertEquals(size[0], out2.dimension(0));
//	}
//
//	// utility to place a small sphere at the center of the image
//	private void placeSphereInCenter(Img<FloatType> img) {
//
//		final Point center = new Point(img.numDimensions());
//
//		for (int d = 0; d < img.numDimensions(); d++)
//			center.setPosition(img.dimension(d) / 2, d);
//
//		HyperSphere<FloatType> hyperSphere = new HyperSphere<>(img, center, 2);
//
//		for (final FloatType value : hyperSphere) {
//			value.setReal(1);
//		}
//	}
//
//	/** tests fft based convolve */
//	@Test
//	public void testCreateAndConvolvePoints() {
//
//		final int xSize = 128;
//		final int ySize = 128;
//		final int zSize = 128;
//
//		int[] size = new int[] { xSize, ySize, zSize };
//
//		Img<DoubleType> phantom = (Img<DoubleType>) ops.op("create.img").input(size).apply();
//
//		RandomAccess<DoubleType> randomAccess = phantom.randomAccess();
//
//		randomAccess.setPosition(new long[] { xSize / 2, ySize / 2, zSize / 2 });
//		randomAccess.get().setReal(255.0);
//
//		randomAccess.setPosition(new long[] { xSize / 4, ySize / 4, zSize / 4 });
//		randomAccess.get().setReal(255.0);
//
//		Point location = new Point(phantom.numDimensions());
//		location.setPosition(new long[] { 3 * xSize / 4, 3 * ySize / 4, 3 * zSize /
//			4 });
//
//		HyperSphere<DoubleType> hyperSphere = new HyperSphere<>(phantom, location,
//			5);
//
//		for (DoubleType value : hyperSphere) {
//			value.setReal(16);
//		}
//
//		// create psf using the gaussian kernel op (alternatively PSF could be an
//		// input to the script)
//		RandomAccessibleInterval<DoubleType> psf = (RandomAccessibleInterval<DoubleType>) ops.run("create.kernelGauss",
//			new double[] { 5, 5, 5 }, new DoubleType());
//
//		// convolve psf with phantom
//		RandomAccessibleInterval<DoubleType> convolved = (RandomAccessibleInterval<DoubleType>) ops.run("filter.convolve",
//			phantom, psf);
//
//		DoubleType sum = new DoubleType();
//		DoubleType max = new DoubleType();
//		DoubleType min = new DoubleType();
//
//		ops.op("stats.sum").input(Views.iterable(convolved), sum).apply();
//		ops.op("stats.max").input(Views.iterable(convolved), max).apply();
//		ops.op("stats.min").input(Views.iterable(convolved), min).apply();
//
//		assertEquals(sum.getRealDouble(), 8750.00, 0.001);
//		assertEquals(max.getRealDouble(), 3.155, 0.001);
//		assertEquals(min.getRealDouble(), 2.978E-7, 0.001);
//
//	}
