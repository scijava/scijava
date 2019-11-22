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
package net.imagej.ops.transform.permuteView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.function.BiFunction;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.function.Functions;
import org.scijava.ops.types.Nil;
import org.scijava.ops.function.Functions;
import org.scijava.util.MersenneTwisterFast;

/**
 * Tests {@link net.imagej.ops.Ops.Transform.PermuteView} ops.
 * <p>
 * This test only checks if the op call works with all parameters and that the
 * result is equal to that of the {@link Views} method call. It is not a
 * correctness test of {@link Views} itself.
 * </p>
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 * @author Gabe Selzer
 */
public class PermuteViewTest extends AbstractTestEnvironment {

	private static final long SEED = 0x12345678;

	public static <T> RandomAccessible<T> deinterval(RandomAccessibleInterval<T> input) {
		return Views.extendBorder(input);
	}

	@Test
	public void defaultPermuteTest() {

		Functions.Arity3<RandomAccessible<DoubleType>, Integer, Integer, MixedTransformView<DoubleType>> permuteFunc = Functions
				.match(ops, "transform.permuteView", new Nil<RandomAccessible<DoubleType>>() {
				}, new Nil<Integer>() {
				}, new Nil<Integer>() {
				}, new Nil<MixedTransformView<DoubleType>>() {
				});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(new int[] { 10, 10 });

		MixedTransformView<DoubleType> il2 = Views.permute((RandomAccessible<DoubleType>) img, 1, 0);
		MixedTransformView<DoubleType> opr = permuteFunc.apply(deinterval(img), 1, 0);

		for (int i = 0; i < il2.getTransformToSource().getMatrix().length; i++) {
			for (int j = 0; j < il2.getTransformToSource().getMatrix()[i].length; j++) {
				assertEquals(il2.getTransformToSource().getMatrix()[i][j], opr.getTransformToSource().getMatrix()[i][j],
						1e-10);
			}
		}
	}

	@Test
	public void defaultPermuteCoordinatesTest() {

		BiFunction<RandomAccessibleInterval<DoubleType>, int[], IntervalView<DoubleType>> permuteFunc = Functions
				.match(ops, "transform.permuteCoordinatesView", new Nil<RandomAccessibleInterval<DoubleType>>() {
				}, new Nil<int[]>() {
				}, new Nil<IntervalView<DoubleType>>() {
				});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(new int[] { 2, 2 });
		Cursor<DoubleType> c = img.cursor();
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		while (c.hasNext()) {
			c.next().set(r.nextDouble());
		}
		Cursor<DoubleType> il2 = Views.permuteCoordinates(img, new int[] { 0, 1 }).cursor();
		RandomAccess<DoubleType> opr = permuteFunc.apply(img, new int[] { 0, 1 }).randomAccess();

		while (il2.hasNext()) {
			il2.next();
			opr.setPosition(il2);
			assertEquals(il2.get().get(), opr.get().get(), 1e-10);
		}

	}

	@Test
	public void permuteCoordinatesOfDimensionTest() {

		Functions.Arity3<RandomAccessibleInterval<DoubleType>, int[], Integer, IntervalView<DoubleType>> permuteFunc = Functions
				.match(ops, "transform.permuteCoordinatesView", new Nil<RandomAccessibleInterval<DoubleType>>() {
				}, new Nil<int[]>() {
				}, new Nil<Integer>() {
				}, new Nil<IntervalView<DoubleType>>() {
				});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(new int[] { 2, 2 });
		Cursor<DoubleType> c = img.cursor();
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		while (c.hasNext()) {
			c.next().set(r.nextDouble());
		}
		Cursor<DoubleType> il2 = Views.permuteCoordinates(img, new int[] { 0, 1 }, 1).cursor();
		RandomAccess<DoubleType> opr = permuteFunc.apply(img, new int[] { 0, 1 }, 1)
				.randomAccess();

		while (il2.hasNext()) {
			il2.next();
			opr.setPosition(il2);
			assertEquals(il2.get().get(), opr.get().get(), 1e-10);
		}

	}

	@Test
	public void defaultPermuteCoordinatesInverseTest() {

		BiFunction<RandomAccessibleInterval<DoubleType>, int[], IntervalView<DoubleType>> permuteFunc = Functions
				.match(ops, "transform.permuteCoordinatesInverseView",
						new Nil<RandomAccessibleInterval<DoubleType>>() {
				}, new Nil<int[]>() {
				}, new Nil<IntervalView<DoubleType>>() {
				});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(new int[] { 2, 2 });
		Cursor<DoubleType> c = img.cursor();
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		while (c.hasNext()) {
			c.next().set(r.nextDouble());
		}
		Cursor<DoubleType> il2 = Views.permuteCoordinatesInverse(img, new int[] { 0, 1 }).cursor();
		RandomAccess<DoubleType> opr = permuteFunc.apply(img, new int[] { 0, 1 })
				.randomAccess();

		while (il2.hasNext()) {
			il2.next();
			opr.setPosition(il2);
			assertEquals(il2.get().get(), opr.get().get(), 1e-10);
		}
	}

	@Test
	public void permuteCoordinatesInverseOfDimensionTest() {

		Functions.Arity3<RandomAccessibleInterval<DoubleType>, int[], Integer, IntervalView<DoubleType>> permuteFunc = Functions
				.match(ops, "transform.permuteCoordinatesInverseView",
						new Nil<RandomAccessibleInterval<DoubleType>>() {
				}, new Nil<int[]>() {
				}, new Nil<Integer>() {
				}, new Nil<IntervalView<DoubleType>>() {
				});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(new int[] { 2, 2 });
		Cursor<DoubleType> c = img.cursor();
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		while (c.hasNext()) {
			c.next().set(r.nextDouble());
		}

		IntervalView<DoubleType> out = Views.permuteCoordinatesInverse(img, new int[] { 0, 1 }, 1);

		Cursor<DoubleType> il2 = out.cursor();
		RandomAccess<DoubleType> opr = permuteFunc.apply(img, new int[] { 0, 1 }, 1)
				.randomAccess();

		while (il2.hasNext()) {
			il2.next();
			opr.setPosition(il2);
			assertEquals(il2.get().get(), opr.get().get(), 1e-10);
		}
	}

	@Test
	public void testIntervalPermute() {

		Functions.Arity3<RandomAccessibleInterval<DoubleType>, Integer, Integer, IntervalView<DoubleType>> permuteFunc = Functions
				.match(ops, "transform.permuteView",
						new Nil<RandomAccessibleInterval<DoubleType>>() {
				}, new Nil<Integer>() {
				}, new Nil<Integer>() {
				}, new Nil<IntervalView<DoubleType>>() {
				});

		Img<DoubleType> img = new ArrayImgFactory<>(new DoubleType()).create(new int[] { 10, 10 });

		IntervalView<DoubleType> expected = Views.permute((RandomAccessibleInterval<DoubleType>) img, 1, 0);
		IntervalView<DoubleType> actual = permuteFunc.apply(img, 1, 0);

		for (int i = 0; i < ((MixedTransformView<DoubleType>) expected.getSource()).getTransformToSource()
				.getMatrix().length; i++) {
			for (int j = 0; j < ((MixedTransformView<DoubleType>) expected.getSource()).getTransformToSource()
					.getMatrix()[i].length; j++) {
				assertEquals(
						((MixedTransformView<DoubleType>) expected.getSource()).getTransformToSource()
						.getMatrix()[i][j],
						((MixedTransformView<DoubleType>) actual.getSource()).getTransformToSource().getMatrix()[i][j],
						1e-10);
			}
		}
	}

	@Test
	public void testIntervalPermuteCoordinates() {

		BiFunction<RandomAccessibleInterval<DoubleType>, int[], IntervalView<DoubleType>> permuteFunc = Functions
				.match(ops, "transform.permuteCoordinatesView", new Nil<RandomAccessibleInterval<DoubleType>>() {
				}, new Nil<int[]>() {
				}, new Nil<IntervalView<DoubleType>>() {
				});

		Img<DoubleType> img = ArrayImgs.doubles(2, 2);
		Cursor<DoubleType> c = img.cursor();
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		while (c.hasNext()) {
			c.next().set(r.nextDouble());
		}
		IntervalView<DoubleType> expected = Views.permuteCoordinates(img, new int[] { 0, 1 });
		Cursor<DoubleType> e = expected.cursor();
		RandomAccessibleInterval<DoubleType> actual = permuteFunc.apply(img, new int[] { 0, 1 });
		RandomAccess<DoubleType> actualRA = actual.randomAccess();

		while (e.hasNext()) {
			e.next();
			actualRA.setPosition(e);
			assertEquals(e.get().get(), actualRA.get().get(), 1e-10);
		}

		assertTrue(Intervals.equals(expected, actual));

	}

	@Test
	public void testIntervalPermuteDimensionCoordinates() {

		Functions.Arity3<RandomAccessibleInterval<DoubleType>, int[], Integer, IntervalView<DoubleType>> permuteFunc = Functions
				.match(ops, "transform.permuteCoordinatesInverseView",
						new Nil<RandomAccessibleInterval<DoubleType>>() {
				}, new Nil<int[]>() {
				}, new Nil<Integer>() {
				}, new Nil<IntervalView<DoubleType>>() {
				});

		Img<DoubleType> img = ArrayImgs.doubles(2, 2);
		Cursor<DoubleType> c = img.cursor();
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		while (c.hasNext()) {
			c.next().set(r.nextDouble());
		}
		IntervalView<DoubleType> expected = Views.permuteCoordinates(img, new int[] { 0, 1 }, 1);
		Cursor<DoubleType> e = expected.cursor();
		RandomAccessibleInterval<DoubleType> actual = permuteFunc.apply(img, new int[] { 0, 1 },
				1);
		RandomAccess<DoubleType> actualRA = actual.randomAccess();

		while (e.hasNext()) {
			e.next();
			actualRA.setPosition(e);
			assertEquals(e.get().get(), actualRA.get().get(), 1e-10);
		}

		assertTrue(Intervals.equals(expected, actual));

	}

	@Test
	public void testIntervalPermuteInverseCoordinates() {

		BiFunction<RandomAccessibleInterval<DoubleType>, int[], IntervalView<DoubleType>> permuteFunc = Functions
				.match(ops, "transform.permuteCoordinatesInverseView",
						new Nil<RandomAccessibleInterval<DoubleType>>() {
				}, new Nil<int[]>() {
				}, new Nil<IntervalView<DoubleType>>() {
				});

		Img<DoubleType> img = ArrayImgs.doubles(2, 2);
		Cursor<DoubleType> c = img.cursor();
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		while (c.hasNext()) {
			c.next().set(r.nextDouble());
		}
		IntervalView<DoubleType> expected = Views.permuteCoordinatesInverse(img, new int[] { 0, 1 });
		Cursor<DoubleType> e = expected.cursor();
		RandomAccessibleInterval<DoubleType> actual = permuteFunc.apply(img,
				new int[] { 0, 1 });
		RandomAccess<DoubleType> actualRA = actual.randomAccess();

		while (e.hasNext()) {
			e.next();
			actualRA.setPosition(e);
			assertEquals(e.get().get(), actualRA.get().get(), 1e-10);
		}

		assertTrue(Intervals.equals(expected, actual));

	}

	@Test
	public void testIntervalPermuteInverseDimensionCoordinates() {

		Functions.Arity3<RandomAccessibleInterval<DoubleType>, int[], Integer, IntervalView<DoubleType>> permuteFunc = Functions
				.match(ops, "transform.permuteCoordinatesInverseView",
						new Nil<RandomAccessibleInterval<DoubleType>>() {
				}, new Nil<int[]>() {
				}, new Nil<Integer>() {
				}, new Nil<IntervalView<DoubleType>>() {
				});

		Img<DoubleType> img = ArrayImgs.doubles(2, 2);
		Cursor<DoubleType> c = img.cursor();
		MersenneTwisterFast r = new MersenneTwisterFast(SEED);
		while (c.hasNext()) {
			c.next().set(r.nextDouble());
		}
		IntervalView<DoubleType> expected = Views.permuteCoordinatesInverse(img, new int[] { 0, 1 }, 1);
		Cursor<DoubleType> e = expected.cursor();
		RandomAccessibleInterval<DoubleType> actual = permuteFunc.apply(img,
				new int[] { 0, 1 }, 1);
		RandomAccess<DoubleType> actualRA = actual.randomAccess();

		while (e.hasNext()) {
			e.next();
			actualRA.setPosition(e);
			assertEquals(e.get().get(), actualRA.get().get(), 1e-10);
		}

		assertTrue(Intervals.equals(expected, actual));

	}
}
