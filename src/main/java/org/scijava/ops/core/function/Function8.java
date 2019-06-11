package org.scijava.ops.core.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that accepts eight arguments and produces a result.
 * This is the eight-arity specialization of {@link Function}.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose
 * functional method is {@link #apply(Object, Object)}.
 *
 * @param <I1>
 *            the type of the first argument to the function
 * @param <I2>
 *            the type of the second argument to the function
 * @param <I3>
 *            the type of the third argument to the function
 * @param <I4>
 *            the type of the fourth argument to the function
 * @param <I5>
 *            the type of the fifth argument to the function
 * @param <I6>
 *            the type of the sixth argument to the function
 * @param <I7>
 *            the type of the seventh argument to the function
 * @param <I8>
 *            the type of the eighth argument to the function
 * @param <O>
 *            the type of the output of the function
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface Function8<I1, I2, I3, I4, I5, I6, I7, I8, O> {

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t
	 *            the first function argument
	 * @param u
	 *            the second function argument
	 * @param v
	 *            the third function argument
	 * @param w
	 *            the fourth function argument
	 * @param x
	 *            the fifth function argument
	 * @param y
	 *            the sixth function argument
	 * @param z
	 *            the seventh function argument
	 * @param a
	 *            the eighth function argument
	 * @return the function output
	 */
	O apply(I1 t, I2 u, I3 v, I4 w, I5 x, I6 y, I7 z, I8 a);

	/**
	 * Returns a composed function that first applies this function to its input,
	 * and then applies the {@code after} function to the result. If evaluation of
	 * either function throws an exception, it is relayed to the caller of the
	 * composed function.
	 *
	 * @param <O2>
	 *            the type of output of the {@code after} function, and of the
	 *            composed function
	 * @param after
	 *            the function to apply after this function is applied
	 * @return a composed function that first applies this function and then applies
	 *         the {@code after} function
	 * @throws NullPointerException
	 *             if after is null
	 */
	default <O2> Function8<I1, I2, I3, I4, I5, I6, I7, I8, O2> andThen(Function<? super O, ? extends O2> after) {
		Objects.requireNonNull(after);
		return (I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8) -> after.apply(apply(in1, in2, in3, in4, in5, in6, in7, in8));
	}
}
