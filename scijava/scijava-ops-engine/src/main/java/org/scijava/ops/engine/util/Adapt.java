package org.scijava.ops.engine.util;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.function.Producer;
import org.scijava.ops.api.Op;

/**
 * Utility providing adaptation between {@link Op} types.
 */
public class Adapt {

	private Adapt() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Adapters from Functions to Computers
	 */
	public static class FunctionAdapt {
		private FunctionAdapt() {}

		public static <I, O> Computers.Arity1<I, O> asComputer(final Function<I, O> function, final Computers.Arity1<O, O> copy) {
			return (in, out) -> {
				final O tmp = function.apply(in);
				copy.accept(tmp, out);
			};
		}

		public static <I1, I2, O> Computers.Arity2<I1, I2, O> asComputer2(
			final BiFunction<I1, I2, O> biFunction, final Computers.Arity1<O, O> copy)
		{
			return (in1, in2, out) -> {
				final O tmp = biFunction.apply(in1, in2);
				copy.accept(tmp, out);
			};
		}

		public static <I, O> Callable<O> asCallable(final Function<I, O> function, I input) {
			return () -> function.apply(input);
		}

		public static <I1, I2, O> Callable<O> asNullaryFunction(final BiFunction<I1, I2, O> function, I1 input1,
				I2 input2) {
			return () -> function.apply(input1, input2);
		}

		/**
		 * Restricts the second parameter of a {@link BiFunction} to turn it into a
		 * {@link Function}.
		 * 
		 * @param biFunction
		 *            - a two-arity Function
		 * @param in2
		 *            - a input of type I2 that should always be passed through to the
		 *            {@link BiFunction} as the second argument.
		 * @return {@link Function} - the {@link BiFunction} with the second parameter
		 *         restricted to in2.
		 */
		public static <I1, I2, O> Function<I1, O> asFunction(final BiFunction<I1, I2, O> biFunction, I2 in2) {
			return (in1) -> {
				return biFunction.apply(in1, in2);
			};
		}

		public static <I1, I2, I3, O> Function<I1, O> asFunction(final Functions.Arity3<I1, I2, I3, O> function3, I2 in2,
				I3 in3) {
			return (in1) -> {
				return function3.apply(in1, in2, in3);
			};
		}

		public static <I1, I2, I3, O> BiFunction<I1, I2, O> asBiFunction(final Functions.Arity3<I1, I2, I3, O> function3,
				I3 in3) {
			return (in1, in2) -> {
				return function3.apply(in1, in2, in3);
			};
		}
	}

	/**
	 * Adapters from Computers to Functions
	 */
	public static class ComputerAdapt {
		private ComputerAdapt() {}
		
		public static <O> Producer<O> asFunction(final Computers.Arity0<O> computer,
				final Producer<O> inputAwareSource) {
			return () -> {
				O out = inputAwareSource.get();
				computer.compute(out);
				return out;
			};
		}

		public static <I, O> Function<I, O> asFunction(final Computers.Arity1<I, O> computer,
				final Function<I, O> inputAwareSource) {
			return (in) -> {
				O out = inputAwareSource.apply(in);
				computer.compute(in, out);
				return out;
			};
		}

		public static <I, O> Callable<O> asNullaryFunction(final Computers.Arity1<I, O> computer, final I input,
				final Function<I, O> inputAwareSource) {
			return FunctionAdapt.asCallable(asFunction(computer, inputAwareSource), input);
		}

		public static <I1, I2, O> BiFunction<I1, I2, O> asBiFunction(final Computers.Arity2<I1, I2, O> computer,
				final Function<I1, O> inputAwareSource) {
			return (in1, in2) -> {
				O out = inputAwareSource.apply(in1);
				computer.compute(in1, in2, out);
				return out;
			};
		}

		public static <I1, I2, I3, O> Functions.Arity3<I1, I2, I3, O> asFunction3(Computers.Arity3<I1, I2, I3, O> computer,
				Function<I1, O> inputAwareSource) {
			return (in1, in2, in3) -> {
				O out = inputAwareSource.apply(in1);
				computer.compute(in1, in2, in3, out);
				return out;
			};
		}

		public static <I1, I2, O> Computers.Arity1<I1, O> asComputer(final Computers.Arity2<I1, I2, O> computer, I2 in2) {
			return (in1, out) -> {
				computer.compute(in1, in2, out);
			};
		}

		public static <I1, I2, I3, O> Computers.Arity1<I1, O> asComputer(final Computers.Arity3<I1, I2, I3, O> computer, I2 in2,
				I3 in3) {
			return (in1, out) -> {
				computer.compute(in1, in2, in3, out);
			};
		}

		public static <I1, I2, I3, O> Computers.Arity2<I1, I2, O> asComputer2(final Computers.Arity3<I1, I2, I3, O> computer,
				I3 in3) {
			return (in1, in2, out) -> {
				computer.compute(in1, in2, in3, out);
			};
		}

	}

	// CTR FIXME: These are wrong. It needs to make a copy first!
	public static class InplaceAdapt {

		public static <IO, I2> Inplaces.Arity1<IO> asInplace(Inplaces.Arity2_1<IO, I2> inplace, I2 in2) {
			return (io) -> {
				inplace.mutate(io, in2);
			};
		}

		public static <I1, IO> Inplaces.Arity1<IO> asInplace(Inplaces.Arity2_2<I1, IO> inplace, I1 in1) {
			return (io) -> {
				inplace.mutate(in1, io);
			};
		}

		public static <IO> Function<IO, IO> asFunction(Inplaces.Arity1<IO> inplace) {
			return (io) -> {
				inplace.mutate(io);
				return io;
			};
		}

		public static <IO, I2> BiFunction<IO, I2, IO> asBiFunction(Inplaces.Arity2_1<IO, I2> inplace) {
			return (io, in2) -> {
				inplace.mutate(io, in2);
				return io;
			};
		}

		public static <I1, IO> BiFunction<I1, IO, IO> asBiFunction(Inplaces.Arity2_2<I1, IO> inplace) {
			return (in1, io) -> {
				inplace.mutate(in1, io);
				return io;
			};
		}

		public static <IO, I2, I3> Functions.Arity3<IO, I2, I3, IO> asFunction3(Inplaces.Arity3_1<IO, I2, I3> inplace) {
			return (io, in2, in3) -> {
				inplace.mutate(io, in2, in3);
				return io;
			};
		}

		public static <I1, IO, I3> Functions.Arity3<I1, IO, I3, IO> asFunction3(Inplaces.Arity3_2<I1, IO, I3> inplace) {
			return (in1, io, in3) -> {
				inplace.mutate(in1, io, in3);
				return io;
			};
		}

		public static <I1, I2, IO> Functions.Arity3<I1, I2, IO, IO> asFunction3(Inplaces.Arity3_3<I1, I2, IO> inplace) {
			return (in1, in2, io) -> {
				inplace.mutate(in1, in2, io);
				return io;
			};
		}
	}

	public static class Methods {

		public static <T> T lambdaize(Class<T> functionalInterface, MethodHandle methodHandle) throws Throwable {
			MethodHandles.Lookup caller = MethodHandles.lookup();

			// determine the method name used by the functionalInterface (e.g. for
			// Consumer this name is "accept").
			String[] invokedNames = Arrays.stream(functionalInterface.getDeclaredMethods())
					.filter(method -> Modifier.isAbstract(method.getModifiers())).map(method -> method.getName())
					.toArray(String[]::new);
			if (invokedNames.length != 1)
				throw new IllegalArgumentException("The passed class is not a functional interface");
			
			// see the LambdaMetafactory javadocs for explanations on these MethodTypes.
			MethodType invokedType = MethodType.methodType(functionalInterface);
			MethodType methodType = methodHandle.type();
			Class<?> rType = methodType.returnType();
			MethodType samMethodType = methodType.generic()
					.changeReturnType(rType == void.class ? rType : Object.class);
			MethodHandle callSite = LambdaMetafactory.metafactory(caller, invokedNames[0], //
					invokedType, samMethodType, methodHandle, methodType).getTarget();
			return (T) callSite.invoke();
		}
	}

}
