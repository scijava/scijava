package org.scijava.ops.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.scijava.ops.OpService;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.ops.core.computer.Computer6;
import org.scijava.ops.core.computer.NullaryComputer;
import org.scijava.ops.types.Nil;
import org.scijava.util.Types;

public class Computers {

	/**
	 * All known computer types and their arities. The entries are sorted by
	 * arity, i.e., the {@code i}-th entry has an arity of {@code i}.
	 */
	public static final BiMap<Class<?>, Integer> ALL_COMPUTERS;

	static {
		final Map<Class<?>, Integer> computers = new HashMap<>(7);
		computers.put(NullaryComputer.class, 0);
		computers.put(Computer.class, 1);
		computers.put(BiComputer.class, 2);
		computers.put(Computer3.class, 3);
		computers.put(Computer4.class, 4);
		computers.put(Computer5.class, 5);
		computers.put(Computer6.class, 6);
		ALL_COMPUTERS = ImmutableBiMap.copyOf(computers);
	}

	private Computers() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * @return {@code true} if the given type is a {@link #ALL_COMPUTERS known}
	 *         computer type, {@code false} otherwise. <br>
	 *         Note that only the type itself and not its type hierarchy is
	 *         considered.
	 * @throws NullPointerException If {@code type} is {@code null}.
	 */
	public static boolean isComputer(Type type) {
		return ALL_COMPUTERS.containsKey(Types.raw(type));
	}

	public static <O> NullaryComputer<O> nullary(final OpService ops, final String opName, final Nil<O> outputType) {
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
				outputType);
	}

	public static <I, O> Computer<I, O> unary(final OpService ops, final String opName, final Nil<I> inputType,
			final Nil<O> outputType) {

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
				outputType);
	}

	public static <I1, I2, O> BiComputer<I1, I2, O> binary(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<O> outputType) {

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
				outputType);
	}

	public static <I1, I2, I3, O> Computer3<I1, I2, I3, O> ternary(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type, final Nil<O> outputType) {

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
				outputType);
	}

	public static <I1, I2, I3, I4, O> Computer4<I1, I2, I3, I4, O> quaternary(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type, final Nil<I4> input4Type,
			final Nil<O> outputType) {

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
				outputType);
	}

	public static <I1, I2, I3, I4, I5, O> Computer5<I1, I2, I3, I4, I5, O> quinary(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<I5> input5Type, final Nil<O> outputType) {

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
				outputType);
	}

}
