
package org.scijava.ops.api;

import java.util.Arrays;

/**
 * A basic interface for storing and accessing Hints. The general structure for
 * a Hint is
 * <p>
 * {@code hint = hintType.option}
 * <p>
 * <ul>
 * <li>{@code hintType} designates the category of hint
 * <li>{@code option} designates the preference within the category
 * <li>{@code hint} is the combination of {@code hintType} and {@code option}
 * with a delimiting {@code .}
 * </ul>
 * <p>
 * For example, you might want a {@code hintType} to designate preferences on
 * lossiness (as a tradeoff for performance). That {@code hintType} might be
 * {@code Lossiness}, with {@option}s {@code LOSSLESS} and {@LOSSY}.
 * 
 * @author Gabriel Selzer
 */
public interface Hints {

	Hints plus(String... hints);

	Hints minus(String... hints);

	boolean contains(String hint);

	default boolean containsNone(String... hints) {
		return !containsAny(hints);
	}

	default boolean containsAny(String... hints) {
		return Arrays.stream(hints).anyMatch(hint -> contains(hint));
	}

	default boolean containsAll(String... hints) {
		return Arrays.stream(hints).allMatch(hint -> contains(hint));
	}

	/**
	 * Generates a new {@link Hints} with identical hints.
	 * 
	 * @return a new {@link Hints} Object with the same hints as this
	 *         {@link Hints}
	 */
	Hints copy();

}
