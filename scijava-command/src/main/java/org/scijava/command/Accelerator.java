/*
 * #%L
 * Discoverable commands, with the metadata a menu is built from.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.command;

import java.util.Optional;

/**
 * A keyboard shortcut, as {@link Menu#accelerator()} writes it.
 * <p>
 * The notation is ImageJ's, because that is what tens of thousands of existing
 * commands are written in: {@code ^} is the platform's menu shortcut - control,
 * or command on a Mac - {@code !} is alt and {@code +} is shift, followed by
 * the key. So {@code "^+C"} is shift-ctrl-C, or shift-cmd-C.
 * </p>
 * <p>
 * NB: parsing it here rather than in each binding is a lesson from the third
 * one: Swing wants a {@code KeyStroke}, JavaFX a {@code KeyCombination}, and
 * AWT a {@code MenuShortcut} that cannot express alt at all. Those are three
 * ways of <em>saying</em> a shortcut; what the shortcut <em>is</em> was the
 * same each time, and a binding that has to parse the string again is a
 * binding that can disagree about what it means.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Accelerator {

	private final String key;
	private final boolean shortcut;
	private final boolean alt;
	private final boolean shift;

	private Accelerator(final String key, final boolean shortcut,
		final boolean alt, final boolean shift)
	{
		this.key = key;
		this.shortcut = shortcut;
		this.alt = alt;
		this.shift = shift;
	}

	/**
	 * Reads an accelerator.
	 *
	 * @param accelerator the shortcut, e.g. {@code "^O"} or {@code "^+C"}
	 * @return the accelerator, or empty if there is nothing to read
	 */
	public static Optional<Accelerator> parse(final String accelerator) {
		if (accelerator == null || accelerator.isEmpty()) return Optional.empty();
		boolean shortcut = false, alt = false, shift = false;
		int i = 0;
		for (; i < accelerator.length(); i++) {
			final char c = accelerator.charAt(i);
			if (c == '^') shortcut = true;
			else if (c == '!') alt = true;
			else if (c == '+') shift = true;
			else break;
		}
		final String key = accelerator.substring(i);
		if (key.isEmpty()) return Optional.empty();
		return Optional.of(new Accelerator(key, shortcut, alt, shift));
	}

	/** Gets the key itself, without modifiers: {@code "O"}, {@code "F1"}. */
	public String key() {
		return key;
	}

	/** Gets whether the key is one character, as a shortcut usually is. */
	public boolean isCharacter() {
		return key.length() == 1;
	}

	/** Gets whether the platform's menu shortcut - control, or command. */
	public boolean isShortcut() {
		return shortcut;
	}

	/** Gets whether alt is held. */
	public boolean isAlt() {
		return alt;
	}

	/** Gets whether shift is held. */
	public boolean isShift() {
		return shift;
	}

	/** Gets whether any modifier is held. */
	public boolean hasModifiers() {
		return shortcut || alt || shift;
	}

	@Override
	public String toString() {
		return (shortcut ? "^" : "") + (alt ? "!" : "") + (shift ? "+" : "") + key;
	}
}
