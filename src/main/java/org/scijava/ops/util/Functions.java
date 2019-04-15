package org.scijava.ops.util;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function4;
import org.scijava.ops.core.function.Function5;
import org.scijava.types.Nil;
import org.scijava.util.Types;

/**
 * Utility providing adaptation between {@link Op} types.
 */
public class Functions {

	private Functions() {
		// NB: Prevent instantiation of utility class.
	}

	public static <I, O> Function<I, O> unary(final OpService ops, final String opName, final Nil<I> inputType,
			final Nil<O> outputType, final Object... secondaryArgs) {

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

	public static <I1, I2, I3, O> Function3<I1, I2, I3, O> ternary(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type, final Nil<O> outputType,
			final Object... secondaryArgs) {

		Nil<Function3<I1, I2, I3, O>> functionNil = new Nil<Function3<I1, I2, I3, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Function3.class, new Type[] { input1Type.getType(), input2Type.getType(),
						input3Type.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				functionNil, //
				new Nil[] { input1Type, input2Type, input3Type }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

	public static <I1, I2, I3, I4, O> Function4<I1, I2, I3, I4, O> quaternary(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<O> outputType, final Object... secondaryArgs) {

		Nil<Function4<I1, I2, I3, I4, O>> functionNil = new Nil<Function4<I1, I2, I3, I4, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Function4.class, new Type[] { input1Type.getType(), input2Type.getType(),
						input3Type.getType(), input4Type.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				functionNil, //
				new Nil[] { input1Type, input2Type, input3Type, input4Type }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}
	
	public static <I1, I2, I3, I4, I5, O> Function5<I1, I2, I3, I4, I5, O> quinary(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<I5> input5Type, final Nil<O> outputType, final Object... secondaryArgs) {

		Nil<Function5<I1, I2, I3, I4, I5, O>> functionNil = new Nil<Function5<I1, I2, I3, I4, I5, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Function5.class, new Type[] { input1Type.getType(), input2Type.getType(),
						input3Type.getType(), input4Type.getType(), input5Type.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				functionNil, //
				new Nil[] { input1Type, input2Type, input3Type, input4Type, input5Type }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}
}
