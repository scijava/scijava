/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package org.scijava.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.command.CommandInfo;
import org.scijava.ops.OpCandidate.StatusCode;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.service.Service;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;

/**
 * Utility methods for working with ops. In particular, this class contains
 * handy methods for generating human-readable strings describing ops and match
 * requests against them.
 * 
 * @author Curtis Rueden
 */
public final class OpUtils {

	private OpUtils() {
		// NB: prevent instantiation of utility class.
	}

	// -- Utility methods --

	public static Object[] args(final Object[] latter, final Object... former) {
		final Object[] result = new Object[former.length + latter.length];
		int i = 0;
		for (final Object o : former) {
			result[i++] = o;
		}
		for (final Object o : latter) {
			result[i++] = o;
		}
		return result;
	}

	/**
	 * Gets the given {@link Struct}'s list of inputs, excluding special ones
	 * like {@link Service}s and {@link Context}s.
	 */
	public static List<Member<?>> inputs(final Struct info) {
		final List<Member<?>> inputs = asList(info.inputs());
		return filter(inputs, input -> !isInjectable(input.getType()));
	}

	/** Gets the given {@link Struct}'s list of outputs. */
	public static List<Member<?>> outputs(final Struct info) {
		return asList(info.outputs());
	}

	/** Gets the namespace portion of the given op name. */
	public static String getNamespace(final String opName) {
		if (opName == null) return null;
		final int dot = opName.lastIndexOf(".");
		return dot < 0 ? null : opName.substring(0, dot);
	}

	/** Gets the simple portion (without namespace) of the given op name. */
	public static String stripNamespace(final String opName) {
		if (opName == null) return null;
		final int dot = opName.lastIndexOf(".");
		return dot < 0 ? opName : opName.substring(dot + 1);
	}

	/**
	 * Gets a string describing the given op request.
	 * 
	 * @param name The op's name.
	 * @param args The op's input arguments.
	 * @return A string describing the op request.
	 */
	public static String opString(final String name, final Object... args) {
		final StringBuilder sb = new StringBuilder();
		sb.append(name + "(\n\t\t");
		boolean first = true;
		for (final Object arg : args) {
			if (first) first = false;
			else sb.append(",\n\t\t");
			if (arg == null) sb.append("null");
			else if (arg instanceof Class) {
				// NB: Class instance used to mark argument type.
				sb.append(((Class<?>) arg).getSimpleName());
			}
			else sb.append(arg.getClass().getSimpleName());
		}
		sb.append(")");
		return sb.toString();
	}

	// -- Helper methods --

	/** Filters a list with the given predicate, concealing boilerplate crap. */
	private static <T> List<T> filter(final List<T> list, final Predicate<T> p) {
		return list.stream().filter(p).collect(Collectors.toList());
	}

	// TODO: Move to Context.
	private static boolean isInjectable(final Class<?> type) {
		return Service.class.isAssignableFrom(type) || //
			Context.class.isAssignableFrom(type);
	}
}
