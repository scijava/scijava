package org.scijava.ops.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.inplace.BiInplaceFirst;
import org.scijava.ops.core.inplace.BiInplaceSecond;
import org.scijava.ops.core.inplace.Inplace;

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
		MethodType methodTypeFunction = MethodType.methodType(void.class, String.class, long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class, "computer", methodTypeFunction);

		Computer<String, long[]> dynamicComputer = (Computer<String, long[]>) Adapt.Methods.lambdaize(Computer.class,
				functionHandle);
		String input = "four";
		long[] output = { 0 };
		dynamicComputer.accept(input, output);
		assert (output[0] == 4);

	}

	public static void biComputer(String in1, String in2, long[] out) {
		out[0] = in1.length() + in2.length();
	}

	@Test
	public void testBiComputer() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(void.class, String.class, String.class, long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class, "biComputer", methodTypeFunction);

		BiComputer<String, String, long[]> dynamicComputer = (BiComputer<String, String, long[]>) Adapt.Methods
				.lambdaize(BiComputer.class, functionHandle);
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
		MethodType methodTypeFunction = MethodType.methodType(void.class, String.class, String.class, String.class,
				long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class, "computer3", methodTypeFunction);

		Computer3<String, String, String, long[]> dynamicComputer = (Computer3<String, String, String, long[]>) Adapt.Methods
				.lambdaize(Computer3.class, functionHandle);
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
		MethodType methodTypeFunction = MethodType.methodType(long[].class, String.class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class, "function", methodTypeFunction);

		Function<String, long[]> dynamicFunction = (Function<String, long[]>) Adapt.Methods.lambdaize(Function.class,
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
		MethodType methodTypeFunction = MethodType.methodType(long[].class, String.class, String.class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class, "biFunction", methodTypeFunction);

		BiFunction<String, String, long[]> dynamicFunction = (BiFunction<String, String, long[]>) Adapt.Methods
				.lambdaize(BiFunction.class, functionHandle);
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
		MethodType methodTypeFunction = MethodType.methodType(void.class, long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class, "inplace", methodTypeFunction);

		Inplace<long[]> dynamicFunction = (Inplace<long[]>) Adapt.Methods.lambdaize(Inplace.class, functionHandle);
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
	public void testBiInplaceFirst() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(void.class, long[].class, long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class, "biInplaceFirst", methodTypeFunction);

		BiInplaceFirst<long[], long[]> dynamicFunction = (BiInplaceFirst<long[], long[]>) Adapt.Methods
				.lambdaize(BiInplaceFirst.class, functionHandle);
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
	public void testBiInplaceSecond() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodTypeFunction = MethodType.methodType(void.class, long[].class, long[].class);
		MethodHandle functionHandle = lookup.findStatic(LambdaizerTest.class, "biInplaceSecond", methodTypeFunction);

		BiInplaceSecond<long[], long[]> dynamicFunction = (BiInplaceSecond<long[], long[]>) Adapt.Methods
				.lambdaize(BiInplaceSecond.class, functionHandle);
		long[] io = { 1, 2, 3 };
		long[] i2 = { 3, 6, 9 };
		long[] expected = { 4, 8, 12 };
		dynamicFunction.mutate(io, i2);
		for (int i = 0; i < io.length; i++) {
			assert (i2[i] == expected[i]);
		}
	}

}
