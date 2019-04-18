package org.scijava.ops.util;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.core.Op;
import org.scijava.ops.types.Nil;
import org.scijava.util.Types;

/**
 * Utility providing adaptation between {@link Op} types.
 */
public class Functions {

	private Functions() {
		// NB: Prevent instantiation of utility class.
	}

	public static <I, O> Function<I, O> unary(final OpService ops, final String opName,
			final Nil<I> inputType, final Nil<O> outputType, final Object... secondaryArgs) {

		Nil<Function<I, O>> functionNil = new Nil<Function<I, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Function.class, new Type[] { inputType.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				functionNil, //
				new Nil[] { inputType }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

	public static <I1, I2, O> BiFunction<I1, I2, O> binary(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<O> outputType,
			final Object... secondaryArgs) {

		Nil<BiFunction<I1, I2, O>> functionNil = new Nil<BiFunction<I1, I2, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(BiFunction.class,
						new Type[] { input1Type.getType(), input2Type.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				functionNil, //
				new Nil[] { input1Type, input2Type }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

}
