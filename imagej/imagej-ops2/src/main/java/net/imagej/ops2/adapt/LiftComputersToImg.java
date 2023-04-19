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

package net.imagej.ops2.adapt;


import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.Type;
import org.scijava.function.Computers;

import java.util.function.Function;

public class LiftComputersToImg<I1, I2, I3, I4, I5, O extends Type<O>> {

	/**
	 * @implNote op names='adapt', priority='100.'
	 */
	public final Function<Computers.Arity1<I1, O>, Computers.Arity1<Img<I1>, Img<O>>> lift1 =
			(computer) -> {
				return (raiInput1, raiOutput) -> {
					LoopBuilder.setImages(raiInput1, raiOutput).multiThreaded()
					    .forEachPixel((in1, out) -> computer.compute(in1, out));
				};
			};

	/**
	 * @implNote op names='adapt', priority='100.'
	 */
	public final Function<Computers.Arity2<I1, I2, O>, Computers.Arity2<Img<I1>, Img<I2>, Img<O>>> lift2 =
			(computer) -> {
				return (raiInput1, raiInput2, raiOutput) -> {
					LoopBuilder.setImages(raiInput1, raiInput2, raiOutput).multiThreaded()
					    .forEachPixel((in1, in2, out) -> computer.compute(in1, in2, out));
				};
			};

	/**
	 * @implNote op names='adapt', priority='100.'
	 */
	public final Function<Computers.Arity3<I1, I2, I3, O>, Computers.Arity3<Img<I1>, Img<I2>, Img<I3>, Img<O>>> lift3 =
			(computer) -> {
				return (raiInput1, raiInput2, raiInput3, raiOutput) -> {
					LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiOutput).multiThreaded()
					    .forEachPixel((in1, in2, in3, out) -> computer.compute(in1, in2, in3, out));
				};
			};

	/**
	 * @implNote op names='adapt', priority='100.'
	 */
	public final Function<Computers.Arity4<I1, I2, I3, I4, O>, Computers.Arity4<Img<I1>, Img<I2>, Img<I3>, Img<I4>, Img<O>>> lift4 =
			(computer) -> {
				return (raiInput1, raiInput2, raiInput3, raiInput4, raiOutput) -> {
					LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiInput4, raiOutput).multiThreaded()
					    .forEachPixel((in1, in2, in3, in4, out) -> computer.compute(in1, in2, in3, in4, out));
				};
			};

	/**
	 * @implNote op names='adapt', priority='100.'
	 */
	public final Function<Computers.Arity5<I1, I2, I3, I4, I5, O>, Computers.Arity5<Img<I1>, Img<I2>, Img<I3>, Img<I4>, Img<I5>, Img<O>>> lift5 =
			(computer) -> {
				return (raiInput1, raiInput2, raiInput3, raiInput4, raiInput5, raiOutput) -> {
					LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiInput4, raiInput5, raiOutput).multiThreaded()
					    .forEachPixel((in1, in2, in3, in4, in5, out) -> computer.compute(in1, in2, in3, in4, in5, out));
				};
			};
}