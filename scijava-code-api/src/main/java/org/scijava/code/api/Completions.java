/*-
 * #%L
 * Core API for SciJava code intelligence features.
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

package org.scijava.code.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for building {@link Completion}s, in particular via reflection
 * over Java classes (public fields and methods). These helpers are
 * toolkit-agnostic and shared by completers across languages.
 *
 * @author Curtis Rueden
 */
public final class Completions {

	private Completions() {
		// prevent instantiation of utility class
	}

	/**
	 * Builds completions for the public fields and methods of the given class
	 * whose names start with the given (case-insensitive) prefix.
	 *
	 * @param c the class to introspect
	 * @param prefix prefix filter; empty matches everything
	 * @param staticOnly if true, only {@code static} members are included
	 */
	public static List<Completion> membersOf(final Class<?> c,
		final String prefix, final boolean staticOnly)
	{
		final List<Completion> matches = new ArrayList<>();
		if (c == null) return matches;
		final String lp = prefix == null ? "" : prefix.toLowerCase();

		for (final Field f : c.getFields()) {
			if (staticOnly && !Modifier.isStatic(f.getModifiers())) continue;
			if (!f.getName().toLowerCase().startsWith(lp)) continue;
			matches.add(field(f));
		}
		for (final Method m : c.getMethods()) {
			if (staticOnly && !Modifier.isStatic(m.getModifiers())) continue;
			if (!m.getName().toLowerCase().startsWith(lp)) continue;
			matches.add(method(m));
		}
		return matches;
	}

	/** Builds a {@link Completion} describing a reflected field. */
	public static Completion field(final Field f) {
		return Completion.builder(f.getName()) //
			.kind(Completion.Kind.FIELD) //
			.returnType(typeName(f.getType())) //
			.declaringClass(f.getDeclaringClass().getName()) //
			.summary(typeName(f.getType()) + " " + f.getName()) //
			.build();
	}

	/** Builds a {@link Completion} describing a reflected method. */
	public static Completion method(final Method m) {
		final List<Completion.Parameter> params = new ArrayList<>();
		for (final java.lang.reflect.Parameter p : m.getParameters()) {
			params.add(new Completion.Parameter(p.getName(), typeName(p.getType())));
		}
		return Completion.builder(m.getName()) //
			.kind(Completion.Kind.METHOD) //
			.parameters(params) //
			.returnType(typeName(m.getReturnType())) //
			.declaringClass(m.getDeclaringClass().getName()) //
			.summary(signature(m)) //
			.build();
	}

	/** Renders a human-readable method signature, e.g. {@code String concat(String)}. */
	public static String signature(final Method m) {
		final StringBuilder sb = new StringBuilder();
		sb.append(typeName(m.getReturnType())).append(" ");
		sb.append(m.getName()).append("(");
		final Class<?>[] types = m.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(typeName(types[i]));
		}
		sb.append(")");
		return sb.toString();
	}

	private static String typeName(final Class<?> c) {
		final String n = c.getCanonicalName();
		return n == null ? c.getName() : n;
	}
}
