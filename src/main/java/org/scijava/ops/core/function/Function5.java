package org.scijava.ops.core.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that accepts five arguments and produces a result.
 * This is the five-arity specialization of {@link Function}.
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
 * @param <O>
 *            the type of the output of the function
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface Function5<I1, I2, I3, I4, I5, O> {

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
	 * @return the function output
	 */
	O apply(I1 t, I2 u, I3 v, I4 w, I5 x);

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
	default <O2> Function5<I1, I2, I3, I4, I5, O2> andThen(Function<? super O, ? extends O2> after) {
		Objects.requireNonNull(after);
		return (I1 in1, I2 in2, I3 in3, I4 in4, I5 in5) -> after.apply(apply(in1, in2, in3, in4, in5));
	}
}
