/*
 * #%L
 * Running scripts as things that declare their inputs and outputs.
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

package org.scijava.script3;

import java.util.Optional;

import org.scijava.execute.Behavior;

/**
 * An interpreter with its own state, in which a script runs.
 * <p>
 * NB: {@link #function} is the reason this is a session rather than a
 * one-shot {@code eval}. A script's parameters may name callbacks, validators
 * and generators of choices, and those are functions <em>in the script</em>;
 * resolving them means keeping the interpreter that defined them. SciJava
 * Common resolved such names by Java reflection, which is precisely why its
 * scripts never had callbacks.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface ScriptSession extends AutoCloseable {

	/** Sets a variable the script can read. */
	void put(String name, Object value);

	/** Gets a variable the script has set, or null if it has none. */
	Object get(String name);

	/**
	 * Runs the given code in this session.
	 *
	 * @param code the script
	 * @return whatever the script evaluated to, if anything
	 * @throws ScriptException if the script fails
	 */
	Object eval(String code);

	/**
	 * Gets a function the script has defined, so that it can be called.
	 *
	 * @param name the function's name
	 * @return the function, or empty if the script defines no such thing
	 */
	default Optional<Behavior> function(final String name) {
		return Optional.empty();
	}

	@Override
	default void close() {
		// NB: most sessions have nothing to release.
	}
}
