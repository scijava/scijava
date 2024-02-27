/*
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

 package org.scijava.ops.image.convert.copy;

 import net.imglib2.RandomAccessibleInterval;
 import net.imglib2.loops.LoopBuilder;
 import net.imglib2.type.numeric.ComplexType;
 import net.imglib2.type.numeric.RealType;
 
 /**
  * Copies the value of one {@link RealType} into another using {@code double}
  * precision.
  *
  * @author Martin Horn (University of Konstanz)
  * @author Gabriel Selzer
  */
 public class CopyComplexTypes {
 
	 /**
	  * Copies the real and imaginary components from {@code input} to
	  * {@code output}.
	  *
	  * @param input the input
	  * @param output the preallocated output
	  * @implNote op names='convert.copy, engine.copy', priority='-10000',
	  *           type=Computer
	  */
	 public static <I extends ComplexType<I>, O extends ComplexType<O>> void
		 copyComplexTypes(I input, O output)
	 {
		 output.setReal(input.getRealDouble());
		 output.setImaginary(input.getImaginaryDouble());
	 }
 
	 /**
	  * Copies the real and imaginary components from each element of {@code input}
	  * to the corresponding element of {@code output}.
	  *
	  * @param input the input
	  * @param output the preallocated output
	  * @implNote op names='convert.copy, engine.copy', priority='-10000',
	  *           type=Computer
	  */
	 public static < //
			 I extends ComplexType<I>, //
			 O extends ComplexType<O>, //
			 RAII extends RandomAccessibleInterval<I>, //
			 RAIO extends RandomAccessibleInterval<O> //
	 > void copyRAIs(RAII input, RAIO output) {
		 LoopBuilder.setImages(input, output) //
			 .multiThreaded() //
			 .forEachPixel(CopyComplexTypes::copyComplexTypes);
	 }
 
 }