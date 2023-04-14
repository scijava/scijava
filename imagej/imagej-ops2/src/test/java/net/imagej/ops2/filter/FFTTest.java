/* #%L
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

package net.imagej.ops2.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ExecutorService;

import net.imagej.ops2.AbstractOpTest;
import net.imagej.testutil.TestImgGeneration;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.IterableInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Test FFT implementations
 * 
 * @author Brian Northan
 */
public class FFTTest extends AbstractOpTest {

	private final boolean expensiveTestsEnabled = "enabled".equals(System.getProperty("imagej.ops.expensive.tests"));

	/**
	 * test that a forward transform followed by an inverse transform gives us back
	 * the original image
	 */
	@Test
	public void testFFT3DOp() {
		ExecutorService es = threads.getExecutorService();
		final int min = expensiveTestsEnabled ? 115 : 9;
		final int max = expensiveTestsEnabled ? 120 : 11;
		for (int i = min; i < max; i++) {

			final long[] dimensions = new long[] { i, i, i };

			// create an input with a small sphere at the center
			final Img<FloatType> in = TestImgGeneration.floatArray(false, dimensions);
			placeSphereInCenter(in);

			final Img<FloatType> inverse = TestImgGeneration.floatArray(false, dimensions);

			final RandomAccessibleInterval<ComplexFloatType> out = ops.op("filter.fft")
					.arity5().input(in, null, true, new ComplexFloatType(), es)
					.outType(new Nil<RandomAccessibleInterval<ComplexFloatType>>() {}).apply();
			ops.op("filter.ifft").arity2().input(out, es).output(inverse).compute();

			assertImagesEqual(in, inverse, .00005f);
		}

	}

	/**
	 * test the fast FFT
	 */
	@Test
	public void testFastFFT3DOp() {

		ExecutorService es = threads.getExecutorService();

		final int min = expensiveTestsEnabled ? 120 : 9;
		final int max = expensiveTestsEnabled ? 130 : 11;
		final int size = expensiveTestsEnabled ? 129 : 10;
		for (int i = min; i < max; i++) {

			// define the original dimensions
			final long[] originalDimensions = new long[] { i, size, size };

			// arrays for the fast dimensions
			long[] fastDimensions = new long[3];
			long[] fftDimensions = new long[3];

			// compute the dimensions that will result in the fastest FFT time
			Pair<long[], long[]> fftSize = ops.op("filter.fftSize")
					.arity5().input(new FinalDimensions(originalDimensions), fastDimensions, fftDimensions, true, true)
					.outType(new Nil<Pair<long[], long[]>>() {}).apply();

			fastDimensions = fftSize.getA();
			fftDimensions = fftSize.getB();

			// create an input with a small sphere at the center
			final Img<FloatType> inOriginal = TestImgGeneration.floatArray(false, originalDimensions);
			placeSphereInCenter(inOriginal);

			// create a similar input using the fast size
			final Img<FloatType> inFast = TestImgGeneration.floatArray(false, fastDimensions);
			placeSphereInCenter(inFast);

			// call FFT passing false for "fast" (in order to pass the optional
			// parameter we have to pass null for the
			// output parameter).
			final RandomAccessibleInterval<ComplexFloatType> fft1 = ops.op("filter.fft")
					.arity5().input(inOriginal, null, false, new ComplexFloatType(), es)
					.outType(new Nil<RandomAccessibleInterval<ComplexFloatType>>() {}).apply();

			// call FFT passing true for "fast" The FFT op will pad the input to the
			// fast
			// size.
			final RandomAccessibleInterval<ComplexFloatType> fft2 = ops.op("filter.fft")
					.arity5().input(inOriginal, null, true, new ComplexFloatType(), es)
					.outType(new Nil<RandomAccessibleInterval<ComplexFloatType>>() {}).apply();

			// call fft using the img that was created with the fast size
			final RandomAccessibleInterval<ComplexFloatType> fft3 = ops.op("filter.fft")
					.arity5().input(inFast, null, true, new ComplexFloatType(), es)
					.outType(new Nil<RandomAccessibleInterval<ComplexFloatType>>() {}).apply();

			// create an image to be used for the inverse, using the original
			// size
			final Img<FloatType> inverseOriginalSmall = TestImgGeneration.floatArray(false, originalDimensions);

			// create an inverse image to be used for the inverse, using the
			// original
			// size
			final Img<FloatType> inverseOriginalFast = TestImgGeneration.floatArray(false, originalDimensions);

			// create an inverse image to be used for the inverse, using the
			// fast size
			final Img<FloatType> inverseFast = TestImgGeneration.floatArray(false, fastDimensions);

			// invert the "small" FFT
			ops.op("filter.ifft").arity2().input(fft1, es).output(inverseOriginalSmall).compute();

			// invert the "fast" FFT. The inverse will should be the original
			// size.
			ops.op("filter.ifft").arity2().input(fft2, es).output(inverseOriginalFast).compute();

			// invert the "fast" FFT that was acheived by explicitly using an
			// image
			// that had "fast" dimensions. The inverse will be the fast size
			// this
			// time.
			ops.op("filter.ifft").arity2().input(fft3, es).output(inverseFast).compute();

			// assert that the inverse images are equal to the original
			assertImagesEqual(inverseOriginalSmall, inOriginal, .0001f);
			assertImagesEqual(inverseOriginalFast, inOriginal, .00001f);
			assertImagesEqual(inverseFast, inFast, 0.00001f);
		}
	}

	@Test
	public void testPadShiftKernel() {
		long[] dims = new long[] { 1024, 1024 };
		Img<ComplexDoubleType> test = ops.op("create.img").arity2().input(new FinalDimensions(dims), new ComplexDoubleType())
				.outType(new Nil<Img<ComplexDoubleType>>() {}).apply();

		RandomAccessibleInterval<ComplexDoubleType> shift = ops.op("filter.padShiftKernel")
				.arity2().input(test, new FinalDimensions(dims))
				.outType(new Nil<RandomAccessibleInterval<ComplexDoubleType>>() {}).apply();

		RandomAccessibleInterval<ComplexDoubleType> shift2 = ops.op("filter.padShiftKernelFFTMethods")
				.arity2().input(test, new FinalDimensions(dims))
				.outType(new Nil<RandomAccessibleInterval<ComplexDoubleType>>() {}).apply();

		// assert there was no additional padding done by PadShiftKernel
		assertEquals(1024, shift.dimension(0));
		// assert that PadShiftKernelFFTMethods padded to the FFTMethods fast size
		assertEquals(1120, shift2.dimension(0));

	}

	/**
	 * utility that places a sphere in the center of the image
	 * 
	 * @param img
	 */
	private void placeSphereInCenter(final Img<FloatType> img) {

		final Point center = new Point(img.numDimensions());

		for (int d = 0; d < img.numDimensions(); d++)
			center.setPosition(img.dimension(d) / 2, d);

		final HyperSphere<FloatType> hyperSphere = new HyperSphere<>(img, center, 2);

		for (final FloatType value : hyperSphere) {
			value.setReal(1);
		}
	}

	/**
	 * a utility to assert that two images are equal
	 * 
	 * @param img1
	 * @param img2
	 * @param delta
	 */
	protected void assertImagesEqual(final Img<FloatType> img1, final Img<FloatType> img2, final float delta) {
		final Cursor<FloatType> c1 = img1.cursor();
		final Cursor<FloatType> c2 = img2.cursor();

		while (c1.hasNext()) {
			c1.fwd();
			c2.fwd();

			// assert that the inverse = the input within the error delta
			assertEquals(c1.get().getRealFloat(), c2.get().getRealFloat(), delta);
		}

	}

	// a utility to assert that two rais are equal
	protected void assertRAIsEqual(final RandomAccessibleInterval<FloatType> rai1,
			final RandomAccessibleInterval<FloatType> rai2, final float delta) {
		final IterableInterval<FloatType> rai1Iterator = Views.iterable(rai1);
		final IterableInterval<FloatType> rai2Iterator = Views.iterable(rai2);

		final Cursor<FloatType> c1 = rai1Iterator.cursor();
		final Cursor<FloatType> c2 = rai2Iterator.cursor();

		while (c1.hasNext()) {
			c1.fwd();
			c2.fwd();

			// assert that the inverse = the input within the error delta
			assertEquals(c1.get().getRealFloat(), c2.get().getRealFloat(), delta);
		}
	}

	// a utility to assert that two images are equal
	protected void assertComplexImagesEqual(final Img<ComplexFloatType> img1, final Img<ComplexFloatType> img2,
			final float delta) {
		final Cursor<ComplexFloatType> c1 = img1.cursor();
		final Cursor<ComplexFloatType> c2 = img2.cursor();

		while (c1.hasNext()) {
			c1.fwd();
			c2.fwd();

			// assert that the inverse = the input within the error delta
			assertEquals(c1.get().getRealFloat(), c2.get().getRealFloat(), delta);
			// assert that the inverse = the input within the error delta
			assertEquals(c1.get().getImaginaryFloat(), c2.get().getImaginaryFloat(), delta);
		}

	}

}
