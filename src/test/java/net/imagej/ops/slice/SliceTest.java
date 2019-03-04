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

package net.imagej.ops.slice;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.junit.Before;
import org.junit.Test;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.Computer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Testing functionality of SlicingIterableIntervals
 * 
 * @author Christian Dietz (University of Konstanz)
 * @author Brian Northan
 */
@Plugin(type = OpCollection.class)
public class SliceTest <I extends RealType<I>, O extends RealType<O>> extends AbstractOpTest {

	private Img<ByteType> in;

	private ArrayImg<ByteType, ByteArray> out;
	
	@Override
	@Before
	public void setUp() {
		super.setUp();
		
		in = ArrayImgs.bytes(20, 20, 21);
		out = ArrayImgs.bytes(20, 20, 21);

		// fill array img with values (plane position = value in px);

		for (final Cursor<ByteType> cur = in.cursor(); cur.hasNext();) {
			cur.fwd();
			cur.get().set((byte) cur.getIntPosition(2));
		}
	}

	@Test
	public void testXYCropping() {
		
		// fill array img with values (plane position = value in px);

		for (final Cursor<ByteType> cur = in.cursor(); cur.hasNext();) {
			cur.fwd();
			cur.get().set((byte) cur.getIntPosition(2));
		}

		// selected interval XY
		final int[] xyAxis = new int[] { 0, 1 };
		
//		// get DummyOp instance TODO why does this not work when the lambda Op test does?
//		Computer<RandomAccessibleInterval<ByteType>, RandomAccessibleInterval<ByteType>> dummyOp = Computers.unary(ops(),
//				"test.dummyOp", new Nil<RandomAccessibleInterval<ByteType>>() {
//				}, new Nil<RandomAccessibleInterval<ByteType>>() {
//				});

		ops().run("slice", in, test, xyAxis, true, out);

		for (final Cursor<ByteType> cur = out.cursor(); cur.hasNext();) {
			cur.fwd();
			assertEquals(cur.getIntPosition(2), cur.get().getRealDouble(), 0);
		}
	}

	@Test
	public void testXYZCropping() {
		// the slices can end up being processed in parallel. So try with a few
		// different timepoint values
		// in order to test the chunker with various chunk sizes
		testXYZCropping(1);
		testXYZCropping(5);
		testXYZCropping(11);
		testXYZCropping(17);
		testXYZCropping(27);
	}

	private void testXYZCropping(int t) {

		Img<ByteType> inSequence = ArrayImgs.bytes(20, 20, 21, t);
		ArrayImg<ByteType, ByteArray> outSequence = ArrayImgs.bytes(20, 20, 21, t);

		// fill array img with values (plane position = value in px);
		for (final Cursor<ByteType> cur = inSequence.cursor(); cur.hasNext();) {
			cur.fwd();
			cur.get().set((byte) cur.getIntPosition(2));
		}

		// selected interval XYZ
		final int[] xyAxis = new int[] { 0, 1, 2 };
		
//		// get DummyOp instance TODO why does this not work when the lambda Op test does?
//		Computer<RandomAccessibleInterval<ByteType>, RandomAccessibleInterval<ByteType>> dummyOp = Computers.unary(ops(),
//				"test.dummyOp", new Nil<RandomAccessibleInterval<ByteType>>() {
//				}, new Nil<RandomAccessibleInterval<ByteType>>() {
//				});

		ops().run("slice", inSequence, test, xyAxis, true, outSequence);

		for (final Cursor<ByteType> cur = outSequence.cursor(); cur.hasNext();) {
			cur.fwd();
			assertEquals(cur.getIntPosition(2), cur.get().getRealDouble(), 0);
		}
	}

	@Test
	public void testNonZeroMinimumInterval() {

		Img<ByteType> img3D = ArrayImgs.bytes(50, 50, 3);
		IntervalView<ByteType> interval2D = Views.interval(img3D,
				new FinalInterval(new long[] { 25, 25, 2 }, new long[] { 35, 35, 2 }));
		final int[] xyAxis = new int[] { 0, 1 };

		// iterate through every slice, should return a single
		// RandomAccessibleInterval<?> from 25, 25, 2 to 35, 35, 2

		final SlicesII<ByteType> hyperSlices = new SlicesII<>(interval2D, xyAxis, true);
		final Cursor<RandomAccessibleInterval<ByteType>> c = hyperSlices.cursor();
		int i = 0;
		while (c.hasNext()) {
			c.next();
			i++;
		}

		assertEquals(1, i);
	}

	@Test
	public void LoopThroughHyperSlicesTest() {
		final int xSize = 40;
		final int ySize = 50;
		final int numChannels = 3;
		final int numSlices = 25;
		final int numTimePoints = 5;

		final Img<UnsignedByteType> testImage = generateUnsignedByteArrayTestImg(true, xSize, ySize, numChannels,
				numSlices, numTimePoints);

		final int[] axisIndices = new int[3];

		// set up the axis so the resulting hyperslices are x,y,z and
		// we loop through channels and time
		axisIndices[0] = 0;
		axisIndices[1] = 1;
		axisIndices[2] = 3;

		final SlicesII<UnsignedByteType> hyperSlices = new SlicesII<>(testImage, axisIndices, true);

		final Cursor<RandomAccessibleInterval<UnsignedByteType>> c = hyperSlices.cursor();

		int numHyperSlices = 0;
		while (c.hasNext()) {
			c.fwd();
			numHyperSlices++;
			final RandomAccessibleInterval<UnsignedByteType> hyperSlice = c.get();
			assertEquals(3, hyperSlice.numDimensions());
			assertEquals(hyperSlice.dimension(0), xSize);
			assertEquals(hyperSlice.dimension(1), ySize);
			assertEquals(hyperSlice.dimension(2), numSlices);
		}

		assertEquals(numChannels * numTimePoints, numHyperSlices);

	}
	
	public Computer<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> test = (input, output) -> {
		final Iterator<I> itA = Views.iterable(input).iterator();
		final Iterator<O> itB = Views.iterable(output).iterator();

		while (itA.hasNext() && itB.hasNext()) {
			itB.next().setReal(itA.next().getRealDouble());
		}
	};
	
}

@Plugin(type = Op.class, name = "test.dummyOp")
@Parameter(key = "input")
@Parameter(key = "output", type = ItemIO.BOTH)
class DummyOp<I extends RealType<I>, O extends RealType<O>> implements Computer<RandomAccessibleInterval<ByteType>, RandomAccessibleInterval<ByteType>> {

	@Override
	public void compute(final RandomAccessibleInterval<ByteType> input, final RandomAccessibleInterval<ByteType> output) {
		final Iterator<ByteType> itA = Views.iterable(input).iterator();
		final Iterator<ByteType> itB = Views.iterable(output).iterator();

		while (itA.hasNext() && itB.hasNext()) {
			itB.next().set(itA.next().get());
		}
	}

}
