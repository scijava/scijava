package org.scijava.ops.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.Op;
import org.scijava.ops.base.OpService;
import org.scijava.ops.types.Nil;

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

		// Parameterize special type corresponding to this method with in/out types. Then we do not have to specify them
		// explicitly anymore? Also, outside of this 'Functions' convenience thing, one could just search for the deeply
		// typed (having all type variables bound to a specific type) special type. As we are using Types.satisfies, this 
		// should be type safe. Hence, we could allow to not specify the ins/outs if the special type does not contain
		// type variables.
		// ParameterizedType parameterizedBiFuncType = Types.parameterize(BiFunction.class,
		//	new Type[] { input1Type.getType(), input2Type.getType(), outputType.getType() });

		return ops.findOp( //
				opClass, //
				new Nil<BiFunction<I1, I2, O>>() {
				}, //
				new Nil[] { input1Type, input2Type }, //
				new Nil[] { outputType }, //
				secondaryArgs);
	}

}
