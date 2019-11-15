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

package net.imagej.ops.threshold.apply;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalHuang;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalIJ1;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalIntermodes;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalIsoData;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalLi;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalMaxEntropy;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalMaxLikelihood;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalMinError;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalMinimum;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalMoments;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalOtsu;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalPercentile;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalRenyiEntropy;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalRosin;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalShanbhag;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalTriangle;
import net.imagej.ops.threshold.ApplyThresholdMethodLocal.LocalYen;
import net.imagej.ops.threshold.LocalThresholdMethod;
import net.imagej.ops.threshold.localBernsen.LocalBernsenThreshold;
import net.imagej.ops.threshold.localContrast.LocalContrastThreshold;
import net.imagej.ops.threshold.localMean.LocalMeanThreshold;
import net.imagej.ops.threshold.localMedian.LocalMedianThreshold;
import net.imagej.ops.threshold.localNiblack.LocalNiblackThreshold;
import net.imagej.ops.threshold.localSauvola.LocalSauvolaThreshold;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;

import org.junit.Before;
import org.junit.Test;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers;
import org.scijava.ops.types.Nil;
import org.scijava.ops.function.Computers;

/**
 * Test for {@link LocalThreshold} and various {@link LocalThresholdMethod}s.
 *
 * @author Jonathan Hale
 * @author Martin Horn
 * @see LocalThreshold
 * @see LocalThresholdMethod
 */
public class LocalThresholdTest extends AbstractOpTest {

	Img<ByteType> in;
	Img<DoubleType> normalizedIn;
	Img<BitType> out;

	/**
	 * Initialize images.
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Before
	public void before() throws Exception {
		in = generateByteArrayTestImg(true, new long[] { 10, 10 });
		Pair<ByteType, ByteType> minMax = (Pair<ByteType, ByteType>) ops.run("stats.minMax", in);
		normalizedIn = (Img<DoubleType>) ops.run("image.normalize", in,
			minMax.getA(), minMax.getB(), new DoubleType(0.0), new DoubleType(1.0));

		out = in.factory().imgFactory(new BitType()).create(in, new BitType());
	}

//	/**
//	 * Test whether parameters for ops in {@link ThresholdNamespace} opmethods are
//	 * correctly set.
//	 */
//	@Test
//	public void testOpMethods() {
//		ops.run(out, in, new RectangleShape(3, false),
//			0.0);
//		ops.threshold().localMeanThreshold(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE), 0.0);
//		ops.threshold().localMeanThreshold(out, in, new DiamondShape(3), 0.0);
//		ops.threshold().localMeanThreshold(out, in, new DiamondShape(3),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE), 0.0);
//
//		ops.threshold().localBernsenThreshold(out, in, new RectangleShape(3, false),
//			1.0, Double.MAX_VALUE * 0.5);
//		ops.threshold().localBernsenThreshold(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE), 1.0, Double.MAX_VALUE * 0.5);
//
//		ops.threshold().localContrastThreshold(out, in, new RectangleShape(3,
//			false));
//		ops.threshold().localContrastThreshold(out, in, new RectangleShape(3,
//			false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//
//		ops.threshold().localMedianThreshold(out, in, new RectangleShape(3, false),
//			1.0);
//		ops.threshold().localMedianThreshold(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE), 1.0);
//
//		ops.threshold().localMidGreyThreshold(out, in, new RectangleShape(3, false),
//			1.0);
//		ops.threshold().localMidGreyThreshold(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE), 1.0);
//
//		ops.threshold().localNiblackThreshold(out, in, new RectangleShape(3, false),
//			1.0, 2.0);
//		ops.threshold().localNiblackThreshold(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE), 1.0, 2.0);
//
//		ops.threshold().localPhansalkarThreshold(out, in, new RectangleShape(3,
//			false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE), 0.25, 0.5);
//		ops.threshold().localPhansalkarThreshold(out, in, new RectangleShape(3,
//			false));
//
//		ops.threshold().localSauvolaThreshold(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE), 0.5, 0.5);
//		ops.threshold().localSauvolaThreshold(out, in, new RectangleShape(3,
//			false));
//
//		/* Locally applied global threshold ops */
//		ops.threshold().huang(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().huang(out, in, new RectangleShape(3, false));
//
//		ops.threshold().ij1(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().ij1(out, in, new RectangleShape(3, false));
//
//		ops.threshold().intermodes(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().intermodes(out, in, new RectangleShape(3, false));
//
//		ops.threshold().isoData(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().isoData(out, in, new RectangleShape(3, false));
//
//		ops.threshold().li(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().li(out, in, new RectangleShape(3, false));
//
//		ops.threshold().maxEntropy(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().maxEntropy(out, in, new RectangleShape(3, false));
//
//		ops.threshold().maxLikelihood(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().maxLikelihood(out, in, new RectangleShape(3, false));
//
//		ops.threshold().minError(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().minError(out, in, new RectangleShape(3, false));
//
//		ops.threshold().minimum(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().minimum(out, in, new RectangleShape(3, false));
//
//		ops.threshold().moments(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().moments(out, in, new RectangleShape(3, false));
//
//		ops.threshold().otsu(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().otsu(out, in, new RectangleShape(3, false));
//
//		ops.threshold().percentile(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().percentile(out, in, new RectangleShape(3, false));
//
//		ops.threshold().renyiEntropy(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().renyiEntropy(out, in, new RectangleShape(3, false));
//
//		ops.threshold().shanbhag(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().shanbhag(out, in, new RectangleShape(3, false));
//
//		ops.threshold().triangle(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().triangle(out, in, new RectangleShape(3, false));
//
//		ops.threshold().yen(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().yen(out, in, new RectangleShape(3, false));
//
//		ops.threshold().rosin(out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
//				Boundary.SINGLE));
//		ops.threshold().rosin(out, in, new RectangleShape(3, false));
//	}

	/**
	 * @see LocalBernsenThreshold
	 */
	@Test
	public void testLocalBernsenThreshold() {
		final Computers.Arity5<RandomAccessibleInterval<ByteType>, Shape, Double, Double, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.localBernsen", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(3, false), 1.0, Double.MAX_VALUE *
			0.5,
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

	/**
	 * @see LocalContrastThreshold
	 */
	@Test
	public void testLocalContrastThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.localContrast", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(3, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

	/**
	 * @see LocalHuang
	 */
	@Test
	public void testLocalHuangThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.huang", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

	/**
	 * @see LocalIJ1
	 */
	@Test
	public void testLocalIJ1Threshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.ij1", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

	/**
	 * @see LocalIntermodes
	 */
	@Test
	public void testLocalIntermodesThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.intermodes", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

	/**
	 * @see LocalIsoData
	 */
	@Test
	public void testLocalIsoDataThreshold() {
		// NB: Test fails for RectangleShapes of span 1
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.isoData", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(2, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

	/**
	 * @see LocalLi
	 */
	@Test
	public void testLocalLiThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.li", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

	/**
	 * @see LocalMaxEntropy
	 */
	@Test
	public void testLocalMaxEntropyThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.maxEntropy", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

	/**
	 * @see LocalMaxLikelihood
	 */
	@Test
	public void testLocalMaxLikelihoodThreshold() {
		// NB: Test fails for RectangleShapes of up to span==2
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.maxLikelihood", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(3, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

	/**
	 * @see LocalMeanThreshold
	 */
	@Test
	public void testLocalThresholdMean() {
		final Computers.Arity4<RandomAccessibleInterval<ByteType>, Shape, Double, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.localMean", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false), 0d,
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

//
//	/**
//	 * @see LocalMeanThresholdIntegral
//	 */
//	@Test
//	public void testLocalMeanThresholdIntegral() {
//		ops.run(LocalMeanThresholdIntegral.class, out, in, new RectangleShape(3,
//			false), new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(
//				Boundary.SINGLE), 0.0);
//
//		assertEquals(true, out.firstElement().get());
//	}
//
//	/**
//	 * @see LocalMeanThresholdIntegral
//	 * @see LocalMeanThreshold
//	 */
//	@Test
//	public void testLocalMeanResultsConsistency() {
//		Img<BitType> out2 = null;
//		Img<BitType> out3 = null;
//		try {
//			out2 = in.factory().imgFactory(new BitType()).create(in, new BitType());
//			out3 = in.factory().imgFactory(new BitType()).create(in, new BitType());
//		}
//		catch (IncompatibleTypeException exc) {
//			exc.printStackTrace();
//		}
//
//		// Default implementation
//		ops.run(LocalMeanThreshold.class, out2, in, new RectangleShape(2, false),
//			new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(Boundary.SINGLE),
//			0.0);
//
//		// Integral image-based implementation
//		ops.run(LocalMeanThresholdIntegral.class, out3, in, new RectangleShape(2,
//			false), new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(
//				Boundary.SINGLE), 0.0);
//
//		testIterableIntervalSimilarity(out2, out3);
//	}
//
	/**
	 * @see LocalMedianThreshold
	 */
	@Test
	public void testLocalMedianThreshold() {
		final Computers.Arity4<RandomAccessibleInterval<ByteType>, Shape, Double, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.localMedian", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(3, false), 0d,
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

	/**
	 * @see LocalMidGrey
	 */
	@Test
	public void testLocalMidGreyThreshold() {
		final Computers.Arity4<RandomAccessibleInterval<ByteType>, Shape, Double, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.localMidGrey", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(3, false), 0d,
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

	/**
	 * @see LocalMinError
	 */
	@Test
	public void testLocalMinErrorThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.minError", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

	/**
	 * @see LocalMinimum
	 */
	@Test
	public void testLocalMinimumThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.minimum", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

	/**
	 * @see LocalMoments
	 */
	@Test
	public void testLocalMomentsThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.moments", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

	/**
	 * @see LocalNiblackThreshold
	 */
	@Test
	public void testLocalNiblackThreshold() {
		final Computers.Arity5<RandomAccessibleInterval<ByteType>, Shape, Double, Double, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.localNiblack", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false), 0.2, 0.0,
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

//	/**
//	 * @see LocalNiblackThresholdIntegral
//	 */
//	@Test
//	public void testLocalNiblackThresholdIntegral() {
//		ops.run(LocalNiblackThresholdIntegral.class, out, in, new RectangleShape(3,
//			false), new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(
//				Boundary.SINGLE), 0.2, 0.0);
//
//		assertEquals(true, out.firstElement().get());
//	}
//
//	/**
//	 * @see LocalNiblackThresholdIntegral
//	 * @see LocalNiblackThreshold
//	 */
//	@Test
//	public void testLocalNiblackResultsConsistency() {
//		Img<BitType> out2 = null;
//		Img<BitType> out3 = null;
//		try {
//			out2 = in.factory().imgFactory(new BitType()).create(in, new BitType());
//			out3 = in.factory().imgFactory(new BitType()).create(in, new BitType());
//		}
//		catch (IncompatibleTypeException exc) {
//			exc.printStackTrace();
//		}
//
//		// Default implementation
//		ops.run(LocalNiblackThreshold.class, out2, normalizedIn, new RectangleShape(
//			2, false), new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(
//				Boundary.SINGLE), 0.2, 1.0);
//
//		// Integral image-based implementation
//		ops.run(LocalNiblackThresholdIntegral.class, out3, normalizedIn,
//			new RectangleShape(2, false),
//			new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(Boundary.SINGLE),
//			0.2, 1.0);
//
//		testIterableIntervalSimilarity(out2, out3);
//	}

	/**
	 * @see LocalOtsu
	 */
	@Test
	public void testLocalOtsuThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.otsu", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

	/**
	 * @see LocalPercentile
	 */
	@Test
	public void testLocalPercentileThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.percentile", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

	/**
	 * @see LocalPhansalkar
	 */
	@Test
	public void testLocalPhansalkar() {
		final Computers.Arity5<RandomAccessibleInterval<DoubleType>, Shape, Double, Double, //
				OutOfBoundsFactory<DoubleType, RandomAccessibleInterval<DoubleType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.localPhansalkar", //
						new Nil<RandomAccessibleInterval<DoubleType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<OutOfBoundsFactory<DoubleType, RandomAccessibleInterval<DoubleType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(normalizedIn, new RectangleShape(2, false), 0.25, 0.5,
			new OutOfBoundsMirrorFactory<DoubleType, RandomAccessibleInterval<DoubleType>>(
				Boundary.SINGLE), out);

		assertEquals(true, out.firstElement().get());
	}

//	/**
//	 * @see LocalPhansalkarThresholdIntegral
//	 */
//	@Test
//	public void testLocalPhansalkarIntegral() {
//		ops.run(LocalPhansalkarThresholdIntegral.class, out, normalizedIn,
//			new RectangleShape(2, false),
//			new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(Boundary.SINGLE),
//			0.25, 0.5);
//
//		assertEquals(true, out.firstElement().get());
//	}
//
//	/**
//	 * @see LocalPhansalkarThresholdIntegral
//	 * @see LocalPhansalkarThreshold
//	 */
//	@Test
//	public void testLocalPhansalkarResultsConsistency() {
//		Img<BitType> out2 = null;
//		Img<BitType> out3 = null;
//		try {
//			out2 = in.factory().imgFactory(new BitType()).create(in, new BitType());
//			out3 = in.factory().imgFactory(new BitType()).create(in, new BitType());
//		}
//		catch (final IncompatibleTypeException exc) {
//			exc.printStackTrace();
//		}
//
//		// Default implementation
//		ops.run(LocalPhansalkarThreshold.class, out2, normalizedIn,
//			new RectangleShape(2, false),
//			new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(Boundary.SINGLE),
//			0.25, 0.5);
//
//		// Integral image-based implementation
//		ops.run(LocalPhansalkarThresholdIntegral.class, out3, normalizedIn,
//			new RectangleShape(2, false),
//			new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(Boundary.SINGLE),
//			0.25, 0.5);
//
//		testIterableIntervalSimilarity(out2, out3);
//	}

	/**
	 * @see LocalRenyiEntropy
	 */
	@Test
	public void testLocalRenyiEntropyThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.renyiEntropy", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

	/**
	 * @see LocalSauvolaThreshold
	 */
	@Test
	public void testLocalSauvola() {
		final Computers.Arity5<RandomAccessibleInterval<ByteType>, Shape, Double, Double, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.localSauvola", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<Double>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(2, false), 0.5, 0.5,
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

//	/**
//	 * @see LocalSauvolaThresholdIntegral
//	 */
//	@Test
//	public void testLocalSauvolaIntegral() {
//		ops.run(LocalSauvolaThresholdIntegral.class, out, in, new RectangleShape(2,
//			false), new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(
//				Boundary.SINGLE), 0.5, 0.5);
//
//		assertEquals(false, out.firstElement().get());
//	}
//
//	/**
//	 * @see LocalSauvolaThresholdIntegral
//	 * @see LocalSauvolaThreshold
//	 */
//	@Test
//	public void testLocalSauvolaResultsConsistency() {
//		Img<BitType> out2 = null;
//		Img<BitType> out3 = null;
//		try {
//			out2 = in.factory().imgFactory(new BitType()).create(in, new BitType());
//			out3 = in.factory().imgFactory(new BitType()).create(in, new BitType());
//		}
//		catch (IncompatibleTypeException exc) {
//			exc.printStackTrace();
//		}
//
//		// Default implementation
//		ops.run(LocalSauvolaThreshold.class, out2, normalizedIn, new RectangleShape(
//			2, false), new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(
//				Boundary.SINGLE), 0.5, 0.5);
//
//		// Integral image-based implementation
//		ops.run(LocalSauvolaThresholdIntegral.class, out3, normalizedIn,
//			new RectangleShape(2, false),
//			new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(Boundary.SINGLE),
//			0.5, 0.5);
//
//		testIterableIntervalSimilarity(out2, out3);
//	}

	/**
	 * @see LocalShanbhag
	 */
	@Test
	public void testLocalShanbhagThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.shanbhag", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

	/**
	 * @see LocalTriangle
	 */
	@Test
	public void testLocalTriangleThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.triangle", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

	/**
	 * @see LocalYen
	 */
	@Test
	public void testLocalYenThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.yen", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

	/**
	 * @see LocalRosin
	 */
	@Test
	public void testLocalRosinThreshold() {
		final Computers.Arity3<RandomAccessibleInterval<ByteType>, Shape, //
				OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>, IterableInterval<BitType>> opToTest =
					Computers.match(ops, "threshold.rosin", //
						new Nil<RandomAccessibleInterval<ByteType>>()
						{}, //
						new Nil<Shape>()
						{}, //
						new Nil<OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>>>()
						{}, //
						new Nil<IterableInterval<BitType>>()
						{}); //

		opToTest.compute(in, new RectangleShape(1, false),
			new OutOfBoundsMirrorFactory<ByteType, RandomAccessibleInterval<ByteType>>(
				Boundary.SINGLE), out);

		assertEquals(false, out.firstElement().get());
	}

//	@Test(expected = IllegalArgumentException.class)
//	public void testContingencyOfNormalImplementation() {
//		ops.run(LocalSauvolaThreshold.class, out, in, new RectangleShape(3, false),
//			new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(Boundary.SINGLE),
//			0.0, 0.0);
//	}

	public ArrayImg<ByteType, ByteArray> generateKnownByteArrayTestImgSmall() {
		final long[] dims = new long[] { 2, 2 };
		final byte[] array = new byte[4];

		array[0] = (byte) 10;
		array[1] = (byte) 20;
		array[2] = (byte) 30;
		array[3] = (byte) 40;

		return ArrayImgs.bytes(array, dims);
	}

	public ArrayImg<ByteType, ByteArray> generateKnownByteArrayTestImgLarge() {
		final long[] dims = new long[] { 3, 3 };
		final byte[] array = new byte[9];

		array[0] = (byte) 40;
		array[1] = (byte) 40;
		array[2] = (byte) 20;

		array[3] = (byte) 40;
		array[4] = (byte) 40;
		array[5] = (byte) 20;

		array[6] = (byte) 20;
		array[7] = (byte) 20;
		array[8] = (byte) 100;

		return ArrayImgs.bytes(array, dims);
	}

	/**
	 * Checks if two {@link IterableInterval} have the same content.
	 *
	 * @param ii1
	 * @param ii2
	 */
	public static <T extends RealType<T>, S extends RealType<S>> void
		testIterableIntervalSimilarity(final IterableInterval<T> ii1,
			final IterableInterval<S> ii2)
	{
		// Test for pixel-wise equality of the results
		final Cursor<T> cursor1 = ii1.localizingCursor();
		final Cursor<S> cursor2 = ii2.cursor();
		while (cursor1.hasNext() && cursor2.hasNext()) {
			final T value1 = cursor1.next();
			final S value2 = cursor2.next();

			assertEquals(value1.getRealDouble(), value2.getRealDouble(), 0.00001d);
		}
	}

}
