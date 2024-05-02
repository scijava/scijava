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

package org.scijava.ops.image.types.adapt;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.function.Functions;
import org.scijava.types.Nil;

public class LiftFunctionsToRAITest<I extends RealType<I>> extends
	AbstractOpTest
{

	/**
	 * @implNote op names='test.function.liftImg'
	 */
	public final Function<I, DoubleType> testOp = (in) -> new DoubleType(10d);

	/**
	 * @implNote op names='test.function.liftImg'
	 */
	public final BiFunction<I, I, DoubleType> testOp2 = (in1,
		in2) -> new DoubleType(20d);

	/**
	 * @implNote op names='test.function.liftImg'
	 */
	public final Functions.Arity3<I, I, I, DoubleType> testOp3 = (in1, in2,
		in3) -> new DoubleType(30d);

	/**
	 * @implNote op names='test.function.liftImg'
	 */
	public final Functions.Arity4<I, I, I, I, DoubleType> testOp4 = (in1, in2,
		in3, in4) -> new DoubleType(40d);

	/**
	 * @implNote op names='test.function.liftImg'
	 */
	public final Functions.Arity5<I, I, I, I, I, DoubleType> testOp5 = (in1, in2,
		in3, in4, in5) -> new DoubleType(50d);

	@Test
	public void testLiftFunction1ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(
			new FinalDimensions(3, 3), new DoubleType()).outType(
				new Nil<Img<DoubleType>>()
				{}).apply();

		RandomAccessibleInterval<DoubleType> output = ops.op(
			"test.function.liftImg").input(input).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while (cursor.hasNext()) {
			assert (cursor.next().get() == 10.);
		}

	}

	@Test
	public void testLiftFunction2ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(
			new FinalDimensions(3, 3), new DoubleType()).outType(
				new Nil<Img<DoubleType>>()
				{}).apply();

		RandomAccessibleInterval<DoubleType> output = ops.op(
			"test.function.liftImg").input(input, input).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while (cursor.hasNext()) {
			assert (cursor.next().get() == 20.);
		}

	}

	@Test
	public void testLiftFunction3ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(
			new FinalDimensions(3, 3), new DoubleType()).outType(
				new Nil<Img<DoubleType>>()
				{}).apply();

		RandomAccessibleInterval<DoubleType> output = ops.op(
			"test.function.liftImg").input(input, input, input).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while (cursor.hasNext()) {
			assert (cursor.next().get() == 30.);
		}

	}

	@Test
	public void testLiftFunction4ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(
			new FinalDimensions(3, 3), new DoubleType()).outType(
				new Nil<Img<DoubleType>>()
				{}).apply();

		RandomAccessibleInterval<DoubleType> output = ops.op(
			"test.function.liftImg").input(input, input, input, input).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while (cursor.hasNext()) {
			assert (cursor.next().get() == 40.);
		}

	}

	@Test
	public void testLiftFunction5ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(
			new FinalDimensions(3, 3), new DoubleType()).outType(
				new Nil<Img<DoubleType>>()
				{}).apply();

		RandomAccessibleInterval<DoubleType> output = ops.op(
			"test.function.liftImg").input(input, input, input, input, input).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while (cursor.hasNext()) {
			assert (cursor.next().get() == 50.);
		}

	}

}
