/*-
 * #%L
 * Interactive tutorial for SciJava Ops.
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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

package org.scijava.ops.tutorial;

import org.scijava.function.Producer;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * SciJvaa Ops allows users to take advantage of the strong type safety of the
 * Java language, which can provide benefits in certainty and efficiency. The
 * downside of this is verbosity, especially in obtaining generically-typed
 * outputs from OpBuilder calls.
 * <p>
 * Luckily, SciJava Ops allows you to specify the generic types of outputs using
 * the {@link Nil} type, as shown in this tutorial.
 *
 * @author Gabriel Selzer
 */
public class UsingNils {

	/**
	 * This Op returns a 10x10 image of unsigned bytes
	 *
	 * @output
	 * @implNote op names="tutorial.nils"
	 */
	public final Producer<Img<UnsignedByteType>> imgOfBytes = //
		() -> ArrayImgs.unsignedBytes(10, 10);

	/**
	 * This Op returns a 10x10 image of doubles
	 *
	 * @output
	 * @implNote op names="tutorial.nils"
	 */
	public final Producer<Img<DoubleType>> imgOfDoubles = //
		() -> ArrayImgs.doubles(10, 10);

	public static void main(String... args) {
		OpEnvironment ops = OpEnvironment.build();

		// The following is the syntax used to create a Nil that encodes the type we
		// want. Namely, if we want to ensure that the return is an Img<DoubleType>,
		// we put that type as the generic type of the Nil:
		Nil<Img<DoubleType>> outType = new Nil<>() {};

		// By passing a Nil as the output type, instead of as a class, we guarantee
		// the generic typing of the output. Note that the Nil allows us to
		// differentiate between the two Ops above, one of which would instead
		// return an ArrayImg of unsigned bytes.
		Img<DoubleType> out = ops.op("tutorial.nils").outType(outType).create();

		System.out.println("Found an image " + out + " of doubles!");
	}

}
