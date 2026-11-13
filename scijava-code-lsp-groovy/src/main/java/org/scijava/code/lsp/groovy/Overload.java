/*-
 * #%L
 * Groovy code intelligence for SciJava, via Groovy's own compiler.
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

package org.scijava.code.lsp.groovy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureInformation;
import org.scijava.code.lsp.CompletionItems;

/**
 * One overload of a method or constructor that Groovy code may call: its
 * name, its parameters with their Java types (where known), and its return
 * type. Unlike a completion, it keeps the types: to rate overloads against
 * the arguments typed, and to offer arguments fitting a parameter.
 *
 * @author Gabriel Selzer
 */
final class Overload {

	/** A parameter: its name, and its type (or null if unknown). */
	static final class Param {

		private final String name;
		private final String type;

		Param(final String name, final String type) {
			this.name = name;
			this.type = type;
		}

		String name() {
			return name;
		}

		/** The type (e.g. {@code double} or {@code java.lang.String}), or null. */
		String type() {
			return type;
		}

		/** E.g. {@code double a}, or {@code a} if the type is unknown. */
		@Override
		public String toString() {
			return type == null ? name : type + " " + name;
		}
	}

	private final String name;
	private final List<Param> params;
	private final String returnType;
	private String detail;

	/**
	 * @param name The name (as called, e.g. {@code max}).
	 * @param params Its parameters.
	 * @param returnType Its return type, or null if unknown (or none).
	 */
	Overload(final String name, final List<Param> params,
		final String returnType)
	{
		this.name = name;
		this.params = Collections.unmodifiableList(new ArrayList<>(params));
		this.returnType = returnType;
	}

	/** The public methods of a class with exactly the given name. */
	static List<Overload> methods(final Class<?> c, final String name,
		final boolean staticOnly)
	{
		final List<Overload> out = new ArrayList<>();
		if (c == null) return out;
		for (final Method m : c.getMethods()) {
			if (staticOnly && !Modifier.isStatic(m.getModifiers())) continue;
			if (!m.getName().equals(name)) continue;
			out.add(new Overload(m.getName(), params(m.getParameters()),
				CompletionItems.typeName(m.getReturnType())));
		}
		return out;
	}

	/** The public constructors of a class. */
	static List<Overload> constructors(final Class<?> c) {
		final List<Overload> out = new ArrayList<>();
		if (c == null) return out;
		for (final Constructor<?> ctor : c.getConstructors()) {
			out.add(new Overload(c.getSimpleName(), params(ctor.getParameters()),
				CompletionItems.typeName(c)));
		}
		return out;
	}

	/** Sets a one-line summary, as a completion shows it. */
	Overload detail(final String d) {
		detail = d;
		return this;
	}

	String name() {
		return name;
	}

	List<Param> params() {
		return params;
	}

	/** The parameter types, e.g. for sorting overloads. */
	String parameterTypes() {
		final StringBuilder sb = new StringBuilder();
		for (final Param p : params) {
			if (sb.length() > 0) sb.append(',');
			sb.append(p.type());
		}
		return sb.toString();
	}

	/**
	 * As a completion (see
	 * {@link CompletionItems#method(String, List, String)}).
	 */
	CompletionItem toItem() {
		final List<String> names = new ArrayList<>();
		for (final Param p : params) names.add(p.name());
		final CompletionItem item = CompletionItems.method(name, names,
			parameterList());
		item.setDetail(detail);
		return item;
	}

	/** As a signature, e.g. {@code max(double a, double b) -> double}. */
	SignatureInformation toSignature() {
		final List<ParameterInformation> infos = new ArrayList<>();
		for (final Param p : params) infos.add(new ParameterInformation(p
			.toString()));
		final String label = name + parameterList() + (returnType == null ? ""
			: " -> " + returnType);
		final SignatureInformation s = new SignatureInformation(label);
		s.setParameters(infos);
		return s;
	}

	// -- Helper methods --

	/** E.g. {@code (double a, double b)}. */
	private String parameterList() {
		final StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < params.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(params.get(i));
		}
		return sb.append(')').toString();
	}

	private static List<Param> params(
		final java.lang.reflect.Parameter[] parameters)
	{
		final List<Param> out = new ArrayList<>();
		for (final java.lang.reflect.Parameter p : parameters) {
			out.add(new Param(p.getName(), CompletionItems.typeName(p.getType())));
		}
		return out;
	}
}
