package org.scijava.ops.util;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.core.OneToOneCommand;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.Source;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function4;

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
	public static class Functions {
		private Functions() {
		}

		public static <I, O> Computer<I, O> asComputer(final Function<I, O> function, final Computer<O, O> copy) {
			return (in, out) -> {
				final O tmp = function.apply(in);
				copy.accept(tmp, out);
			};
		}

		public static <I1, I2, O> BiComputer<I1, I2, O> asBiComputer(final BiFunction<I1, I2, O> biFunction,
				final Computer<O, O> copy) {
			return (in1, in2, out) -> {
				final O tmp = biFunction.apply(in1, in2);
				copy.accept(tmp, out);
			};
		}

		public static <I, O> Callable<O> asNullaryFunction(final Function<I, O> function, I input) {
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

		public static <I1, I2, I3, O> Function<I1, O> asFunction(final Function3<I1, I2, I3, O> function3, I2 in2,
				I3 in3) {
			return (in1) -> {
				return function3.apply(in1, in2, in3);
			};
		}

		public static <I1, I2, I3, O> BiFunction<I1, I2, O> asBiFunction(final Function3<I1, I2, I3, O> function3,
				I3 in3) {
			return (in1, in2) -> {
				return function3.apply(in1, in2, in3);
			};
		}

	}

	/**
	 * Adapters from Computers to Functions
	 */
	public static class Computers {
		private Computers() {
		}

		public static <I, O> Function<I, O> asFunction(final Computer<I, O> computer, final Source<O> source) {
			return (in) -> {
				O out = source.create();
				computer.compute(in, out);
				return out;
			};
		}

		public static <I, O> Function<I, O> asFunction(final Computer<I, O> computer,
				final Function<I, O> inputAwareSource) {
			return (in) -> {
				O out = inputAwareSource.apply(in);
				computer.compute(in, out);
				return out;
			};
		}

		public static <I, O> Callable<O> asNullaryFunction(final Computer<I, O> computer, final I input,
				final Function<I, O> inputAwareSource) {
			return Functions.asNullaryFunction(asFunction(computer, inputAwareSource), input);
		}

		public static <I1, I2, O> BiFunction<I1, I2, O> asBiFunction(final BiComputer<I1, I2, O> computer,
				final Source<O> source) {
			return (in1, in2) -> {
				O out = source.create();
				computer.compute(in1, in2, out);
				return out;
			};
		}

		public static <I1, I2, O> BiFunction<I1, I2, O> asBiFunction(final BiComputer<I1, I2, O> computer,
				final BiFunction<I1, I2, O> inputAwareSource) {
			return (in1, in2) -> {
				O out = inputAwareSource.apply(in1, in2);
				computer.compute(in1, in2, out);
				return out;
			};
		}

		public static <I1, I2, I3, O> Function3<I1, I2, I3, O> asFunction3(final Computer3<I1, I2, I3, O> computer,
				final Source<O> source) {
			return (in1, in2, in3) -> {
				O out = source.create();
				computer.compute(in1, in2, in3, out);
				return out;
			};
		}

		public static <I1, I2, I3, O> Function3<I1, I2, I3, O> asFunction3(Computer3<I1, I2, I3, O> computer,
				Function3<I1, I2, I3, O> inputAwareSource) {
			return (in1, in2, in3) -> {
				O out = inputAwareSource.apply(in1, in2, in3);
				computer.compute(in1, in2, in3, out);
				return out;
			};
		}

		public static <I1, I2, I3, I4, O> Function4<I1, I2, I3, I4, O> asFunction4(
				final Computer4<I1, I2, I3, I4, O> computer, final Source<O> source) {
			return (in1, in2, in3, in4) -> {
				O out = source.create();
				computer.compute(in1, in2, in3, in4, out);
				return out;
			};
		}

		public static <I1, I2, I3, I4, O> Function4<I1, I2, I3, I4, O> asFunction4(
				Computer4<I1, I2, I3, I4, O> computer, Function4<I1, I2, I3, I4, O> inputAwareSource) {
			return (in1, in2, in3, in4) -> {
				O out = inputAwareSource.apply(in1, in2, in3, in4);
				computer.compute(in1, in2, in3, in4, out);
				return out;
			};
		}

		public static <I, O> OneToOneCommand<I, O> asCommand(final Computer<I, O> computer, I input, O output) {
			OneToOneCommand<I, O> command = new OneToOneCommand<I, O>() {
				@Override
				public void run() {
					computer.compute(input, output);
				}
			};
			// Populate the input and output member of the computer command
			Inject.Commands.all(command, input, output);
			return command;
		}

		public static <I1, I2, O> Computer<I1, O> asComputer(final BiComputer<I1, I2, O> computer, I2 in2) {
			return (in1, out) -> {
				computer.compute(in1, in2, out);
			};
		}

		public static <I1, I2, I3, O> Computer<I1, O> asComputer(final Computer3<I1, I2, I3, O> computer, I2 in2,
				I3 in3) {
			return (in1, out) -> {
				computer.compute(in1, in2, in3, out);
			};
		}

		public static <I1, I2, I3, O> BiComputer<I1, I2, O> asBiComputer(final Computer3<I1, I2, I3, O> computer,
				I3 in3) {
			return (in1, in2, out) -> {
				computer.compute(in1, in2, in3, out);
			};
		}

	}

}
