/*
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpDependencyMember;
import org.scijava.ops.engine.exceptions.impl.InvalidOpNameException;
import org.scijava.ops.engine.exceptions.impl.MultipleOutputsOpException;
import org.scijava.ops.engine.exceptions.impl.UnnamedOpException;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
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
		for (var name: info.names()) {
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
	 * Generates a {@link String} describing the given {@link OpInfo}
	 *
	 * @param info the {@link OpInfo} of interest
	 * @return a descriptor for {@code info}
	 */
	public static String describe(final OpInfo info) {
		return describe(info, null, null);
	}

	/**
	 * Generates a {@link String} describing the given {@link OpInfo}
	 *
	 * @param info the {@link OpInfo} of interest
	 * @param name the name that the Op was searched by
	 * @return a descriptor for {@code info}
	 */
	public static String describe(final OpInfo info, final String name) {
		return describe(info, name, null);
	}

	/**
	 * Generates a verbose {@link String} describing the given {@link OpInfo}
	 *
	 * @param info the {@link OpInfo} of interest
	 * @return a descriptor for {@code info}
	 */
	public static String describeVerbose(final OpInfo info) {
			return describeVerbose(info, null, null);
	}

	/**
	 * Generates a verbose {@link String} describing the given {@link OpInfo}
	 *
	 * @param info the {@link OpInfo} of interest
	 * @param name the name that the Op was searched by
	 * @return a descriptor for {@code info}
	 */
	public static String describeVerbose(final OpInfo info, final String name) {
		return describeVerbose(info, name, null);
	}

	/**
	 * Writes a {@link String} describing the {@link OpInfo} of interest
	 * <b>with a particular {@link Member} highlighted</b>.
	 *
	 * @param info the {@link OpInfo} of interest
	 * @param special a {@link Member} to highlight
	 * @return a descriptor for {@code info}
	 */
	public static String describe(final OpInfo info, final Member<?> special) {
		return describe(info, null, special);
	}

	/**
	 * Writes a {@link String} describing the {@link OpInfo} of interest
	 * <b>with a particular {@link Member} highlighted</b>.
	 *
	 * @param info the {@link OpInfo} of interest
	 * @param name the name that the Op was searched by
	 * @param special a {@link Member} to highlight
	 * @return a descriptor for {@code info}
	 */
	public static String describe(final OpInfo info, final String name, final Member<?> special) {
		return description(info, name, special, false);
	}

	/**
	 * Writes a verbose {@link String} describing the {@link OpInfo} of interest
	 * <b>with a particular {@link Member} highlighted</b>.
	 *
	 * @param info the {@link OpInfo} of interest
	 * @param special a {@link Member} to highlight
	 * @return a descriptor for {@code info}
	 */
	public static String describeVerbose(final OpInfo info, final Member<?> special) {
			return describeVerbose(info, null, special);
	}

	/**
	 * Writes a verbose {@link String} describing the {@link OpInfo} of interest
	 * <b>with a particular {@link Member} highlighted</b>.
	 *
	 * @param info the {@link OpInfo} of interest
	 * @param name the name that the Op was searched by
	 * @param special a {@link Member} to highlight
	 * @return a descriptor for {@code info}
	 */
	public static String describeVerbose(final OpInfo info, final String name, final Member<?> special) {
		return description(info, name, special, true);
	}

	/**
	 * Private method to describe {@code info}, optionally highlighting some member {@code special}. Description is verbose iff {@code verbose=true}.
	 *
	 * @param info the {@link OpInfo} to describe
	 * @param special the {@link Member} to highlight
	 * @param verbose iff {@code true}, returns a verbose description
	 * @return a description of {@code info}
	 */
	private static String description(final OpInfo info, final String name, final Member<?> special,
		boolean verbose)
	{
		final StringBuilder sb = new StringBuilder();
		// Step 1: Name
		if (name != null) {
			if (!info.names().contains(name)) {
				throw new IllegalArgumentException("OpInfo " + info.implementationName() + " has no name " + name);
			}
			sb.append(name);
		}
		else {
			sb.append(info.names().get(0));
		}
		sb.append("(\n");
		// Step 2: Inputs
		List<Member<?>> members = info.inputs();
		List<Member<?>> containers = new ArrayList<>();
		if (!members.isEmpty()) {
			sb.append("\t Inputs:\n");
			for (final Member<?> arg : info.inputs()) {
				if (arg.getIOType() == ItemIO.INPUT)
					appendParam(sb, arg, special, verbose);
				else containers.add(arg);
			}
		}
		// Step 3: Output
		if (containers.isEmpty()) {
			sb.append("\t Output:\n");
			appendParam(sb, info.output(), special, verbose);
		}
		else {
			sb.append("\t Container (I/O):\n");
			containers.forEach(c -> appendParam(sb, c, special, verbose));
		}
		sb.append(")\n");
		return sb.toString();
	}

	public static String describeOneLine(final OpInfo info) {
		final StringBuilder sb = new StringBuilder("(");
		// Step 2: Inputs
		var inputs = info.inputs().stream().map(member-> {
			var str = "";
			switch (member.getIOType()) {
				case INPUT:
					str += member.getKey();
					break;
				case MUTABLE:
					str += "^" + member.getKey();
					break;
				case CONTAINER:
					str += "*" + member.getKey();
					break;
				default:
					throw new IllegalArgumentException("Invalid IO type: " + member.getIOType());
			}
			if (!member.isRequired()) {
				str += " = null";
			}
			return str;
		}).collect(Collectors.joining(", "));
		sb.append(inputs);
		sb.append(")");
		// Step 3: Output
		Member<?> output = info.output();
		sb.append(" -> ");
		switch (output.getIOType()) {
			case OUTPUT:
				sb.append(typeString(output.getType(), false));
				break;
			case MUTABLE:
				sb.append("None");
//				sb.append("None [Overwrites MUTABLE]");
				break;
			case CONTAINER:
				sb.append("None");
//				sb.append("None [Fills CONTAINER]");
				break;
			default:
				throw new IllegalArgumentException("Invalid IO type: " + output.getIOType());
		}
		return sb.toString();
	}

	/**
	 * Appends a {@link Member} to the {@link StringBuilder} writing the Op
	 * string.
	 *
	 * @param sb      the {@link StringBuilder}
	 * @param arg     the {@link Member} being appended to {@code sb}
	 * @param special the {@link Member} to highlight
	 * @param verbose appends a verbose description iff {@code true}
	 */
	private static void appendParam(final StringBuilder sb, final Member<?> arg,
													 final Member<?> special, final boolean verbose) {
			if (arg == special) sb.append("==> \t"); // highlight special item
			else sb.append("\t\t");
			sb.append(typeString(arg.getType(), verbose));
			sb.append(" ");
			sb.append(arg.getKey());
			if (!arg.isRequired()) sb.append(" = null");
			if (!arg.getDescription().isEmpty()) {
					sb.append(" -> ");
					sb.append(arg.getDescription());
			}
			sb.append("\n");
	}

	private static String typeString(final Type input, final boolean verbose) {
			var str = input.getTypeName();
			if (verbose) return str;
			if (input instanceof TypeVariable<?>) {
					var bounds = ((TypeVariable<?>)input).getBounds();
					String[] s = new String[bounds.length];
					for(int i = 0; i < s.length; i++) {
							s[i] = typeString(Types.raw(bounds[i]), verbose);
					}
					return String.join("+", s);
			}
			else if (input instanceof ParameterizedType) {
					var pType = (ParameterizedType) input;
					var raw = typeString(pType.getRawType(), verbose);
					Type[] args = pType.getActualTypeArguments();
					String[] s = new String[args.length];
					for(int i = 0; i < args.length; i++) {
							s[i] = typeString(args[i], verbose);
					}
					return raw + "<" + String.join(", ", s) + ">";
			}
			return str.replaceAll("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*([a-zA-Z_$][a-zA-Z\\d_$]*)", "$2");
	}
}
