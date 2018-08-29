package org.scijava.ops.util;

import java.lang.reflect.Type;

import org.scijava.ops.BiComputer;
import org.scijava.ops.Computer;
import org.scijava.ops.Op;
import org.scijava.ops.base.OpService;
import org.scijava.ops.types.Nil;

/**
 * Utility providing adaptation between {@link Op} types.
 */
public class Computers {

	private Computers() {
		// NB: Prevent instantiation of utility class.
	}

	public static <I, O> Computer<I, O> unary(final OpService ops, final Class<? extends Op> opClass,
			final Class<I> inputType, final Class<O> outputType, final Object... secondaryArgs) {
		return ops.findOp( //
				opClass, //
				new Nil<Computer<I, O>>() {
				}, //
				new Type[] { inputType, outputType }, //
				outputType, secondaryArgs);
	}

	public static <I1, I2, O> BiComputer<I1, I2, O> binary(final OpService ops, final Class<? extends Op> opClass,
			final Class<I1> input1Type, final Class<I2> input2Type, final Class<O> outputType,
			final Object... secondaryArgs) {
		return ops.findOp( //
				opClass, //
				new Nil<BiComputer<I1, I2, O>>() {
				}, //
				new Type[] { input1Type, input2Type, outputType }, //
				outputType, secondaryArgs);
	}

}
