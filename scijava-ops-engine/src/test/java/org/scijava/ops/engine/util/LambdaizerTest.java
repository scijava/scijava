/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;

/**
 * Confirms the capabilities of the lambdaizer to convert methods into their
 * respective functional interfaces.
 *
 * @author Gabriel Selzer
 */
public class LambdaizerTest {

	public static void computer(String in, long[] out) {
		out[0] = in.length();
	}

	@Test
	public void testComputer() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(void.class,
			String.class, long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class,
			"computer", methodTypeFunction);

		Computers.Arity1<String, long[]> dynamicComputer = Lambdas.lambdaize(
			Computers.Arity1.class, functionHandle);
		String input = "four";
		long[] output = { 0 };
		dynamicComputer.accept(input, output);
		assert (output[0] == 4);

	}

	public static void biComputer(String in1, String in2, long[] out) {
		out[0] = in1.length() + in2.length();
	}

	@Test
	public void testComputer2() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(void.class,
			String.class, String.class, long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class,
			"biComputer", methodTypeFunction);

		Computers.Arity2<String, String, long[]> dynamicComputer = Lambdas
			.lambdaize(Computers.Arity2.class, functionHandle);
		String input = "four";
		String input2 = "four";
		long[] output = { 0 };
		dynamicComputer.accept(input, input2, output);
		assert (output[0] == 8);

	}

	public static void computer3(String in1, String in2, String in3, long[] out) {
		out[0] = in1.length() + in2.length() + in3.length();
	}

	@Test
	public void testComputer3() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(void.class,
			String.class, String.class, String.class, long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class,
			"computer3", methodTypeFunction);

		Computers.Arity3<String, String, String, long[]> dynamicComputer = Lambdas
			.lambdaize(Computers.Arity3.class, functionHandle);
		String input = "four";
		String input2 = "four";
		String input3 = "four";
		long[] output = { 0 };
		dynamicComputer.accept(input, input2, input3, output);
		assert (output[0] == 12);

		// sanity check: make sure that compute also works
		dynamicComputer.compute(input, input2, input3, output);
	}

	public static long[] function(String in) {
		long[] result = { 0 };
		result[0] = in.length();
		return result;
	}

	@Test
	public void testFunction() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(long[].class,
			String.class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class,
			"function", methodTypeFunction);

		Function<String, long[]> dynamicFunction = Lambdas.lambdaize(Function.class,
			functionHandle);
		String input = "four";
		long[] output = dynamicFunction.apply(input);
		assert (output[0] == 4);
	}

	public static long[] biFunction(String in, String in2) {
		long[] result = { 0 };
		result[0] = in.length() + in2.length();
		return result;
	}

	@Test
	public void testBiFunction() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(long[].class,
			String.class, String.class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class,
			"biFunction", methodTypeFunction);

		BiFunction<String, String, long[]> dynamicFunction = Lambdas.lambdaize(
			BiFunction.class, functionHandle);
		String input = "four";
		String input2 = "four";
		long[] output = dynamicFunction.apply(input, input2);
		assert (output[0] == 8);
	}

	public static void inplace(long[] io) {
		for (int i = 0; i < io.length; i++) {
			io[i] *= 2;
		}
	}

	@Test
	public void testInplace() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(void.class,
			long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class,
			"inplace", methodTypeFunction);

		Inplaces.Arity1<long[]> dynamicFunction = Lambdas.lambdaize(
			Inplaces.Arity1.class, functionHandle);
		long[] io = { 1, 2, 3 };
		long[] expected = { 2, 4, 6 };
		dynamicFunction.mutate(io);
		for (int i = 0; i < io.length; i++) {
			assert (io[i] == expected[i]);
		}
	}

	public static void biInplaceFirst(long[] io, long[] i2) {
		for (int i = 0; i < io.length; i++) {
			io[i] += i2[i];
		}
	}

	@Test
	public void testInplace2_1() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(void.class,
			long[].class, long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class,
			"biInplaceFirst", methodTypeFunction);

		Inplaces.Arity2_1<long[], long[]> dynamicFunction = Lambdas.lambdaize(
			Inplaces.Arity2_1.class, functionHandle);
		long[] io = { 1, 2, 3 };
		long[] i2 = { 3, 6, 9 };
		long[] expected = { 4, 8, 12 };
		dynamicFunction.mutate(io, i2);
		for (int i = 0; i < io.length; i++) {
			assert (io[i] == expected[i]);
		}
	}

	public static void biInplaceSecond(long[] i1, long[] io) {
		for (int i = 0; i < io.length; i++) {
			io[i] += i1[i];
		}
	}

	@Test
	public void testInplace2_2() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(void.class,
			long[].class, long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class,
			"biInplaceSecond", methodTypeFunction);

		Inplaces.Arity2_2<long[], long[]> dynamicFunction = Lambdas.lambdaize(
			Inplaces.Arity2_2.class, functionHandle);
		long[] io = { 1, 2, 3 };
		long[] i2 = { 3, 6, 9 };
		long[] expected = { 4, 8, 12 };
		dynamicFunction.mutate(io, i2);
		for (int i = 0; i < io.length; i++) {
			assert (i2[i] == expected[i]);
		}
	}

}
