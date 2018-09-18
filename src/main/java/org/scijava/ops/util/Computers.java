package org.scijava.ops.util;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.scijava.ops.BiComputer;
import org.scijava.ops.Computer;
import org.scijava.ops.Op;
import org.scijava.ops.base.OpService;
import org.scijava.ops.types.Nil;
import org.scijava.util.Types;

/**
 * Utility providing adaptation between {@link Op} types.
 */
public class Computers {

	private Computers() {
		// NB: Prevent instantiation of utility class.
	}

	public static <I, O> Computer<I, O> unary(final OpService ops, final Class<? extends Op> opClass,
			final Nil<I> inputType, final Nil<O> outputType, final Object... secondaryArgs) {

		Nil<Computer<I, O>> computerNil = new Nil<Computer<I, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Computer.class, new Type[] { inputType.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opClass, //
				computerNil, //
				new Nil[] { inputType, outputType }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

	public static <I1, I2, O> BiComputer<I1, I2, O> binary(final OpService ops, final Class<? extends Op> opClass,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<O> outputType,
			final Object... secondaryArgs) {

		Nil<BiComputer<I1, I2, O>> computerNil = new Nil<BiComputer<I1, I2, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(BiComputer.class,
						new Type[] { input1Type.getType(), input2Type.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opClass, //
				computerNil, //
				new Nil[] { input1Type, input2Type, outputType }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

}
