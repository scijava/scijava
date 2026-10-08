/*-
 * #%L
 * Language Server Protocol bridge for SciJava code intelligence.
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

package org.scijava.code.lsp.jvm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;

/**
 * Completions for the members of Java classes, by reflection: for in-process
 * language servers of JVM languages (e.g. Groovy, Jython).
 *
 * @author Gabriel Selzer
 */
public final class Members {

	private Members() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Completions for the public fields and methods of a class whose names
	 * start with a (case-insensitive) prefix; methods as {@link Callable}s.
	 *
	 * @param c The class.
	 * @param prefix The prefix; empty matches everything.
	 * @param staticOnly Whether to include only static members.
	 */
	public static List<CompletionItem> of(final Class<?> c, final String prefix,
		final boolean staticOnly)
	{
		final List<CompletionItem> out = new ArrayList<>();
		if (c == null) return out;
		final String lp = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
		for (final Field f : c.getFields()) {
			if (staticOnly && !Modifier.isStatic(f.getModifiers())) continue;
			if (!f.getName().toLowerCase(Locale.ROOT).startsWith(lp)) continue;
			out.add(field(f));
		}
		for (final Method m : c.getMethods()) {
			if (staticOnly && !Modifier.isStatic(m.getModifiers())) continue;
			if (!m.getName().toLowerCase(Locale.ROOT).startsWith(lp)) continue;
			out.add(method(m).toItem());
		}
		return out;
	}

	/** The public methods of a class with exactly the given name. */
	public static List<Callable> methods(final Class<?> c, final String name,
		final boolean staticOnly)
	{
		final List<Callable> out = new ArrayList<>();
		if (c == null) return out;
		for (final Method m : c.getMethods()) {
			if (staticOnly && !Modifier.isStatic(m.getModifiers())) continue;
			if (m.getName().equals(name)) out.add(method(m));
		}
		return out;
	}

	/** The public constructors of a class. */
	public static List<Callable> constructors(final Class<?> c) {
		final List<Callable> out = new ArrayList<>();
		if (c == null) return out;
		for (final Constructor<?> ctor : c.getConstructors()) {
			final List<Callable.Param> params = new ArrayList<>();
			for (final java.lang.reflect.Parameter p : ctor.getParameters()) {
				params.add(new Callable.Param(p.getName(), typeName(p.getType())));
			}
			out.add(new Callable(c.getSimpleName(), params, typeName(c)).kind(
				CompletionItemKind.Constructor));
		}
		return out;
	}

	/** A completion for a field. */
	public static CompletionItem field(final Field f) {
		final CompletionItem item = new CompletionItem(f.getName());
		item.setKind(CompletionItemKind.Field);
		item.setInsertText(f.getName());
		item.setDetail(typeName(f.getType()) + " " + f.getName());
		final CompletionItemLabelDetails details = new CompletionItemLabelDetails();
		details.setDescription(typeName(f.getType()));
		item.setLabelDetails(details);
		return item;
	}

	/** A method, as a callable. */
	public static Callable method(final Method m) {
		final List<Callable.Param> params = new ArrayList<>();
		for (final java.lang.reflect.Parameter p : m.getParameters()) {
			params.add(new Callable.Param(p.getName(), typeName(p.getType())));
		}
		return new Callable(m.getName(), params, typeName(m.getReturnType()))
			.detail(signature(m));
	}

	/** A readable method signature, e.g. {@code String concat(String)}. */
	public static String signature(final Method m) {
		final StringBuilder sb = new StringBuilder();
		sb.append(typeName(m.getReturnType())).append(' ').append(m.getName())
			.append('(');
		final Class<?>[] types = m.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(typeName(types[i]));
		}
		return sb.append(')').toString();
	}

	/** The name of a type, as code writes it (e.g. {@code int[]}). */
	public static String typeName(final Class<?> c) {
		final String n = c.getCanonicalName();
		return n == null ? c.getName() : n;
	}
}
