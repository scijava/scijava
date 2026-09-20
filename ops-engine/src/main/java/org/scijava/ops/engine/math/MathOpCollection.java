/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.scijava.priority.Priority;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class MathOpCollection implements OpCollection {

	@OpField(names = MathOps.ADD, priority = Priority.LOW)
	public static final BiFunction<Number, Number, Double> addDoublesFunction = (
		x, y) -> x.doubleValue() + y.doubleValue();

	@OpField(names = MathOps.ADD, priority = Priority.EXTREMELY_HIGH)
	public static final BinaryOperator<Double> addDoublesOperator = (x, y) -> x +
		y;

	@OpField(names = MathOps.SUB)
	public static final BiFunction<Number, Number, Double> subDoublesFunction = (
		t, u) -> t.doubleValue() - u.doubleValue();

	@OpField(names = MathOps.MUL)
	public static final BiFunction<Number, Number, Double> mulDoublesFunction = (
		t, u) -> t.doubleValue() * u.doubleValue();

	@OpField(names = MathOps.DIV)
	public static final BiFunction<Number, Number, Double> divDoublesFunction = (
		t, u) -> t.doubleValue() / u.doubleValue();

	@OpField(names = MathOps.MOD)
	public static final BiFunction<Number, Number, Double> remainderDoublesFunction =
		(t, u) -> t.doubleValue() % u.doubleValue();

}
