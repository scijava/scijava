package org.scijava.ops.function;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.scijava.ops.OpService;
import org.scijava.ops.types.Nil;
import org.scijava.param.Mutable;
import org.scijava.util.Types;

/**
 * Container class for inplace-style functional interfaces at various
 * <a href="https://en.wikipedia.org/wiki/Arity">arities</a>.
 * <p>
 * An inplace has functional method {@code mutate} with a number of arguments
 * corresponding to the arity. Any of the arguments annotated
 * with @{@link Mutable} may be mutated during execution. Some interfaces narrow
 * this behavior to only a specific argument; most ops in practice will
 * implement one of these narrowed interfaces. For example,
 * {@link Inplaces.Arity2_1} is a binary inplace op that mutates the first of
 * two arguments&mdash;e.g., an {@code a /= b} division operation would be an
 * {@link Inplaces.Arity2_1}, whereas {@code b = a / b} would be an
 * {@link Inplaces.Arity2_2}.
 * </p>
 * <p>
 * Each inplace interface implements a corresponding {@link Consumer}-style
 * interface (see {@link Consumers}) with same arity; the consumer's
 * {@code accept} method simply delegates to {@code mutate}. This pattern allows
 * inplace ops to be used directly as consumers as needed.
 * </p>
 * <p>
 * Note that there is no nullary (arity 0) inplace interface, because there
 * would be no arguments to mutate; see also {@link Consumers.Arity0},
 * {@link Computers.Arity0} and {@link Producer}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class Inplaces {

	private Inplaces() {
		// NB: Prevent instantiation of container class.
	}

	// -- BEGIN TEMP --

	/**
	 * All known inplace types and their arities and mutable positions. The entries
	 * are sorted by arity and mutable position.
	 */
	public static final Map<Class<?>, InplaceInfo> ALL_INPLACES;

	static {
		final Map<Class<?>, InplaceInfo> inplaces = new LinkedHashMap<>(22);
		inplaces.put(Inplaces.Arity1.class, new InplaceInfo(1, 0));
		inplaces.put(Inplaces.Arity2_1.class, new InplaceInfo(2, 0));
		inplaces.put(Inplaces.Arity2_2.class, new InplaceInfo(2, 1));
		inplaces.put(Inplaces.Arity3_1.class, new InplaceInfo(3, 0));
		inplaces.put(Inplaces.Arity3_2.class, new InplaceInfo(3, 1));
		inplaces.put(Inplaces.Arity3_3.class, new InplaceInfo(3, 2));
		ALL_INPLACES = Collections.unmodifiableMap(inplaces);
	}

	/**
	 * @return {@code true} if the given type is a {@link #ALL_INPLACES known}
	 *         inplace type, {@code false} otherwise. <br>
	 *         Note that only the type itself and not its type hierarchy is
	 *         considered.
	 * @throws NullPointerException
	 *             If {@code type} is {@code null}.
	 */
	public static boolean isInplace(Type type) {
		return ALL_INPLACES.containsKey(Types.raw(type));
	}

	public static List<Class<?>> getInplacesOfArity(final int arity) {
		return Inplaces.ALL_INPLACES.entrySet().stream() //
				.filter(e -> e.getValue().arity() == arity) //
				.map(Entry<Class<?>, InplaceInfo>::getKey) //
				.collect(Collectors.toList());
	}

	public static <IO> Arity1<IO> match(final OpService ops, final String opName, final Nil<IO> inputOutputType) {

		Nil<Arity1<IO>> inplaceNil = new Nil<Arity1<IO>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Arity1.class, new Type[] { inputOutputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType }, //
				inputOutputType);
	}

	public static <IO, I2> Arity2_1<IO, I2> match1(final OpService ops, final String opName,
			final Nil<IO> inputOutputType, final Nil<I2> input2Type) {

		Nil<Arity2_1<IO, I2>> inplaceNil = new Nil<Arity2_1<IO, I2>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Arity2_1.class,
						new Type[] { inputOutputType.getType(), input2Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType, input2Type }, //
				inputOutputType);
	}

	public static <I1, IO> Arity2_2<I1, IO> match2(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<IO> inputOutputType) {

		Nil<Arity2_2<I1, IO>> inplaceNil = new Nil<Arity2_2<I1, IO>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Arity2_2.class,
						new Type[] { input1Type.getType(), inputOutputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, inputOutputType }, //
				inputOutputType);
	}

	public static <IO, I2, I3> Arity3_1<IO, I2, I3> match1(final OpService ops, final String opName,
			final Nil<IO> inputOutputType, final Nil<I2> input2Type, final Nil<I3> input3Type) {

		Nil<Arity3_1<IO, I2, I3>> inplaceNil = new Nil<Arity3_1<IO, I2, I3>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Arity3_1.class,
						new Type[] { inputOutputType.getType(), input2Type.getType(), input3Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType, input2Type, input3Type }, //
				inputOutputType);
	}

	public static <I1, IO, I3> Arity3_2<I1, IO, I3> match2(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<IO> inputOutputType, final Nil<I3> input3Type) {

		Nil<Arity3_2<I1, IO, I3>> inplaceNil = new Nil<Arity3_2<I1, IO, I3>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Arity3_2.class,
						new Type[] { input1Type.getType(), inputOutputType.getType(), input3Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, inputOutputType, input3Type }, //
				inputOutputType);
	}

	public static <I1, I2, IO> Arity3_3<I1, I2, IO> match3(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<IO> inputOutputType) {

		Nil<Arity3_3<I1, I2, IO>> inplaceNil = new Nil<Arity3_3<I1, I2, IO>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Arity3_3.class,
						new Type[] { input1Type.getType(), input2Type.getType(), inputOutputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, inputOutputType }, //
				inputOutputType);
	}

	public static class InplaceInfo {

		private final int arity;
		private final int mutablePosition;

		public InplaceInfo(final int arity, final int mutablePosition) {
			this.arity = arity;
			this.mutablePosition = mutablePosition;
		}

		public int arity() {
			return arity;
		}

		public int mutablePosition() {
			return mutablePosition;
		}
	}

	// -- END TEMP --

	@FunctionalInterface
	public interface Arity1<IO> extends Consumer<IO> {
		void mutate(@Mutable IO io);

		@Override
		default void accept(IO io) {
			mutate(io);
		}
	}

	@FunctionalInterface
	public interface Arity2<IO1, IO2> extends BiConsumer<IO1, IO2> {
		void mutate(@Mutable IO1 io1, @Mutable IO2 io2);

		@Override
		default void accept(IO1 io1, IO2 io2) {
			mutate(io1, io2);
		}
	}

	@FunctionalInterface
	public interface Arity2_1<IO, I2> extends Arity2<IO, I2> {
		@Override
		void mutate(@Mutable IO io, I2 in2);
	}

	@FunctionalInterface
	public interface Arity2_2<I1, IO> extends Arity2<I1, IO> {
		@Override
		void mutate(I1 in1, @Mutable IO io);
	}

	@FunctionalInterface
	public interface Arity3<IO1, IO2, IO3> extends Consumers.Arity3<IO1, IO2, IO3> {
		void mutate(@Mutable IO1 io1, @Mutable IO2 io2, @Mutable IO3 io3);

		@Override
		default void accept(IO1 io1, IO2 io2, IO3 io3) {
			mutate(io1, io2, io3);
		}
	}

	@FunctionalInterface
	public interface Arity3_1<IO, I2, I3> extends Arity3<IO, I2, I3> {
		@Override
		void mutate(@Mutable IO io, I2 in2, I3 in3);
	}

	@FunctionalInterface
	public interface Arity3_2<I1, IO, I3> extends Arity3<I1, IO, I3> {
		@Override
		void mutate(I1 i1, @Mutable IO io, I3 in3);
	}

	@FunctionalInterface
	public interface Arity3_3<I1, I2, IO> extends Arity3<I1, I2, IO> {
		@Override
		void mutate(I1 i1, I2 in2, @Mutable IO io);
	}
}
