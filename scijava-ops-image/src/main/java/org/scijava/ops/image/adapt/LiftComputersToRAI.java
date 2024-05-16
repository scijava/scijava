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

/*
* This is autogenerated source code -- DO NOT EDIT. Instead, edit the
* corresponding template in templates/ and rerun bin/generate.groovy.
*/

package org.scijava.ops.image.adapt;

import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Functions;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.util.Util;

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
public final class LiftComputersToRAI {

	private LiftComputersToRAI() {
		// prevent instantiation of static utility class
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, O, RAII1 extends RandomAccessibleInterval<I1>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity1<RAII1, RAIO> lift11(Computers.Arity1<I1, O> computer)
	{
		return (raiInput1, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiOutput).multiThreaded() //
				.forEachPixel((i1, out) -> computer.compute(i1, out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, O, RAII1 extends RandomAccessibleInterval<I1>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity2<RAII1, I2, RAIO> lift21(
			Computers.Arity2<I1, I2, O> computer)
	{
		return (raiInput1, in2, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiOutput).multiThreaded() //
				.forEachPixel((i1, out) -> computer.compute(i1, in2, out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, O, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity2<RAII1, RAII2, RAIO> lift22(
			Computers.Arity2<I1, I2, O> computer)
	{
		return (raiInput1, raiInput2, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiInput2, raiOutput).multiThreaded() //
				.forEachPixel((i1, i2, out) -> computer.compute(i1, i2, out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, O, RAII1 extends RandomAccessibleInterval<I1>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity3<RAII1, I2, I3, RAIO> lift31(
			Computers.Arity3<I1, I2, I3, O> computer)
	{
		return (raiInput1, in2, in3, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiOutput).multiThreaded() //
				.forEachPixel((i1, out) -> computer.compute(i1, in2, in3, out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, O, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity3<RAII1, RAII2, I3, RAIO> lift32(
			Computers.Arity3<I1, I2, I3, O> computer)
	{
		return (raiInput1, raiInput2, in3, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiInput2, raiOutput).multiThreaded() //
				.forEachPixel((i1, i2, out) -> computer.compute(i1, i2, in3, out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, O, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity3<RAII1, RAII2, RAII3, RAIO> lift33(
			Computers.Arity3<I1, I2, I3, O> computer)
	{
		return (raiInput1, raiInput2, raiInput3, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiOutput)
				.multiThreaded() //
				.forEachPixel((i1, i2, i3, out) -> computer.compute(i1, i2, i3, out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, O, RAII1 extends RandomAccessibleInterval<I1>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity4<RAII1, I2, I3, I4, RAIO> lift41(
			Computers.Arity4<I1, I2, I3, I4, O> computer)
	{
		return (raiInput1, in2, in3, in4, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiOutput).multiThreaded() //
				.forEachPixel((i1, out) -> computer.compute(i1, in2, in3, in4, out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, O, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity4<RAII1, RAII2, I3, I4, RAIO> lift42(
			Computers.Arity4<I1, I2, I3, I4, O> computer)
	{
		return (raiInput1, raiInput2, in3, in4, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiInput2, raiOutput).multiThreaded() //
				.forEachPixel((i1, i2, out) -> computer.compute(i1, i2, in3, in4, out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, O, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity4<RAII1, RAII2, RAII3, I4, RAIO> lift43(
			Computers.Arity4<I1, I2, I3, I4, O> computer)
	{
		return (raiInput1, raiInput2, raiInput3, in4, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiOutput)
				.multiThreaded() //
				.forEachPixel((i1, i2, i3, out) -> computer.compute(i1, i2, i3, in4,
					out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, O, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAII4 extends RandomAccessibleInterval<I4>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity4<RAII1, RAII2, RAII3, RAII4, RAIO> lift44(
			Computers.Arity4<I1, I2, I3, I4, O> computer)
	{
		return (raiInput1, raiInput2, raiInput3, raiInput4, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiInput4,
				raiOutput).multiThreaded() //
				.forEachPixel((i1, i2, i3, i4, out) -> computer.compute(i1, i2, i3, i4,
					out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, I5, O, RAII1 extends RandomAccessibleInterval<I1>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity5<RAII1, I2, I3, I4, I5, RAIO> lift51(
			Computers.Arity5<I1, I2, I3, I4, I5, O> computer)
	{
		return (raiInput1, in2, in3, in4, in5, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiOutput).multiThreaded() //
				.forEachPixel((i1, out) -> computer.compute(i1, in2, in3, in4, in5,
					out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, I5, O, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity5<RAII1, RAII2, I3, I4, I5, RAIO> lift52(
			Computers.Arity5<I1, I2, I3, I4, I5, O> computer)
	{
		return (raiInput1, raiInput2, in3, in4, in5, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiInput2, raiOutput).multiThreaded() //
				.forEachPixel((i1, i2, out) -> computer.compute(i1, i2, in3, in4, in5,
					out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, I5, O, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity5<RAII1, RAII2, RAII3, I4, I5, RAIO> lift53(
			Computers.Arity5<I1, I2, I3, I4, I5, O> computer)
	{
		return (raiInput1, raiInput2, raiInput3, in4, in5, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiOutput)
				.multiThreaded() //
				.forEachPixel((i1, i2, i3, out) -> computer.compute(i1, i2, i3, in4,
					in5, out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, I5, O, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAII4 extends RandomAccessibleInterval<I4>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity5<RAII1, RAII2, RAII3, RAII4, I5, RAIO> lift54(
			Computers.Arity5<I1, I2, I3, I4, I5, O> computer)
	{
		return (raiInput1, raiInput2, raiInput3, raiInput4, in5, raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiInput4,
				raiOutput).multiThreaded() //
				.forEachPixel((i1, i2, i3, i4, out) -> computer.compute(i1, i2, i3, i4,
					in5, out));
		};
	}

	/**
	 * @param originalOp the original Op
	 * @return the adapted Op
	 * @implNote op names='engine.adapt', priority='100.'
	 */
	public static <I1, I2, I3, I4, I5, O, RAII1 extends RandomAccessibleInterval<I1>, RAII2 extends RandomAccessibleInterval<I2>, RAII3 extends RandomAccessibleInterval<I3>, RAII4 extends RandomAccessibleInterval<I4>, RAII5 extends RandomAccessibleInterval<I5>, RAIO extends RandomAccessibleInterval<O>>
		Computers.Arity5<RAII1, RAII2, RAII3, RAII4, RAII5, RAIO> lift55(
			Computers.Arity5<I1, I2, I3, I4, I5, O> computer)
	{
		return (raiInput1, raiInput2, raiInput3, raiInput4, raiInput5,
			raiOutput) -> {
			LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiInput4,
				raiInput5, raiOutput).multiThreaded() //
				.forEachPixel((i1, i2, i3, i4, i5, out) -> computer.compute(i1, i2, i3,
					i4, i5, out));
		};
	}
}
