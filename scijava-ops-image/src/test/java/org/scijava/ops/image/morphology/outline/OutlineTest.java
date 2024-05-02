/*-
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

package org.scijava.ops.image.morphology.outline;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.morphology.Outline;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Tests for the {@link Outline} op
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class OutlineTest extends AbstractOpTest {

	/** Test basic properties of the op's output */
	@Test
	public void testOutput() throws Exception {
		// SETUP
		final long[] inputDims = { 3, 3, 3 };
		final Img<BitType> img = ArrayImgs.bits(inputDims);

		// EXECUTE
		final Img<BitType> result = ops.op("morphology.outline").input(img,
			Boolean.TRUE).outType(new Nil<Img<BitType>>()
		{}).apply();

		// VERIFY
		assertNotNull(result);
		final long[] outputDims = new long[result.numDimensions()];
		result.dimensions(outputDims);
		assertArrayEquals(inputDims, outputDims);
	}

	/** Test the op with an interval that's full of background elements */
	@Test
	public void testAllBackground() throws Exception {
		// SETUP
		final Img<BitType> img = ArrayImgs.bits(3, 3, 3);

		// EXECUTE
		final Img<BitType> result = ops.op("morphology.outline").input(img,
			Boolean.TRUE).outType(new Nil<Img<BitType>>()
		{}).apply();

		// VERIFY
		assertEquals(0, countForeground(result),
			"Output should contain no foreground");
	}

	/** Test the op with an interval that's full of foreground elements */
	@Test
	public void testAllForeground() throws Exception {
		// SETUP
		final Img<BitType> img = ArrayImgs.bits(3, 3, 3);
		img.forEach(BitType::setOne);

		// EXECUTE
		final Img<BitType> result = ops.op("morphology.outline").input(img,
			Boolean.TRUE).outType(new Nil<Img<BitType>>()
		{}).apply();

		// VERIFY
		assertEquals(0, countForeground(result),
			"Output should contain no foreground");
	}

	/** Test the op with a 2x2 square. The square is in the middle of a 4x4 img */
	@Test
	public void testSquare() throws Exception {
		// SETUP
		final Img<BitType> img = ArrayImgs.bits(4, 4);
		final IntervalView<BitType> square = Views.offsetInterval(img, new long[] {
			1, 1 }, new long[] { 2, 2 });
		square.cursor().forEachRemaining(BitType::setOne);

		// EXECUTE
		final Img<BitType> result = ops.op("morphology.outline").input(img,
			Boolean.TRUE).outType(new Nil<Img<BitType>>()
		{}).apply();

		// VERIFY
		assertEquals(4, countForeground(result),
			"Wrong number of foreground elements in interval");
		final IntervalView<BitType> resultSquare = Views.offsetInterval(result,
			new long[] { 1, 1 }, new long[] { 2, 2 });
		assertTrue(allForeground(resultSquare),
			"Wrong number of foreground elements in object");
	}

	/**
	 * Test the op with a 3x3 square with a hole in the middle. The square is in
	 * the middle of a 5x5 img
	 */
	@Test
	public void testOutlineSquare() throws Exception {
		// SETUP
		final Img<BitType> img = ArrayImgs.bits(5, 5);
		final IntervalView<BitType> square = Views.offsetInterval(img, new long[] {
			1, 1 }, new long[] { 3, 3 });
		square.cursor().forEachRemaining(BitType::setOne);
		final RandomAccess<BitType> access = square.randomAccess();
		access.setPosition(new long[] { 1, 1 });
		access.get().setZero();

		// EXECUTION
		final Img<BitType> result = ops.op("morphology.outline").input(img,
			Boolean.TRUE).outType(new Nil<Img<BitType>>()
		{}).apply();

		// VERIFY
		assertEquals(8, countForeground(result),
			"Wrong number of foreground elements in interval");
		final IntervalView<BitType> resultSquare = Views.offsetInterval(result,
			new long[] { 1, 1 }, new long[] { 3, 3 });
		assertEquals(8, countForeground(resultSquare),
			"Wrong number of foreground elements in object");
		assertPositionBackground(result, new long[] { 2, 2 });
	}

	/**
	 * Test the op with a 3x3 square starting from (0,1) in a 5x5 img
	 *
	 * @see Outline#compute(RandomAccessibleInterval, Boolean,
	 *      RandomAccessibleInterval)
	 * @see #testEdgeSquare()
	 */
	@Test
	public void testEdgeSquare() throws Exception {
		// SETUP
		final Img<BitType> img = ArrayImgs.bits(5, 5);
		final IntervalView<BitType> square = Views.offsetInterval(img, new long[] {
			0, 1 }, new long[] { 3, 3 });
		square.cursor().forEachRemaining(BitType::setOne);

		// EXECUTION
		final Img<BitType> result = ops.op("morphology.outline").input(img,
			Boolean.TRUE).outType(new Nil<Img<BitType>>()
		{}).apply();

		// VERIFY
		assertEquals(7, countForeground(result),
			"Wrong number of foreground elements in interval");
		final IntervalView<BitType> resultSquare = Views.offsetInterval(result,
			new long[] { 0, 1 }, new long[] { 3, 3 });
		assertEquals(7, countForeground(resultSquare),
			"Wrong number of foreground elements in object");
		assertPositionBackground(result, new long[] { 0, 2 });
		assertPositionBackground(result, new long[] { 1, 2 });
	}

	/**
	 * Test the op with a 3x3 square starting from (0,1) in a 5x5 img without
	 * excluding edges
	 *
	 * @see Outline#compute(RandomAccessibleInterval, Boolean,
	 *      RandomAccessibleInterval)
	 * @see #testEdgeSquare()
	 */
	@Test
	public void testEdgeSquareExcludeEdgesFalse() throws Exception {
		// SETUP
		final Img<BitType> img = ArrayImgs.bits(5, 5);
		final IntervalView<BitType> square = Views.offsetInterval(img, new long[] {
			0, 1 }, new long[] { 3, 3 });
		square.cursor().forEachRemaining(BitType::setOne);

		final Img<BitType> result = ops.op("morphology.outline").input(img,
			Boolean.FALSE).outType(new Nil<Img<BitType>>()
		{}).apply();

		assertEquals(8, countForeground(result),
			"Wrong number of foreground elements in interval");
		final IntervalView<BitType> resultSquare = Views.offsetInterval(result,
			new long[] { 0, 1 }, new long[] { 3, 3 });
		assertEquals(8, countForeground(resultSquare),
			"Wrong number of foreground elements in object");
		assertPositionBackground(result, new long[] { 1, 2 });
	}

	/**
	 * Test the op with a 3x3x3x3 hypercube. The cube is in the middle of a
	 * 5x5x5x5 img
	 */
	@Test
	public void testHyperCube() throws Exception {
		// SETUP
		final Img<BitType> img = ArrayImgs.bits(5, 5, 5, 5);
		final IntervalView<BitType> hyperCube = Views.offsetInterval(img,
			new long[] { 1, 1, 1, 1 }, new long[] { 3, 3, 3, 3 });
		hyperCube.cursor().forEachRemaining(BitType::setOne);

		// EXECUTE
		final Img<BitType> result = ops.op("morphology.outline").input(img,
			Boolean.TRUE).outType(new Nil<Img<BitType>>()
		{}).apply();

		// VERIFY
		assertEquals(80, countForeground(result),
			"Wrong number of foreground elements in interval");
		final IntervalView<BitType> resultHyperCube = Views.offsetInterval(result,
			new long[] { 1, 1, 1, 1 }, new long[] { 3, 3, 3, 3 });
		assertEquals(80, countForeground(resultHyperCube),
			"Wrong number of foreground elements in object");
		assertPositionBackground(result, new long[] { 2, 2, 2, 2 });
	}

	// region -- Helper methods --
	private boolean allForeground(final IterableInterval<BitType> interval) {
		for (final BitType element : interval) {
			if (!element.get()) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	private int countForeground(final IterableInterval<BitType> interval) {
		int count = 0;
		for (final BitType element : interval) {
			count = count + element.getInteger();
		}
		return count;
	}

	private void assertPositionBackground(
		final RandomAccessibleInterval<BitType> interval, final long[] position)
	{
		final RandomAccess<BitType> access = interval.randomAccess();
		access.setPosition(position);
		assertFalse(access.get().get(), "Element should be background");
	}
	// endregion
}
