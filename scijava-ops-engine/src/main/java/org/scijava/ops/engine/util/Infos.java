/*
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.OpDependencyMember;
import org.scijava.ops.engine.exceptions.impl.InvalidOpNameException;
import org.scijava.ops.engine.exceptions.impl.MultipleOutputsOpException;
import org.scijava.ops.engine.exceptions.impl.UnnamedOpException;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.types.Nil;
import org.scijava.types.Types;

/**
 * Utility methods for working with {@link OpInfo}s.
 *
 * @author Curtis Rueden
 * @author David Kolb
 * @author Gabriel Selzer
 */
public final class Infos {

	private Infos() {
		// NB: prevent instantiation of utility class.
	}

	// -- Utility methods --

	/**
	 * Parses op names contained in specified String according to the following
	 * format:
	 *
	 * <pre>
	 *  'prefix1'.'prefix2' , 'prefix1'.'prefix3'
	 * </pre>
	 *
	 * E.g. "math.add, math.pow". </br>
	 * The name delimiter is a comma (,). Furthermore, names without prefixes are
	 * added. The above example will result in the following output:
	 *
	 * <pre>
	 *  [math.add, add, math.pow, pow]
	 * </pre>
	 *
	 * @param names the string containing the names to parse
	 * @return an array of parsed Op names
	 */
	public static String[] parseNames(String names) {
		return Arrays.stream(names.split(",")) //
			.map(String::trim) //
			.toArray(String[]::new);
	}

	/**
	 * Asserts common requirements for an {@link OpInfo} to be valid
	 *
	 * @param info the {@link OpInfo} to validate
	 */
	public static void validate(final OpInfo info) {
		final long numOutputs = info.struct().members().stream() //
			.filter(Member::isOutput).count();
		if (numOutputs > 1) {
			throw new MultipleOutputsOpException(info.implementationName());
		}
		if (Objects.isNull(info.names()) || info.names().isEmpty()) {
			throw new UnnamedOpException(info.implementationName());
		}
		for (var name : info.names()) {
			if (!name.contains(".")) {
				throw new InvalidOpNameException(info, name);
			}
		}
	}

	/**
	 * Returns the index of the argument that is both the input and the output.
	 * <b>If there is no such argument (i.e. the Op produces a pure output), -1 is
	 * returned</b>
	 *
	 * @return the index of the mutable argument.
	 */
	public static int IOIndex(final OpInfo info) {
		List<Member<?>> inputs = info.inputs();
		Optional<Member<?>> ioArg = inputs.stream() //
			.filter(m -> m.isInput() && m.isOutput()) //
			.findFirst();
		if (ioArg.isEmpty()) return -1;
		Member<?> ioMember = ioArg.get();
		return inputs.indexOf(ioMember);
	}

	public static boolean hasPureOutput(final OpInfo info) {
		return IOIndex(info) == -1;
	}

	/** Gets the op's dependencies on other ops. */
	public static List<OpDependencyMember<?>> dependencies(OpInfo info) {
		return info.struct().members().stream() //
			.filter(m -> m instanceof OpDependencyMember) //
			.map(m -> (OpDependencyMember<?>) m) //
			.collect(Collectors.toList());
	}

	/**
	 * Forms a verbose description of {@code info}
	 *
	 * @param info an {@link OpInfo}
	 * @return a verbose description of {@code info}
	 */
	public static String describe(final OpInfo info) {
		final StringBuilder sb = new StringBuilder(info.implementationName());
		// Step 2: Inputs
		for (var member : info.inputs()) {
			sb.append("\n\t");
			sb.append("> ").append(member.getKey()) //
				.append(member.isRequired() ? "" : " (optional)") //
				.append(" : ");
			if (member.getIOType() == ItemIO.CONTAINER) {
				sb.append("@CONTAINER ");
			}
			else if (member.getIOType() == ItemIO.MUTABLE) {
				sb.append("@MUTABLE ");
			}

			sb.append(typeString(member.getType(), true)); //
			if (!member.getDescription().isBlank()) {
				sb.append("\n\t\t").append(member.getDescription().replaceAll("\n\\s*",
					"\n\t\t"));
			}
		}
		// Step 3: Output
		Member<?> output = info.output();
		if (output.getIOType() == ItemIO.OUTPUT) {
			sb.append("\n\tReturns : ").append(typeString(output.getType(), true));
		}
		return sb.toString();
	}

	private static String typeString(final Type input, final boolean verbose) {
		var str = input.getTypeName();
		if (verbose) return str;
		if (input instanceof TypeVariable<?>) {
			var bounds = ((TypeVariable<?>) input).getBounds();
			String[] s = new String[bounds.length];
			for (int i = 0; i < s.length; i++) {
				s[i] = typeString(Types.raw(bounds[i]), false);
			}
			return String.join("+", s);
		}
		else if (input instanceof ParameterizedType) {
			var pType = (ParameterizedType) input;
			var raw = typeString(pType.getRawType(), false);
			Type[] args = pType.getActualTypeArguments();
			String[] s = new String[args.length];
			for (int i = 0; i < args.length; i++) {
				s[i] = typeString(args[i], false);
			}
			return raw + "<" + String.join(", ", s) + ">";
		}
		return str.replaceAll(
			"([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*([a-zA-Z_$][a-zA-Z\\d_$]*)", "$2");
	}
}
