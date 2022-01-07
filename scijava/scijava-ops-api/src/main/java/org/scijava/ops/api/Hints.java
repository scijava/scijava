
package org.scijava.ops.api;

import java.util.Arrays;

/**
 * A basic interface for storing and accessing Hints. The general structure for
 * a Hint is
 * <p>
 * {@code hint = hintType.option}
 * <p>
 * <ul>
 * <li>{@code hintType} designates the category of hint</li>
 * <li>{@code option} designates the preference within the category</li>
 * <li>{@code hint} is the combination of {@code hintType} and {@code option}
 * with a delimiting {@code .}</li>
 * </ul>
 * <p>
 * For example, you might write a {@code hintType} to designate preferences on a
 * tradeoff between performance and loss. That {@code hintType} might be
 * {@code Lossiness}, with options {@code LOSSLESS} and {@code LOSSY}.
 * 
 * @author Gabriel Selzer
 */
public interface Hints {

	/**
	 * Returns a <b>new</b> {@link Hints} with:
	 * <ol>
	 * <li>All hints in this {@link Hints}</li>
	 * <li>All hints in {@code hints}</li>
	 * </ol>
	 *
	 * @param hints the hints to add to this {@link Hints}
	 * @return a <b>new</b> {@link Hints} containing the union of the two sets of
	 *         hints
	 */
	Hints plus(String... hints);

	/**
	 * Returns a <b>new</b> {@link Hints} with <b>only</b> the hints in this
	 * {@link Hints} that are not also in {@code hints}
	 *
	 * @param hints the hints that should not carry over from this {@link Hints}
	 * @return a <b>new</b> {@link Hints} containing the hints in this {@link Hints}
	 *         but <b>not</b> in {@code hints}
	 */
	Hints minus(String... hints);

	/**
	 * Determines whether {@code hint} is in this {@link Hints}
	 * 
	 * @param hint a hint
	 * @return {@code true} iff {@code hint} is in this {@link Hints}
	 */
	boolean contains(String hint);

	/**
	 * Determines whether any hints in {@code hints} are also in this {@link Hints}
	 * 
	 * @param hints an array of hints
	 * @return true iff <b>each</b> hint in {@code hints} is <b>not</b> in this
	 *         {@link Hints}
	 */
	default boolean containsNone(String... hints) {
		return !containsAny(hints);
	}

	/**
	 * Determines whether any hints in {@code hints} are in this {@link Hints}
	 * 
	 * @param hints an array of hints
	 * @return true iff <b>any</b> hint in {@code hints} is in this {@link Hints}
	 */
	default boolean containsAny(String... hints) {
		return Arrays.stream(hints).anyMatch(hint -> contains(hint));
	}

	/**
	 * Determines whether any hints in {@code hints} are in this {@link Hints}
	 * 
	 * @param hints an array of hints
	 * @return true iff <b>each</b> hint in {@code hints} is in this {@link Hints}
	 */
	default boolean containsAll(String... hints) {
		return Arrays.stream(hints).allMatch(hint -> contains(hint));
	}

	/**
	 * Generates a <b>new</b> {@link Hints} with identical hints.
	 * 
	 * @return a <b>new</b> {@link Hints} Object with the same hints as this
	 *         {@link Hints}
	 */
	Hints copy();

}
