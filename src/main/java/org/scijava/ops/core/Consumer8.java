package org.scijava.ops.core;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts eight input arguments and returns no
 * result. This is the eight-arity specialization of {@link Consumer}. Unlike most
 * other functional interfaces, {@code Consumer8} is expected to operate via
 * side-effects.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose
 * functional method is {@link #accept(Object, Object)}.
 *
 * @param <T>
 *            the type of the first argument to the operation
 * @param <U>
 *            the type of the second argument to the operation
 * @param <V>
 *            the type of the third argument to the operation
 * @param <W>
 *            the type of the fourth argument to the operation
 * @param <X>
 *            the type of the fifth argument to the operation
 * @param <Y>
 *            the type of the sixth argument to the operation
 * @param <Z>
 *            the type of the seventh argument to the operation
 * @param <A>
 *            the type of the eighth argument to the operation
 *
 * @see Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface Consumer8<T, U, V, W, X, Y, Z, A> {

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
	 * @param x
	 *            the fifth input argument
	 * @param y
	 *            the sixth input argument
	 * @param z
	 *            the seventh input argument
	 * @param a
	 *            the eighth input argument
	 */
	void accept(T t, U u, V v, W w, X x, Y y, Z z, A a);

	/**
	 * Returns a composed {@code Consumer8} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the composed
	 * operation. If performing this operation throws an exception, the
	 * {@code after} operation will not be performed.
	 *
	 * @param after
	 *            the operation to perform after this operation
	 * @return a composed {@code Consumer8} that performs in sequence this
	 *         operation followed by the {@code after} operation
	 * @throws NullPointerException
	 *             if {@code after} is null
	 */
	default Consumer8<T, U, V, W, X, Y, Z, A> andThen(
			Consumer8<? super T, ? super U, ? super V, ? super W, ? super X, ? super Y, ? super Z, ? super A> after) {
		Objects.requireNonNull(after);

		return (t, u, v, w, x, y, z, a) -> {
			accept(t, u, v, w, x, y, z, a);
			after.accept(t, u, v, w, x, y, z, a);
		};
	}
}
