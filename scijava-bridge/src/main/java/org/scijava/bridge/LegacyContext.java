/*
 * #%L
 * Presenting SciJava Common's modules as SciJava3 commands.
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

package org.scijava.bridge;

import java.util.List;

import org.scijava.command3.CommandInfo;

/**
 * The old world, running alongside the new one.
 * <p>
 * It owns a SciJava Common context and hands out what that context can run, as
 * SciJava3 commands:
 * </p>
 *
 * <pre>
 * try (LegacyContext legacy = LegacyContext.start()) {
 * 	entries.addAll(legacy.commands());
 * }
 * </pre>
 * <p>
 * NB: no SciJava Common type appears in this class's signatures, deliberately.
 * A JPMS module using the bridge would otherwise have to {@code requires
 * org.scijava} merely to name the context it is holding - which is a great
 * deal of coupling for something an application wants to keep at arm's length,
 * and impossible to express for test-scoped use at all. An application that
 * already owns a legacy context, as Fiji will, passes it to
 * {@link Legacy#commands} instead.
 * </p>
 *
 * @author Curtis Rueden
 */
public class LegacyContext implements AutoCloseable {

	private final org.scijava.Context context;

	private LegacyContext(final org.scijava.Context context) {
		this.context = context;
	}

	/** Starts a legacy context with every service it can find. */
	public static LegacyContext start() {
		return new LegacyContext(new org.scijava.Context());
	}

	/**
	 * Wraps a legacy context the caller already has.
	 *
	 * @param context a SciJava Common context; closing this does not dispose it,
	 *          the caller having made it
	 */
	public static LegacyContext of(final Object context) {
		if (!(context instanceof org.scijava.Context)) {
			throw new IllegalArgumentException("Not a SciJava Common context: " + //
				(context == null ? "null" : context.getClass().getName()));
		}
		return new LegacyContext((org.scijava.Context) context) {

			@Override
			public void close() {
				// NB: not ours to dispose.
			}
		};
	}

	/** Gets everything this context can run, as commands, in menu order. */
	public List<CommandInfo> commands() {
		return Legacy.commands(context);
	}

	/**
	 * Gets the SciJava Common context itself.
	 * <p>
	 * Typed as {@link Object} so that naming it costs a caller nothing; cast it
	 * when you mean to use it.
	 * </p>
	 */
	public Object context() {
		return context;
	}

	@Override
	public void close() {
		context.dispose();
	}
}
