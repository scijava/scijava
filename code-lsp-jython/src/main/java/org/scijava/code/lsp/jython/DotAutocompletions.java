/*-
 * #%L
 * Jython language support for SciJava Script Editor.
 * %%
 * Copyright (C) 2020 - 2024 SciJava developers.
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
package org.scijava.code.lsp.jython;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


public interface DotAutocompletions {
	
	static public final DotAutocompletions EMPTY = new EmptyDotAutocompletions();
	
	default public String getClassname() {
		return null;
	}
	
	default public String getSummary() {
		return null;
	}

	public List<CompletionText> get();
	
	default public Stream<CompletionText> getStream() {
		return get().stream();
	}

	static public List<CompletionText> getPublicFieldsAndMethods(final String className) {
		final List<CompletionText> ac = new ArrayList<>();
		if (null != className) {
			try {
				final Class<?> c = Class.forName(className);
				for (final Field f: c.getFields())
					ac.add(new CompletionText(f.getName(), c, f));
				for (final Method m: c.getMethods())
					ac.add(new CompletionText(m.getName() + "()", c, m)); // TODO could do a parameter-driven autocompletion
			} catch (final Exception e) {
				JythonDev.print("Could not load class " + className, e);
			}
		}
		return ac;
	}
	

	/** Collect static fields and static methods from {@code c} into {@code ac}.
	 * 
	 * @param c
	 * @param ac
	 */
	static public void staticFieldsAndStaticMethodsInto(final Class<?> c, final List<CompletionText> ac) {
		for (final Field f: c.getDeclaredFields())
			if (Modifier.isStatic(f.getModifiers()))
				ac.add(new CompletionText(f.getName(), c, f));
		for (final Method m: c.getDeclaredMethods())
			if (Modifier.isStatic(m.getModifiers()))
				ac.add(new CompletionText(m.getName() + "()", c, m));
	}

	/** Collect non-static fields and non-static methods of class {@code c} into {@code ac}.
	 * 
	 * @param c
	 * @param ac
	 */
	static public void fieldsAndMethodsInto(final Class<?> c, final List<CompletionText> ac) {
		for (final Field f: c.getFields())
			if (!Modifier.isStatic(f.getModifiers()))
				ac.add(new CompletionText(f.getName(), c, f));
		for (final Method m: c.getMethods())
			if (!Modifier.isStatic(m.getModifiers()))
				ac.add(new CompletionText(m.getName() + "()", c, m));
	}
	
	static public Stream<CompletionText> staticFieldsAndStaticMethodsStream(final Class<?> c, final boolean staticFields, final boolean staticMethods) {
		return Stream.concat(
					Arrays.stream(c.getDeclaredFields())
					.filter(f -> Modifier.isStatic(f.getModifiers()))
					.map(f -> new CompletionText(f.getName(), c, f)),
					Arrays.stream(c.getDeclaredMethods())
					.filter(m -> Modifier.isStatic(m.getModifiers()))
					.map(m -> new CompletionText(m.getName(), c, m)));
	}
	
	static public Stream<CompletionText> fieldsAndMethodsStream(final Class<?> c, final boolean staticFields, final boolean staticMethods) {
		return Stream.concat(
					Arrays.stream(c.getFields())
					.filter(f -> !Modifier.isStatic(f.getModifiers()))
					.map(f -> new CompletionText(f.getName(), c, f)),
					Arrays.stream(c.getMethods())
					.filter(m -> !Modifier.isStatic(m.getModifiers()))
					.map(m -> new CompletionText(m.getName(), c, m)));
	}

}
