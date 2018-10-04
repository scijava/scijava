package org.scijava.ops.core;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that accepts three arguments and produces a result.
 * This is the three-arity specialization of {@link Function}.
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
 * @param <O>
 *            the type of the output of the function
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface TriFunction<I1, I2, I3, O> {

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t
	 *            the first function argument
	 * @param u
	 *            the second function argument
	 * @param v
	 *            the third function argument
	 * @return the function output
	 */
	O apply(I1 t, I2 u, I3 v);

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
	default <O2> TriFunction<I1, I2, I3, O2> andThen(Function<? super O, ? extends O2> after) {
		Objects.requireNonNull(after);
		return (I1 in1, I2 in2, I3 in3) -> after.apply(apply(in1, in2, in3));
	}
}
