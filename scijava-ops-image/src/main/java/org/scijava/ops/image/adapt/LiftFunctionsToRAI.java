/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

/*
* This is autogenerated source code -- DO NOT EDIT. Instead, edit the
* corresponding template in templates/ and rerun bin/generate.groovy.
*/

package org.scijava.ops.image.adapt;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.Type;
import net.imglib2.util.Util;

import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;

/**
 * Lifts {@link Functions} operating on some types {@code I1, I2, ..., In},
 * {@code O extends Type<O>} to a Function operating on
 * {@link RandomAccessibleInterval}s of those types. An output
 * {@link RandomAccessibleInterval} is created based off of the dimensions of
 * the first input image and using the output type of the passed
 * {@link Function}. The {@Function}{@code <I, O>} is then applied iteratively
 * over each pixel of the input image(s). NOTE: It is assumed that the input
 * {@code RAI}s are the same size. If they are not, the lifted {@link Function}
 * will only iteratively process the images until one image runs out of pixels
 * to iterate over.
 *
 * @author Gabriel Selzer
 * @author Mark Hiner
 */
public final class LiftFunctionsToRAI {

	private LiftFunctionsToRAI() {
		// prevent instantiation of static utility class
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAIO extends RandomAccessibleInterval<O>>
		Function<RAII1, RAIO> lift11(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Function<I1, O> func //
	) {
		return (in1) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			O outType = func.apply(inType1);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, outImg).multiThreaded() //
				.forEachPixel((i1, o) -> o.set(func.apply(i1)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAIO extends RandomAccessibleInterval<O>>
		BiFunction<RAII1, I2, RAIO> lift21(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			BiFunction<I1, I2, O> func //
	) {
		return (in1, in2) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			O outType = func.apply(inType1, in2);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, outImg).multiThreaded() //
				.forEachPixel((i1, o) -> o.set(func.apply(i1, in2)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAIO extends RandomAccessibleInterval<O>>
		BiFunction<RAII1, RAII2, RAIO> lift22(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			BiFunction<I1, I2, O> func //
	) {
		return (in1, in2) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			I2 inType2 = Util.getTypeFromInterval(in2);
			O outType = func.apply(inType1, inType2);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, in2, outImg).multiThreaded() //
				.forEachPixel((i1, i2, o) -> o.set(func.apply(i1, i2)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity3<RAII1, I2, I3, RAIO> lift31(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity3<I1, I2, I3, O> func //
	) {
		return (in1, in2, in3) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			O outType = func.apply(inType1, in2, in3);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, outImg).multiThreaded() //
				.forEachPixel((i1, o) -> o.set(func.apply(i1, in2, in3)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity3<RAII1, RAII2, I3, RAIO> lift32(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity3<I1, I2, I3, O> func //
	) {
		return (in1, in2, in3) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			I2 inType2 = Util.getTypeFromInterval(in2);
			O outType = func.apply(inType1, inType2, in3);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, in2, outImg).multiThreaded() //
				.forEachPixel((i1, i2, o) -> o.set(func.apply(i1, i2, in3)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity3<RAII1, RAII2, RAII3, RAIO> lift33(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity3<I1, I2, I3, O> func //
	) {
		return (in1, in2, in3) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			I2 inType2 = Util.getTypeFromInterval(in2);
			I3 inType3 = Util.getTypeFromInterval(in3);
			O outType = func.apply(inType1, inType2, inType3);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, in2, in3, outImg).multiThreaded() //
				.forEachPixel((i1, i2, i3, o) -> o.set(func.apply(i1, i2, i3)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity4<RAII1, I2, I3, I4, RAIO> lift41(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity4<I1, I2, I3, I4, O> func //
	) {
		return (in1, in2, in3, in4) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			O outType = func.apply(inType1, in2, in3, in4);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, outImg).multiThreaded() //
				.forEachPixel((i1, o) -> o.set(func.apply(i1, in2, in3, in4)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity4<RAII1, RAII2, I3, I4, RAIO> lift42(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity4<I1, I2, I3, I4, O> func //
	) {
		return (in1, in2, in3, in4) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			I2 inType2 = Util.getTypeFromInterval(in2);
			O outType = func.apply(inType1, inType2, in3, in4);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, in2, outImg).multiThreaded() //
				.forEachPixel((i1, i2, o) -> o.set(func.apply(i1, i2, in3, in4)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity4<RAII1, RAII2, RAII3, I4, RAIO> lift43(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity4<I1, I2, I3, I4, O> func //
	) {
		return (in1, in2, in3, in4) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			I2 inType2 = Util.getTypeFromInterval(in2);
			I3 inType3 = Util.getTypeFromInterval(in3);
			O outType = func.apply(inType1, inType2, inType3, in4);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, in2, in3, outImg).multiThreaded() //
				.forEachPixel((i1, i2, i3, o) -> o.set(func.apply(i1, i2, i3, in4)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAII4 extends RandomAccessibleInterval<I4>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity4<RAII1, RAII2, RAII3, RAII4, RAIO> lift44(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity4<I1, I2, I3, I4, O> func //
	) {
		return (in1, in2, in3, in4) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			I2 inType2 = Util.getTypeFromInterval(in2);
			I3 inType3 = Util.getTypeFromInterval(in3);
			I4 inType4 = Util.getTypeFromInterval(in4);
			O outType = func.apply(inType1, inType2, inType3, inType4);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, in2, in3, in4, outImg).multiThreaded() //
				.forEachPixel((i1, i2, i3, i4, o) -> o.set(func.apply(i1, i2, i3, i4)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, I5, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity5<RAII1, I2, I3, I4, I5, RAIO> lift51(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity5<I1, I2, I3, I4, I5, O> func //
	) {
		return (in1, in2, in3, in4, in5) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			O outType = func.apply(inType1, in2, in3, in4, in5);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, outImg).multiThreaded() //
				.forEachPixel((i1, o) -> o.set(func.apply(i1, in2, in3, in4, in5)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, I5, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity5<RAII1, RAII2, I3, I4, I5, RAIO> lift52(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity5<I1, I2, I3, I4, I5, O> func //
	) {
		return (in1, in2, in3, in4, in5) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			I2 inType2 = Util.getTypeFromInterval(in2);
			O outType = func.apply(inType1, inType2, in3, in4, in5);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, in2, outImg).multiThreaded() //
				.forEachPixel((i1, i2, o) -> o.set(func.apply(i1, i2, in3, in4, in5)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, I5, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity5<RAII1, RAII2, RAII3, I4, I5, RAIO> lift53(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity5<I1, I2, I3, I4, I5, O> func //
	) {
		return (in1, in2, in3, in4, in5) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			I2 inType2 = Util.getTypeFromInterval(in2);
			I3 inType3 = Util.getTypeFromInterval(in3);
			O outType = func.apply(inType1, inType2, inType3, in4, in5);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, in2, in3, outImg).multiThreaded() //
				.forEachPixel((i1, i2, i3, o) -> o.set(func.apply(i1, i2, i3, in4,
					in5)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, I5, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAII4 extends RandomAccessibleInterval<I4>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity5<RAII1, RAII2, RAII3, RAII4, I5, RAIO> lift54(@OpDependency(
			name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity5<I1, I2, I3, I4, I5, O> func //
	) {
		return (in1, in2, in3, in4, in5) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			I2 inType2 = Util.getTypeFromInterval(in2);
			I3 inType3 = Util.getTypeFromInterval(in3);
			I4 inType4 = Util.getTypeFromInterval(in4);
			O outType = func.apply(inType1, inType2, inType3, inType4, in5);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, in2, in3, in4, outImg).multiThreaded() //
				.forEachPixel((i1, i2, i3, i4, o) -> o.set(func.apply(i1, i2, i3, i4,
					in5)));
			return outImg;
		};
	}

	/**
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, I5, O extends Type<O>, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAII4 extends RandomAccessibleInterval<I4>, RAII5 extends RandomAccessibleInterval<I5>, RAIO extends RandomAccessibleInterval<O>>
		Functions.Arity5<RAII1, RAII2, RAII3, RAII4, RAII5, RAIO> lift55(
			@OpDependency(
				name = "engine.create") BiFunction<Dimensions, O, RAIO> imgCreator, //
			Functions.Arity5<I1, I2, I3, I4, I5, O> func //
	) {
		return (in1, in2, in3, in4, in5) -> {
			I1 inType1 = Util.getTypeFromInterval(in1);
			I2 inType2 = Util.getTypeFromInterval(in2);
			I3 inType3 = Util.getTypeFromInterval(in3);
			I4 inType4 = Util.getTypeFromInterval(in4);
			I5 inType5 = Util.getTypeFromInterval(in5);
			O outType = func.apply(inType1, inType2, inType3, inType4, inType5);
			RAIO outImg = imgCreator.apply(in1, outType);
			LoopBuilder.setImages(in1, in2, in3, in4, in5, outImg).multiThreaded() //
				.forEachPixel((i1, i2, i3, i4, i5, o) -> o.set(func.apply(i1, i2, i3,
					i4, i5)));
			return outImg;
		};
	}
}