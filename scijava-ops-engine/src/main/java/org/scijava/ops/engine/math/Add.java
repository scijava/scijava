/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.math;

import com.google.common.collect.Streams;

import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.scijava.priority.Priority;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public final class Add<M extends Number, I extends Iterable<M>> implements
	OpCollection
{

	public static final String NAMES = MathOps.ADD;

	// --------- Functions ---------

	@OpField(names = NAMES) // vars = "number1, number2, result"
	public static final BiFunction<Double, Double, Double> MathAddDoublesFunction =
		(t, u) -> t + u;

	@OpField(priority = Priority.HIGH, names = NAMES) // vars = "iter1, iter2,
																										// resultArray"
	public final BiFunction<I, I, Iterable<Double>> MathPointwiseAddIterablesFunction =
		(i1, i2) -> {
			Stream<? extends Number> s1 = Streams.stream(
				(Iterable<? extends Number>) i1);
			Stream<? extends Number> s2 = Streams.stream(
				(Iterable<? extends Number>) i2);
			return () -> Streams.zip(s1, s2, (e1, e2) -> e1.doubleValue() + e2
				.doubleValue()).iterator();
		};

	// --------- Computers ---------

	@OpField(names = NAMES) // vars = "integer1, integer2, resultInteger"
	public static final BiFunction<BigInteger, BigInteger, BigInteger> MathAddBigIntegersComputer =
		(t, u) -> t.add(u);

	@OpField(names = NAMES) // vars = "array1, array2, resultArray"
	public static final Computers.Arity2<double[], double[], double[]> MathPointwiseAddDoubleArraysComputer =
		(in1, in2, out) -> {
			for (int i = 0; i < out.length; i++) {
				out[i] = in1[i] + in2[i];
			}
		};

	// --------- Inplaces ---------

	@OpField(names = NAMES) // vars = "arrayIO, array1"
	public static final Inplaces.Arity2_1<double[], double[]> MathPointwiseAddDoubleArraysInplace1 =
		(io, in2) -> {
			for (int i = 0; i < io.length; i++) {
				io[i] += in2[i];
			}
		};

	@OpField(names = NAMES) // vars = "val1, val2, output"
	public final BiFunction<M, M, Double> MathAddNumbersFunction = (in1,
		in2) -> in1.doubleValue() + in2.doubleValue();

	@OpField(names = NAMES) // vars = "iterable, result"
	public final Function<Iterable<M>, Double> MathReductionAdd = (iterable) -> {
		return StreamSupport.stream(iterable.spliterator(), false).mapToDouble(
			Number::doubleValue).sum();
	};

}
