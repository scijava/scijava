/*-
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
package net.imagej.ops2.types.adapt;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.types.Nil;

public class LiftComputersToRAITest<I extends RealType<I>, O extends RealType<O>> extends AbstractOpTest {

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final Computers.Arity1<I, O> testOp = (in, out) -> out.setReal(10.);

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final Computers.Arity2<I, I, O> testOp2 = (in1, in2, out) -> out.setReal(20.);

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final Computers.Arity3<I, I, I, O> testOp3 = (in1, in2, in3, out) -> out.setReal(30.);

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final Computers.Arity4<I, I, I, I, O> testOp4 = (in1, in2, in3, in4, out) -> out.setReal(40.);

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final Computers.Arity5<I, I, I, I, I, O> testOp5 = (in1, in2, in3, in4, in5, out) -> out.setReal(50.);

	@Test
	public void testLiftComputer1ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").arity2().input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		RandomAccessibleInterval<DoubleType> output = ops.op("create.img").arity2().input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		ops.op("test.liftImg").arity1().input(input).output(output).compute();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 10.);
		}
				
	}

	@Test
	public void testLiftComputer2ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").arity2().input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		RandomAccessibleInterval<DoubleType> output = ops.op("create.img").arity2().input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		ops.op("test.liftImg").arity2().input(input, input).output(output).compute();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 20.);
		}
				
	}

	@Test
	public void testLiftComputer3ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").arity2().input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		RandomAccessibleInterval<DoubleType> output = ops.op("create.img").arity2().input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		ops.op("test.liftImg").arity3().input(input, input, input).output(output).compute();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 30.);
		}
				
	}

	@Test
	public void testLiftComputer4ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").arity2().input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		RandomAccessibleInterval<DoubleType> output = ops.op("create.img").arity2().input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		ops.op("test.liftImg").arity4().input(input, input, input, input).output(output).compute();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 40.);
		}
				
	}

	@Test
	public void testLiftComputer5ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").arity2().input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		RandomAccessibleInterval<DoubleType> output = ops.op("create.img").arity2().input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		ops.op("test.liftImg").arity5().input(input, input, input, input, input).output(output).compute();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 50.);
		}
				
	}

}
