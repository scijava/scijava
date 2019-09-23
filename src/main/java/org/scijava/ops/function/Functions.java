package org.scijava.ops.function;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.types.Nil;
import org.scijava.util.Types;

/**
 * Container class for
 * higher-<a href="https://en.wikipedia.org/wiki/Arity">arity</a>
 * {@link Function}-style functional interfaces&mdash;i.e. with functional
 * method {@code apply} with a number of arguments corresponding to the arity.
 * <ul>
 * <li>For 0-arity (nullary) functions, use {@link Producer} (and notice the
 * functional method there is named {@link Producer#create()}).</li>
 * <li>For 1-arity (unary) functions, use {@link Function}.</li>
 * <li>For 2-arity (binary) functions, use {@link BiFunction}.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class Functions {
	
	private Functions() {
		// NB: Prevent instantiation of utility class.
	}

	// -- BEGIN TEMP --

	/**
	 * All known function types and their arities. The entries are sorted by
	 * arity, i.e., the {@code i}-th entry has an arity of {@code i}.
	 */
	public static final BiMap<Class<?>, Integer> ALL_FUNCTIONS;

	static {
		final Map<Class<?>, Integer> functions = new HashMap<>(10);
		functions.put(Producer.class, 0);
		functions.put(Function.class, 1);
		functions.put(BiFunction.class, 2);
		functions.put(Arity3.class, 3);
		ALL_FUNCTIONS = ImmutableBiMap.copyOf(functions);
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

	public static <I, O> Function<I, O> match(final OpService ops,
		final String opName, final Nil<I> inType, final Nil<O> outType)
	{
		final Nil<Function<I, O>> specialType = new Nil<Function<I, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Function.class, //
					new Type[] { inType.getType(), outType.getType() });
			}
		};
		return ops.findOp( //
			opName, //
			specialType, //
			new Nil[] { inType }, //
			outType);
	}

	public static <I1, I2, O> BiFunction<I1, I2, O> match(final OpService ops,
		final String opName, final Nil<I1> in1Type, final Nil<I2> in2Type,
		final Nil<O> outType)
	{
		final Nil<BiFunction<I1, I2, O>> specialType =
			new Nil<BiFunction<I1, I2, O>>()
		{
			@Override
			public Type getType() {
				return Types.parameterize(BiFunction.class, //
					new Type[] { in1Type.getType(), in2Type.getType(), outType.getType() });
			}
		};
		return ops.findOp( //
			opName, //
			specialType, //
			new Nil[] { in1Type, in2Type }, //
			outType);
	}

	public static <I1, I2, I3, O> Arity3<I1, I2, I3, O> match(final OpService ops,
		final String opName, final Nil<I1> in1Type, final Nil<I2> in2Type,
		final Nil<I3> in3Type, final Nil<O> outType)
	{
		final Nil<Arity3<I1, I2, I3, O>> specialType =
			new Nil<Arity3<I1, I2, I3, O>>()
		{
			@Override
			public Type getType() {
				return Types.parameterize(Arity3.class, //
					new Type[] { in1Type.getType(), in2Type.getType(), in3Type.getType(), outType.getType() });
			}
		};
		return ops.findOp( //
			opName, //
			specialType, //
			new Nil[] { in1Type, in2Type, in3Type }, //
			outType);
	}

	// -- END TEMP --

	/**
	 * A 3-arity specialization of {@link Function}.
	 *
	 * @param <I1> the type of the first argument to the function
	 * @param <I2> the type of the second argument to the function
	 * @param <I3> the type of the third argument to the function
	 * @param <O> the type of the output of the function
	 * @see Function
	 */
	@FunctionalInterface
	public interface Arity3<I1, I2, I3, O> {

		/**
		 * Applies this function to the given arguments.
		 *
		 * @param in1 the first function argument
		 * @param in2 the second function argument
		 * @param in3 the third function argument
		 * @return the function output
		 */
		O apply(I1 in1, I2 in2, I3 in3);

		/**
		 * Returns a composed function that first applies this function to its
		 * input, and then applies the {@code after} function to the result. If
		 * evaluation of either function throws an exception, it is relayed to the
		 * caller of the composed function.
		 *
		 * @param <O2> the type of output of the {@code after} function, and of the
		 *          composed function
		 * @param after the function to apply after this function is applied
		 * @return a composed function that first applies this function and then
		 *         applies the {@code after} function
		 * @throws NullPointerException if after is null
		 */
		default <O2> Arity3<I1, I2, I3, O2> andThen(Function<? super O, ? extends O2> after) {
			Objects.requireNonNull(after);
			return (I1 in1, I2 in2, I3 in3) -> after.apply(apply(in1, in2, in3));
		}
	}
}
