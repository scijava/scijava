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

package org.scijava.ops.image.topology;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Tests for the {@link BoxCount} op
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class BoxCountTest extends AbstractOpTest {

	private static final Long MAX_SIZE = 16L;
	private static final Long MIN_SIZE = 2L;
	private static final double SCALING = 2.0;
	private static final long ITERATIONS = 4;
	private static final long DIMENSIONS = 2;
	private static final long[] TEST_DIMS = LongStream.generate(() -> MAX_SIZE)
		.limit(DIMENSIONS).toArray();
	private static final double[] EXPECTED_SIZES = DoubleStream.iterate(MAX_SIZE,
		d -> d / SCALING).map(d -> -Math.log(d)).limit(ITERATIONS).toArray();

	@Test
	public void testAllBackground() throws Exception {
		// SETUP
		final double expectedCount = Math.log(0.0);
		final Img<BitType> img = ArrayImgs.bits(TEST_DIMS);

		// EXECUTE
		final List<ValuePair<DoubleType, DoubleType>> points = ops.op(
			"topology.boxCount").input(img, MAX_SIZE, MIN_SIZE, SCALING, 0l).outType(
				new Nil<List<ValuePair<DoubleType, DoubleType>>>()
				{}).apply();

		// VERIFY
		assertNotNull(points);
		Assertions.assertEquals(ITERATIONS, points.size());
		for (int i = 0; i < ITERATIONS; i++) {
			Assertions.assertEquals(EXPECTED_SIZES[i], points.get(i).a.get(), 1e-12);
			Assertions.assertEquals(expectedCount, points.get(i).b.get(), 1e-12);
		}
	}

	@Test
	public void testAllForeground() {
		// SETUP
		final double scalingPow = DoubleStream.generate(() -> SCALING).limit(
			DIMENSIONS).reduce((i, j) -> i * j).orElse(0);
		final double[] expectedCounts = DoubleStream.iterate(1.0, i -> i *
			scalingPow).map(Math::log).limit(ITERATIONS).toArray();
		final Img<BitType> img = ArrayImgs.bits(TEST_DIMS);
		img.forEach(BitType::setOne);

		// EXECUTE
		final List<ValuePair<DoubleType, DoubleType>> points = ops.op(
			"topology.boxCount").input(img, MAX_SIZE, MIN_SIZE, SCALING, 0l).outType(
				new Nil<List<ValuePair<DoubleType, DoubleType>>>()
				{}).apply();

		// VERIFY
		for (int i = 0; i < ITERATIONS; i++) {
			Assertions.assertEquals(EXPECTED_SIZES[i], points.get(i).a.get(), 1e-12);
			Assertions.assertEquals(expectedCounts[i], points.get(i).b.get(), 1e-12);
		}
	}

	@Test
	public void testHyperCube() {
		// SETUP
		final double[] expectedSizes = DoubleStream.of(4, 2, 1).map(i -> -Math.log(
			i)).toArray();
		final double[] expectedCounts = DoubleStream.of(1, 16, 16).map(Math::log)
			.toArray();
		final Img<BitType> img = ArrayImgs.bits(4, 4, 4, 4);
		final IntervalView<BitType> hyperView = Views.offsetInterval(img,
			new long[] { 1, 1, 1, 1 }, new long[] { 2, 2, 2, 2 });
		hyperView.forEach(BitType::setOne);

		// EXECUTE
		final List<ValuePair<DoubleType, DoubleType>> points = ops.op(
			"topology.boxCount").input(img, 4L, 1L, 2.0, 0L).outType(
				new Nil<List<ValuePair<DoubleType, DoubleType>>>()
				{}).apply();

		// VERIFY
		for (int i = 0; i < expectedSizes.length; i++) {
			Assertions.assertEquals(expectedSizes[i], points.get(i).a.get(), 1e-12);
			Assertions.assertEquals(expectedCounts[i], points.get(i).b.get(), 1e-12);
		}
	}

	/**
	 * Test box counting with a hyper cube and one grid translation (should find a
	 * better fit than in @see {@link #testHyperCube()})
	 */
	@Test
	public void testHyperCubeTranslations() {
		final double[] expectedSizes = DoubleStream.of(4, 2, 1).map(i -> -Math.log(
			i)).toArray();
		final double[] expectedCounts = DoubleStream.of(1, 1, 16).map(Math::log)
			.toArray();
		final Img<BitType> img = ArrayImgs.bits(4, 4, 4, 4);
		final IntervalView<BitType> hyperView = Views.offsetInterval(img,
			new long[] { 1, 1, 1, 1 }, new long[] { 2, 2, 2, 2 });
		hyperView.forEach(BitType::setOne);

		// EXECUTE
		final List<ValuePair<DoubleType, DoubleType>> points = ops.op(
			"topology.boxCount").input(img, 4L, 1L, 2.0, 1L).outType(
				new Nil<List<ValuePair<DoubleType, DoubleType>>>()
				{}).apply();

		// VERIFY
		for (int i = 0; i < expectedSizes.length; i++) {
			Assertions.assertEquals(expectedSizes[i], points.get(i).a.get(), 1e-12);
			Assertions.assertEquals(expectedCounts[i], points.get(i).b.get(), 1e-12);
		}
	}

	@Test
	public void testOneVoxel() {
		// SETUP
		final PrimitiveIterator.OfDouble sizes = DoubleStream.of(9, 3, 1).map(
			i -> -Math.log(i)).iterator();
		final PrimitiveIterator.OfDouble counts = DoubleStream.of(1, 1, 1).map(
			Math::log).iterator();
		final Img<BitType> img = ArrayImgs.bits(9, 9, 9);
		final RandomAccess<BitType> access = img.randomAccess();
		access.setPosition(new long[] { 4, 4, 4 });
		access.get().setOne();

		// EXECUTE
		final List<ValuePair<DoubleType, DoubleType>> points = ops.op(
			"topology.boxCount").input(img, 9L, 3L, 3.0, 0l).outType(
				new Nil<List<ValuePair<DoubleType, DoubleType>>>()
				{}).apply();

		// VERIFY
		points.forEach(p -> {
			Assertions.assertEquals(p.a.get(), sizes.next(), 1e-12);
			Assertions.assertEquals(p.b.get(), counts.next(), 1e-12);
		});
	}

	@Test
	public void testThrowsIAEIfScalingEqualsOne() {
		final Img<BitType> img = ArrayImgs.bits(9, 9, 9);

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ops.op("topology.boxCount").input(img, 8L, 2L, 1.0, 0L).outType(
				new Nil<List<ValuePair<DoubleType, DoubleType>>>()
				{}).apply();
		});

	}

	@Test
	public void testLimitTranslationsThrowsIAEIfSizeNonPositive() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			BoxCount.limitTranslations(0L, 5L);
		});
	}

	@Test
	public void testLimitTranslationsNonPositiveTranslations() {
		Assertions.assertEquals(1L, BoxCount.limitTranslations(10L, 0L));
		Assertions.assertEquals(1L, BoxCount.limitTranslations(100L, -1L));
	}

	@Test
	public void testLimitTranslations() {
		Assertions.assertEquals(9L, BoxCount.limitTranslations(10L, 9L));
		Assertions.assertEquals(10L, BoxCount.limitTranslations(10L, 11L));
	}
}
