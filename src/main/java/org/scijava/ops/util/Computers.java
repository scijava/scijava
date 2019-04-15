package org.scijava.ops.util;

import java.lang.reflect.Type;

import org.scijava.ops.OpService;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.ops.core.computer.NullaryComputer;
import org.scijava.ops.types.Nil;
import org.scijava.util.Types;

/**
 * Utility providing adaptation between {@link Op} types.
 */
public class Computers {

	private Computers() {
		// NB: Prevent instantiation of utility class.
	}

	public static <O> NullaryComputer<O> nullary(final OpService ops, final String opName, final Nil<O> outputType,
			final Object... secondaryArgs) {
		Nil<NullaryComputer<O>> computerNil = new Nil<NullaryComputer<O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(NullaryComputer.class, new Type[] { outputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				computerNil, //
				new Nil[] { outputType }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

	public static <I, O> Computer<I, O> unary(final OpService ops, final String opName, final Nil<I> inputType,
			final Nil<O> outputType, final Object... secondaryArgs) {

		Nil<Computer<I, O>> computerNil = new Nil<Computer<I, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Computer.class, new Type[] { inputType.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				computerNil, //
				new Nil[] { inputType, outputType }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

	public static <I1, I2, O> BiComputer<I1, I2, O> binary(final OpService ops, final String opName,
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
				opName, //
				computerNil, //
				new Nil[] { input1Type, input2Type, outputType }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

	public static <I1, I2, I3, O> Computer3<I1, I2, I3, O> ternary(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type, final Nil<O> outputType,
			final Object... secondaryArgs) {

		Nil<Computer3<I1, I2, I3, O>> computerNil = new Nil<Computer3<I1, I2, I3, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Computer3.class, new Type[] { input1Type.getType(), input2Type.getType(),
						input3Type.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				computerNil, //
				new Nil[] { input1Type, input2Type, input3Type, outputType }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

	public static <I1, I2, I3, I4, O> Computer4<I1, I2, I3, I4, O> quaternary(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type, final Nil<I4> input4Type,
			final Nil<O> outputType, final Object... secondaryArgs) {

		Nil<Computer4<I1, I2, I3, I4, O>> computerNil = new Nil<Computer4<I1, I2, I3, I4, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Computer4.class, new Type[] { input1Type.getType(), input2Type.getType(),
						input3Type.getType(), input4Type.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				computerNil, //
				new Nil[] { input1Type, input2Type, input3Type, input4Type, outputType }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

	public static <I1, I2, I3, I4, I5, O> Computer5<I1, I2, I3, I4, I5, O> quinary(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<I5> input5Type, final Nil<O> outputType,
			final Object... secondaryArgs) {

		Nil<Computer5<I1, I2, I3, I4, I5, O>> computerNil = new Nil<Computer5<I1, I2, I3, I4, I5, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Computer5.class, new Type[] { input1Type.getType(), input2Type.getType(),
						input3Type.getType(), input4Type.getType(), input5Type.getType(), outputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				computerNil, //
				new Nil[] { input1Type, input2Type, input3Type, input4Type, input5Type, outputType }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

}
