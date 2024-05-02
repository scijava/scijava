/*-
 * #%L
 * The public API of SciJava Ops.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
public class Hints {

	// Hints are stored by their hint type (the middle term)
	final Set<String> hints;

	public Hints(final String... startingHints) {
		this(Arrays.asList(startingHints));
	}

	private Hints(final Collection<String> hints) {
		this.hints = new HashSet<>(hints);
	}

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
	public Hints plus(String... hints) {
		Set<String> newHints = new HashSet<>(this.hints);
		newHints.addAll(Arrays.asList(hints));
		return new Hints(newHints);
	}

	/**
	 * Returns a <b>new</b> {@link Hints} with:
	 * <ol>
	 * <li>All hints in this {@link Hints}</li>
	 * <li>All hints in {@code other}</li>
	 * </ol>
	 *
	 * @param other the other {@link Hints} object
	 * @return a <b>new</b> {@link Hints} containing the union of the two sets of
	 *         hints
	 */
	public Hints plus(Hints other) {
		Set<String> newHints = new HashSet<>(this.hints);
		newHints.addAll(other.hints);
		return new Hints(newHints);
	}

	/**
	 * Returns a <b>new</b> {@link Hints} with <b>only</b> the hints in this
	 * {@link Hints} that are not also in {@code hints}
	 *
	 * @param hints the hints that should not carry over from this {@link Hints}
	 * @return a <b>new</b> {@link Hints} containing the hints in this
	 *         {@link Hints} but <b>not</b> in {@code hints}
	 */
	public Hints minus(String... hints) {
		Set<String> newHints = new HashSet<>(this.hints);
		Arrays.asList(hints).forEach(newHints::remove);
		return new Hints(newHints);
	}

	/**
	 * Determines whether {@code hint} is in this {@link Hints}
	 *
	 * @param hint a hint
	 * @return {@code true} iff {@code hint} is in this {@link Hints}
	 */
	public boolean contains(String hint) {
		return hints.contains(hint);
	}

	/**
	 * Determines whether any hints in {@code hints} are also in this
	 * {@link Hints}
	 *
	 * @param hints an array of hints
	 * @return true iff <b>each</b> hint in {@code hints} is <b>not</b> in this
	 *         {@link Hints}
	 */
	public boolean containsNone(String... hints) {
		return !containsAny(hints);
	}

	/**
	 * Determines whether any hints in {@code hints} are in this {@link Hints}
	 *
	 * @param hints an array of hints
	 * @return true iff <b>any</b> hint in {@code hints} is in this {@link Hints}
	 */
	public boolean containsAny(String... hints) {
		return Arrays.stream(hints).anyMatch(this::contains);
	}

	/**
	 * Determines whether any hints in {@code hints} are in this {@link Hints}
	 *
	 * @param hints an array of hints
	 * @return true iff <b>each</b> hint in {@code hints} is in this {@link Hints}
	 */
	public boolean containsAll(String... hints) {
		return Arrays.stream(hints).allMatch(this::contains);
	}

	/**
	 * Generates a <b>new</b> {@link Hints} with identical hints.
	 *
	 * @return a <b>new</b> {@link Hints} Object with the same hints as this
	 *         {@link Hints}
	 */
	public Hints copy() {
		return new Hints(hints);
	}

	@Override
	public int hashCode() {
		return hints.hashCode();
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof Hints)) return false;
		Hints thatHints = (Hints) that;
		return hints.equals(thatHints.hints);
	}
}
