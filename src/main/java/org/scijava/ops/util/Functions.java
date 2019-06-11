package org.scijava.ops.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.scijava.ops.OpService;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function4;
import org.scijava.ops.core.function.Function5;
import org.scijava.ops.core.function.Function6;
import org.scijava.ops.core.function.Function7;
import org.scijava.ops.core.function.Function8;
import org.scijava.ops.core.function.Function9;
import org.scijava.ops.types.Nil;
import org.scijava.util.Types;

public class Functions {

	/**
	 * All known function types and their arities. The entries are sorted by
	 * arity, i.e., the {@code i}-th entry has an arity of {@code i}.
	 */
	public static final BiMap<Class<?>, Integer> ALL_FUNCTIONS;

	static {
		final Map<Class<?>, Integer> functions = new HashMap<>(10);
		functions.put(Supplier.class, 0);
		functions.put(Function.class, 1);
		functions.put(BiFunction.class, 2);
		functions.put(Function3.class, 3);
		functions.put(Function4.class, 4);
		functions.put(Function5.class, 5);
		functions.put(Function6.class, 6);
		functions.put(Function7.class, 7);
		functions.put(Function8.class, 8);
		functions.put(Function9.class, 9);
		ALL_FUNCTIONS = ImmutableBiMap.copyOf(functions);
	}

	private Functions() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * @return {@code true} if the given type is a {@link #ALL_FUNCTIONS known}
	 *         function type, {@code false} otherwise.<br>
	 *         Note that only the type itself and not its type hierarchy is
	 *         considered.
	 * @throws NullPointerException If {@code type} is {@code null}.
	 */
	public static boolean isFunction(Type type) {
		return ALL_FUNCTIONS.containsKey(Types.raw(type));
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
				outputType, //
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
				outputType, //
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
				outputType, //
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
				outputType, //
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
				outputType, //
				secondaryArgs);
	}
}
