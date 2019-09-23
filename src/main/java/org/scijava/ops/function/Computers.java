package org.scijava.ops.function;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.scijava.ops.OpService;
import org.scijava.ops.types.Nil;
import org.scijava.param.Mutable;
import org.scijava.util.Types;

/**
 * Container class for computer-style functional interfaces at various
 * <a href="https://en.wikipedia.org/wiki/Arity">arities</a>.
 * <p>
 * A computer has functional method {@code compute} with a number of arguments
 * corresponding to the arity, plus an additional argument for the preallocated
 * output to be populated by the computation.
 * </p>
 * <p>
 * Each computer interface implements a corresponding {@link Consumer}-style
 * interface (see {@link Consumers}) with arity+1; the consumer's {@code accept}
 * method simply delegates to {@code compute}. This pattern allows computer ops
 * to be used directly as consumers as needed.
 * </p>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class Computers {

	private Computers() {
		// NB: Prevent instantiation of utility class.
	}

	// -- BEGIN TEMP --

	/**
	 * All known computer types and their arities. The entries are sorted by
	 * arity, i.e., the {@code i}-th entry has an arity of {@code i}.
	 */
	public static final BiMap<Class<?>, Integer> ALL_COMPUTERS;

	static {
		final Map<Class<?>, Integer> computers = new HashMap<>();
		computers.put(Arity0.class, 0);
		computers.put(Arity1.class, 1);
		computers.put(Arity2.class, 2);
		computers.put(Arity3.class, 3);
		ALL_COMPUTERS = ImmutableBiMap.copyOf(computers);
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

	public static <O> Arity0<O> match(final OpService ops, final String opName,
		final Nil<O> outType)
	{
		final Nil<Arity0<O>> specialType = new Nil<Arity0<O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Arity0.class, //
					new Type[] { outType.getType() });
			}
		};
		return ops.findOp( //
			opName, //
			specialType, //
			new Nil<?>[] { outType }, //
			outType);
	}

	public static <I, O> Arity1<I, O> match(final OpService ops,
		final String opName, final Nil<I> inType, final Nil<O> outType)
	{
		final Nil<Arity1<I, O>> specialType = new Nil<Arity1<I, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Arity1.class, //
					new Type[] { inType.getType(), outType.getType() });
			}
		};
		return ops.findOp( //
			opName, //
			specialType, //
			new Nil<?>[] { inType, outType }, //
			outType);
	}

	public static <I1, I2, O> Arity2<I1, I2, O> match(final OpService ops, final String opName,
			final Nil<I1> in1Type, final Nil<I2> in2Type, final Nil<O> outType) {
		final Nil<Arity2<I1, I2, O>> specialType = new Nil<Arity2<I1, I2, O>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Arity2.class, //
						new Type[] { in1Type.getType(), in2Type.getType(), outType.getType() });
			}
		};
		return ops.findOp( //
			opName, //
			specialType, //
			new Nil<?>[] { in1Type, in2Type, outType }, //
			outType);
	}

	public static <I1, I2, I3, O> Arity3<I1, I2, I3, O> match(
		final OpService ops, final String opName, final Nil<I1> in1Type,
		final Nil<I2> in2Type, final Nil<I3> in3Type, final Nil<O> outType)
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
			new Nil<?>[] { in1Type, in2Type, in3Type, outType }, //
			outType);
	}

	// -- END TEMP --

	@FunctionalInterface
	public interface Arity0<O> extends Consumer<O> {
		void compute(@Mutable O out);

		@Override
		default void accept(O out) {
			compute(out);
		}
	}

	@FunctionalInterface
	public interface Arity1<I, O> extends BiConsumer<I, O> {
		void compute(I in, @Mutable O out);

		@Override
		default void accept(I in, O out) {
			compute(in, out);
		}
	}

	@FunctionalInterface
	public interface Arity2<I1, I2, O> extends Consumers.Arity3<I1, I2, O> {
		void compute(I1 in1, I2 in2, @Mutable O out);

		@Override
		default void accept(I1 in1, I2 in2, O out) {
			compute(in1, in2, out);
		}
	}

	@FunctionalInterface
	public interface Arity3<I1, I2, I3, O> extends Consumers.Arity4<I1, I2, I3, O> {
		void compute(I1 in1, I2 in2, I3 in3, @Mutable O out);

		@Override
		default void accept(I1 in1, I2 in2, I3 in3, O out) {
			compute(in1, in2, in3, out);
		}
	}
}
