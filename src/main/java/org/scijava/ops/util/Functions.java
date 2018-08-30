package org.scijava.ops.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.Op;
import org.scijava.ops.base.OpService;
import org.scijava.types.Nil;

/**
 * Utility providing adaptation between {@link Op} types.
 */
public class Functions {

	private Functions() {
		// NB: Prevent instantiation of utility class.
	}

	public static <I, O> Function<I, O> unary(final OpService ops, final Class<? extends Op> opClass,
			final Nil<I> inputType, final Nil<O> outputType, final Object... secondaryArgs) {
		return ops.findOp( //
				opClass, //
				new Nil<Function<I, O>>() {
				}, //
				new Nil[] { inputType }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

	public static <I1, I2, O> BiFunction<I1, I2, O> binary(final OpService ops, final Class<? extends Op> opClass,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<O> outputType,
			final Object... secondaryArgs) {
		return ops.findOp( //
				opClass, //
				new Nil<BiFunction<I1, I2, O>>() {
				}, //
				new Nil[] { input1Type, input2Type }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

}
