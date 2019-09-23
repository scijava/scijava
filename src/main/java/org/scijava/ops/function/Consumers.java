package org.scijava.ops.function;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Container class for
 * higher-<a href="https://en.wikipedia.org/wiki/Arity">arity</a>
 * {@link Consumer}-style functional interfaces&mdash;i.e. with functional
 * method {@code accept} with a number of arguments corresponding to the arity.
 * <ul>
 * <li>For 1-arity (unary) consumers, use {@link Consumer}.</li>
 * <li>For 2-arity (binary) consumers, use {@link BiConsumer}.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class Consumers {

	private Consumers() {
		// NB: Prevent instantiation of container class.
	}

	/**
	 * Represents an operation that accepts no input arguments and returns no
	 * result. This is the zero-arity specialization of {@link Consumer}. Unlike
	 * most other functional interfaces, this interface is expected to operate via
	 * side-effects.
	 * <p>
	 * This is a functional interface whose functional method is
	 * {@link #accept()}.
	 * </p>
	 *
	 * @see Consumer
	 */
	@FunctionalInterface
	public interface Arity0 extends Runnable {

		/**
		 * Performs the operation.
		 */
		void accept();

		@Override
		default void run() {
			accept();
		}

		/**
		 * Returns a composed {@code Consumer.Arity0} that performs, in sequence,
		 * this operation followed by the {@code after} operation. If performing
		 * either operation throws an exception, it is relayed to the caller of the
		 * composed operation. If performing this operation throws an exception, the
		 * {@code after} operation will not be performed.
		 *
		 * @param after the operation to perform after this operation
		 * @return a composed {@code Consumer.Arity0} that performs in sequence this
		 *         operation followed by the {@code after} operation
		 * @throws NullPointerException if {@code after} is null
		 */
		default Arity0 andThen(Arity0 after) {
			Objects.requireNonNull(after);

			return () -> {
				accept();
				after.accept();
			};
		}
	}

	/**
	 * Represents an operation that accepts three input arguments and returns no
	 * result. This is the three-arity specialization of {@link Consumer}. Unlike
	 * most other functional interfaces, this interface is expected to operate via
	 * side-effects.
	 * <p>
	 * This is a functional interface whose functional method is
	 * {@link #accept(Object, Object, Object)}.
	 * </p>
	 *
	 * @param <T> the type of the first argument to the operation
	 * @param <U> the type of the second argument to the operation
	 * @see Consumer
	 */
	@FunctionalInterface
	public interface Arity3<T, U, V> {

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param t
		 *            the first input argument
		 * @param u
		 *            the second input argument
		 */
		void accept(T t, U u, V v);

		/**
		 * Returns a composed {@code Consumer.Arity3} that performs, in sequence,
		 * this operation followed by the {@code after} operation. If performing
		 * either operation throws an exception, it is relayed to the caller of the
		 * composed operation. If performing this operation throws an exception, the
		 * {@code after} operation will not be performed.
		 *
		 * @param after the operation to perform after this operation
		 * @return a composed {@code Consumer.Arity3} that performs in sequence this
		 *         operation followed by the {@code after} operation
		 * @throws NullPointerException if {@code after} is null
		 */
		default Arity3<T, U, V> andThen(Arity3<? super T, ? super U, ? super V> after) {
			Objects.requireNonNull(after);

			return (t, u, v) -> {
				accept(t, u, v);
				after.accept(t, u, v);
			};
		}
	}

	/**
	 * Represents an operation that accepts four input arguments and returns no
	 * result. This is the four-arity specialization of
	 * {@link java.util.function.Consumer}. Unlike most other functional
	 * interfaces, this interface is expected to operate via side-effects.
	 * <p>
	 * This is a functional interface whose functional method is
	 * {@link #accept(Object, Object, Object, Object)}.
	 * </p>
	 *
	 * @param <T> the type of the first argument to the operation
	 * @param <U> the type of the second argument to the operation
	 * @param <V> the type of the third argument to the operation
	 * @param <W> the type of the fourth argument to the operation
	 * @see Consumer
	 */
	@FunctionalInterface
	public interface Arity4<T, U, V, W> {

		/**
		 * Performs this operation on the given arguments.
		 *
		 * @param t
		 *            the first input argument
		 * @param u
		 *            the second input argument
		 * @param v
		 *            the third input argument
		 * @param w
		 *            the fourth input argument
		 */
		void accept(T t, U u, V v, W w);

		/**
		 * Returns a composed {@code QuadConsumer} that performs, in sequence, this
		 * operation followed by the {@code after} operation. If performing either
		 * operation throws an exception, it is relayed to the caller of the composed
		 * operation. If performing this operation throws an exception, the
		 * {@code after} operation will not be performed.
		 *
		 * @param after
		 *            the operation to perform after this operation
		 * @return a composed {@code QuadConsumer} that performs in sequence this
		 *         operation followed by the {@code after} operation
		 * @throws NullPointerException
		 *             if {@code after} is null
		 */
		default Arity4<T, U, V, W> andThen(Arity4<? super T, ? super U, ? super V, ? super W> after) {
			Objects.requireNonNull(after);

			return (t, u, v, w) -> {
				accept(t, u, v, w);
				after.accept(t, u, v, w);
			};
		}
	}
}
