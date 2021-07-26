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

package net.imagej.ops2.create;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.function.Functions;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;
import org.scijava.util.MersenneTwisterFast;

/**
 * Tests several ways to create an image
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */

public class CreateImgTest<T extends RealType<T>> extends AbstractOpTest {

	private static final int TEST_SIZE = 100;
	private static final long SEED = 0x12345678;

	@Test
	public void testImageMinimum() {

		final MersenneTwisterFast randomGenerator = new MersenneTwisterFast(SEED);

		for (int i = 0; i < TEST_SIZE; i++) {

			// between 2 and 5 dimensions
			final long[] max = new long[randomGenerator.nextInt(4) + 2];
			final long[] min = new long[max.length];

			// between 2 and 10 pixels per dimensions
			for (int j = 0; j < max.length; j++) {
				max[j] = randomGenerator.nextInt(9) + 2;
				min[j] = Math.max(0, max[j] - randomGenerator.nextInt(4));
			}

			// create img
			final Function<Interval, Img<?>> createFunc = OpBuilder.matchFunction(ops.env(), "create.img", new Nil<Interval>() {
			}, new Nil<Img<?>>() {
			});
			final Img<?> img = createFunc.apply(new FinalInterval(min, max));

			assertArrayEquals(min, Intervals.minAsLongArray(img), "Image Minimum:");
			assertArrayEquals(max, Intervals.maxAsLongArray(img), "Image Maximum:");
		}
	}

	@Test
	public void testImageDimensions() {

		final MersenneTwisterFast randomGenerator = new MersenneTwisterFast(SEED);

		for (int i = 0; i < TEST_SIZE; i++) {

			// between 2 and 5 dimensions
			final long[] dim = new long[randomGenerator.nextInt(4) + 2];

			// between 2 and 10 pixels per dimensions
			for (int j = 0; j < dim.length; j++) {
				dim[j] = randomGenerator.nextInt(9) + 2;
			}

			// create img
			BiFunction<Dimensions, DoubleType, Img<DoubleType>> createFunc = OpBuilder.matchFunction(ops.env(), "create.img",
					new Nil<Dimensions>() {
					}, new Nil<DoubleType>() {
					}, new Nil<Img<DoubleType>>() {
					});
			@SuppressWarnings("unchecked")
			final Img<DoubleType> img = createFunc.apply(new FinalDimensions(dim), new DoubleType());

			assertArrayEquals(dim, Intervals.dimensionsAsLongArray(img), "Image Dimensions:");
		}
	}

	@Test
	public void testImgFromImg() {
		// create img
		BiFunction<Dimensions, ByteType, Img<ByteType>> createFuncDimsType = OpBuilder.matchFunction(ops.env(), "create.img",
				new Nil<Dimensions>() {
				}, new Nil<ByteType>() {
				}, new Nil<Img<ByteType>>() {
				});
		final Img<ByteType> img = createFuncDimsType.apply(new FinalDimensions(1), new ByteType());
		Function<Img<ByteType>, Img<ByteType>> createFuncImg = OpBuilder.matchFunction(ops.env(), "create.img",
				new Nil<Img<ByteType>>() {
				}, new Nil<Img<ByteType>>() {
				});
		@SuppressWarnings("unchecked")
		final Img<ByteType> newImg = createFuncImg.apply(img);

		// should both be ByteType. New Img shouldn't be DoubleType (default)
		assertEquals(img.firstElement().getClass(), newImg.firstElement().getClass());
	}

	@Test
	public void testImageFactory() {
		final Dimensions dim = new FinalDimensions(10, 10, 10);

		Functions.Arity3<Dimensions, DoubleType, ImgFactory<DoubleType>, Img<DoubleType>> createFunc =
			OpBuilder.matchFunction(ops.env(), "create.img", new Nil<Dimensions>()
			{}, new Nil<DoubleType>() {}, new Nil<ImgFactory<DoubleType>>() {},
				new Nil<Img<DoubleType>>()
				{});

		@SuppressWarnings("unchecked")
		final Img<DoubleType> arrayImg = createFunc.apply(dim, new DoubleType(), new ArrayImgFactory<>(new DoubleType()));
		final Class<?> arrayFactoryClass = arrayImg.factory().getClass();
		assertEquals(ArrayImgFactory.class, arrayFactoryClass, "Image Factory: ");

		@SuppressWarnings("unchecked")
		final Img<DoubleType> cellImg = createFunc.apply(dim, new DoubleType(),
				new CellImgFactory<>(new DoubleType()));
		final Class<?> cellFactoryClass = cellImg.factory().getClass();
		assertEquals(CellImgFactory.class, cellFactoryClass, "Image Factory: ");
	}

	@Test
	public void testImageType() {
		final Dimensions dim = new FinalDimensions(10, 10, 10);
		
		BiFunction<Dimensions, T, Img<T>> createFunc = OpBuilder.matchFunction(ops.env(), "create.img", new Nil<Dimensions>() {}, new Nil<T>() {}, new Nil<Img<T>>() {});

		assertEquals(BitType.class, ((Img<?>) createFunc.apply(dim,
			(T) new BitType())).firstElement().getClass(), "Image Type: ");

		assertEquals(ByteType.class,
				((Img<?>) createFunc.apply(dim, (T) new ByteType())).firstElement().getClass(), "Image Type: ");

		assertEquals(UnsignedByteType.class,
				((Img<?>) createFunc.apply(dim, (T) new UnsignedByteType())).firstElement().getClass(), "Image Type: ");

		assertEquals(IntType.class,
				((Img<?>) createFunc.apply(dim, (T) new IntType())).firstElement().getClass(), "Image Type: ");

		assertEquals(FloatType.class,
				((Img<?>) createFunc.apply(dim, (T) new FloatType())).firstElement().getClass(), "Image Type: ");

		assertEquals(DoubleType.class,
				((Img<?>) createFunc.apply(dim, (T)new DoubleType())).firstElement().getClass(), "Image Type: ");
	}

	@Test
	public void testCreateFromImgSameType() {
		final Img<ByteType> input = PlanarImgs.bytes(10, 10, 10);
		BiFunction<Dimensions, ByteType, Img<ByteType>> createFunc = OpBuilder.matchFunction(ops.env(), "create.img",
				new Nil<Dimensions>() {
				}, new Nil<ByteType>() {
				}, new Nil<Img<ByteType>>() {
				});
		final Img<?> res = createFunc.apply(input, input.firstElement().createVariable());

		assertEquals(ByteType.class, res.firstElement().getClass(), "Image Type: ");
		assertArrayEquals(Intervals.dimensionsAsLongArray(input),
				Intervals.dimensionsAsLongArray(res), "Image Dimensions: ");
		assertEquals(input.factory().getClass(), res.factory().getClass(), "Image Factory: ");
	}

	@Test
	public void testCreateFromImgDifferentType() {
		final Img<ByteType> input = PlanarImgs.bytes(10, 10, 10);
		BiFunction<Dimensions, ShortType, Img<ShortType>> createFunc = OpBuilder.matchFunction(ops.env(), "create.img",
				new Nil<Dimensions>() {
				}, new Nil<ShortType>() {
				}, new Nil<Img<ShortType>>() {
				});
		final Img<?> res = createFunc.apply(input, new ShortType());

		assertEquals(ShortType.class, res.firstElement().getClass(), "Image Type: ");
		assertArrayEquals(Intervals.dimensionsAsLongArray(input),
				Intervals.dimensionsAsLongArray(res), "Image Dimensions: ");
		assertEquals(input.factory().getClass(), res.factory().getClass(), "Image Factory: ");
	}

	@Test
	public void testCreateFromRaiDifferentType() {
		final IntervalView<ByteType> input = Views.interval(PlanarImgs.bytes(10, 10, 10),
				new FinalInterval(new long[] { 10, 10, 1 }));

		BiFunction<Dimensions, ShortType, Img<ShortType>> createFunc = OpBuilder.matchFunction(ops.env(), "create.img",
				new Nil<Dimensions>() {
				}, new Nil<ShortType>() {
				}, new Nil<Img<ShortType>>() {
				});

		final Img<?> res = createFunc.apply(input, new ShortType());

		assertEquals(ShortType.class, res.firstElement().getClass(), "Image Type: ");

		assertArrayEquals(Intervals.dimensionsAsLongArray(input),
				Intervals.dimensionsAsLongArray(res), "Image Dimensions: ");

		assertEquals(ArrayImgFactory.class, res.factory().getClass(), "Image Factory: ");
	}

	/**
	 * A simple test to ensure {@link Integer} arrays are not eaten by the varargs
	 * when passed as the only argument.
	 */
	@Test
	public void testCreateFromIntegerArray() {
		final Integer[] dims = new Integer[] { 25, 25, 10 };

		Function<Integer[], Img<?>> createFunc = OpBuilder.matchFunction(ops.env(), "create.img", new Nil<Integer[]>() {
		}, new Nil<Img<?>>() {
		});
		final Img<?> res = createFunc.apply(dims);

		for (int i = 0; i < dims.length; i++) {
			assertEquals(dims[i].longValue(), res.dimension(i), "Image Dimension " + i + ": ");
		}
	}

	/**
	 * A simple test to ensure {@link Long} arrays are not eaten by the varargs when
	 * passed as the only argument.
	 */
	@Test
	public void testCreateFromLongArray() {
		final Long[] dims = new Long[] { 25l, 25l, 10l };

		Function<Long[], Img<?>> createFunc = OpBuilder.matchFunction(ops.env(), "create.img", new Nil<Long[]>() {
		}, new Nil<Img<?>>() {
		});
		final Img<?> res = createFunc.apply(dims);

		for (int i = 0; i < dims.length; i++) {
			assertEquals(dims[i].longValue(), res.dimension(i), "Image Dimension " + i + ": ");
		}
	}

}
