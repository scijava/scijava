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

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;

import org.junit.jupiter.api.Test;
import net.imagej.ops2.AbstractOpTest;
import org.scijava.ops.function.Functions;
import org.scijava.types.Nil;
import org.scijava.ops.function.Functions;
import org.scijava.util.MersenneTwisterFast;

/**
 * Tests several ways to create an image
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */

public class CreateLabelingTest extends AbstractOpTest {

	private static final int TEST_SIZE = 100;
	private static final long SEED = 0x12345678;

	@Test
	public void testImageDimensions() {

		final MersenneTwisterFast randomGenerator = new MersenneTwisterFast(SEED);

		// TODO can we keep this?
		BiFunction<Dimensions, IntType, ImgLabeling<String, IntType>> createFunc = Functions.match(ops,
				"create.imgLabeling", new Nil<Dimensions>() {
				}, new Nil<IntType>() {
				}, new Nil<ImgLabeling<String, IntType>>() {
				});

		for (int i = 0; i < TEST_SIZE; i++) {

			// between 2 and 5 dimensions
			final long[] dim = new long[randomGenerator.nextInt(4) + 2];

			// between 2 and 10 pixels per dimensions
			for (int j = 0; j < dim.length; j++) {
				dim[j] = randomGenerator.nextInt(9) + 2;
			}

			// create imglabeling
			@SuppressWarnings("unchecked")
			final ImgLabeling<String, IntType> img = createFunc.apply(new FinalDimensions(dim), new IntType());

			assertArrayEquals(dim, Intervals.dimensionsAsLongArray(img), "Labeling Dimensions:");
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testImageFactory() {

		final Dimensions dim = new FinalDimensions(10, 10, 10);

		Functions.Arity3<Dimensions, IntType, ImgFactory<IntType>, ImgLabeling<String, IntType>> createFunc = Functions
				.match(ops, "create.imgLabeling", new Nil<Dimensions>() {
				}, new Nil<IntType>() {
				}, new Nil<ImgFactory<IntType>>() {
				}, new Nil<ImgLabeling<String, IntType>>() {
				});

		assertEquals(ArrayImgFactory.class,
				((Img<IntType>) createFunc.apply(dim, new IntType(), new ArrayImgFactory<>(new IntType())).getIndexImg())
						.factory().getClass(), "Labeling Factory: ");

		assertEquals(CellImgFactory.class,
				((Img<IntType>) createFunc.apply(dim, new IntType(), new CellImgFactory<>(new IntType())).getIndexImg())
						.factory().getClass(), "Labeling Factory: ");

	}

	@Test
	public void testImageType() {

		assertEquals(String.class, createLabelingWithType("1").firstElement().toArray()[0]
			.getClass(), "Labeling Type");

		assertEquals(Integer.class, createLabelingWithType(1).firstElement().toArray()[0]
			.getClass(), "Labeling Type");

		assertEquals(Double.class, createLabelingWithType(1d).firstElement()
			.toArray()[0].getClass(), "Labeling Type");

		assertEquals(Float.class, createLabelingWithType(1f).firstElement()
			.toArray()[0].getClass(), "Labeling Type");
	}

	@SuppressWarnings("unchecked")
	private <I> ImgLabeling<I, ?> createLabelingWithType(final I type) {

		BiFunction<Dimensions, IntType, ImgLabeling<I, IntType>> createFunc = Functions.match(ops,
				"create.imgLabeling", new Nil<Dimensions>() {
				}, new Nil<IntType>() {
				}, new Nil<ImgLabeling<I, IntType>>() {
				});
		final ImgLabeling<I, ?> imgLabeling = createFunc.apply(new FinalDimensions(10, 10, 10), new IntType());
		imgLabeling.cursor().next().add(type);
		return imgLabeling;
	}
}
