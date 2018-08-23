package org.scijava.ops;

import java.util.function.BiFunction;
import java.util.function.Function;

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
	}
}
