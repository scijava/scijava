package org.scijava.ops.core;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts nine input arguments and returns no
 * result. This is the nine-arity specialization of {@link Consumer}. Unlike most
 * other functional interfaces, {@code Consumer9} is expected to operate via
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
 * @param <B>
 *            the type of the ninth argument to the operation
 * @param <C>
 *            the type of the tenth argument to the operation
 *
 * @see Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface Consumer10<T, U, V, W, X, Y, Z, A, B, C> {

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
	 * @param b
	 *            the ninth input argument
	 * @param c
	 *            the tenth input argument
	 */
	void accept(T t, U u, V v, W w, X x, Y y, Z z, A a, B b, C c);

	/**
	 * Returns a composed {@code Consumer9} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the composed
	 * operation. If performing this operation throws an exception, the
	 * {@code after} operation will not be performed.
	 *
	 * @param after
	 *            the operation to perform after this operation
	 * @return a composed {@code Consumer9} that performs in sequence this
	 *         operation followed by the {@code after} operation
	 * @throws NullPointerException
	 *             if {@code after} is null
	 */
	default Consumer10<T, U, V, W, X, Y, Z, A, B, C> andThen(
			Consumer10<? super T, ? super U, ? super V, ? super W, ? super X, ? super Y, ? super Z, ? super A, ? super B, ? super C> after) {
		Objects.requireNonNull(after);

		return (t, u, v, w, x, y, z, a, b, c) -> {
			accept(t, u, v, w, x, y, z, a, b, c);
			after.accept(t, u, v, w, x, y, z, a, b, c);
		};
	}
}
