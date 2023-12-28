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
package org.scijava.ops.tutorial;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

/**
 * {@link OpCollection}s, as the name suggests, define many Ops within one
 * class.
 * <p>
 * There are two different types of Ops that can be written inside {@link OpCollection}s:
 * <ol>
 *   <li>{@link OpField}s are Ops written as {@code public final} {@link Field}s.</li>
 *   <li>{@link OpMethod}s are Ops written as {@code public static} {@link Method}s.</li>
 * </ol>
 * Each {@link OpCollection} can contain an arbitrary number of either type of Op.
 */
public class WritingOpCollections implements OpCollection {

	/**
	 * {@link OpField}s are Ops written as {@link Field}s. They <b>must</b> be:
	 * <ul>
	 *   <li>public</li>
	 *   <li>final</li>
	 * </ul>
	 * One major benefit of {@link OpField}s is that they can use Java's lambda
	 * syntax, maximizing expressiveness.
	 */
	@OpField(names="test.opField.power")
	public final BiFunction<Double, Double, Double> opFieldPower =
			(b, e) -> Math.pow(b, e);

	/**
	 * {@link OpMethod}s are Ops written as {@link Method}s. They <b>must</b> be:
	 * <ul>
	 *   <li>public</li>
	 *   <li>static</li>
	 * </ul>
	 *<p>
	 *<b>In addition, Ops written as methods must specify their Op type.</b>
	 * This tells SciJava Ops whether this function should become a Computer,
	 * an Inplace, or something else entirely.
	 */
	@OpMethod(names = "test.opMethod.power", type=BiFunction.class)
	public static Double opMethodPower(Double b, Double e) {
		return Math.pow(b, e);
	}

	public static void main(String... args){
		OpEnvironment ops = OpEnvironment.build();

		Double result = ops.binary("test.opField.power") //
				.input(2.0, 10.0) //
				.outType(Double.class) //
				.apply();

		System.out.println("2.0 to the power of 10.0 is " + result);

		result = ops.binary("test.opMethod.power") //
				.input(2.0, 20.0) //
				.outType(Double.class) //
				.apply();

		System.out.println("2.0 to the power of 20.0 is " + result);
	}

}
