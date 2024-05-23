/*
 * #%L
 * The public API of SciJava Ops.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

package org.scijava.ops.api;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.scijava.types.Nil;
import org.scijava.types.Types;

/**
 * Data structure which identifies an Op by name and/or type(s) and/or argument
 * type(s), along with a list of input arguments.
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public interface OpRequest {

	// -- OpRef methods --

	/**
	 * Gets the name of the requested Op.
	 *
	 * @return the name of the requested Op
	 */
	String name();

	/**
	 * Gets the <b>functional</b> Op {@link Type} requested.
	 *
	 * @return the functional Op type requested
	 */
	Type type();

	/**
	 * Gets the request's expected output {@link Type}.
	 *
	 * @return the desired {@link Type} of output objects.
	 */
	Type outType();

	/**
	 * Gets the request's argument types.
	 *
	 * @return the {@link Type}s of the arguments that the user wishes to pass to
	 *         the Op
	 */
	Type[] argTypes();

	/**
	 * Gets a label identifying the Op's scope (i.e., its name and/or types).
	 *
	 * @return a label identifying the Op's scope
	 */
	String label();

	/**
	 * Determines whether the specified type satisfies the op's required types
	 * using {@link Types#isApplicable(Type[], Type[])}.
	 */
	boolean typesMatch(final Type opType,
		final Map<TypeVariable<?>, Type> typeVarAssigns);

	// -- Object methods --

	default String requestString() {
		StringBuilder n = new StringBuilder(name() == null ? "" : "Name: \"" +
			name() + "\", Types: ");
		n.append(type()).append("\n");
		n.append("Input Types: \n");
		for (Type arg : argTypes()) {
			n.append("\t\t* ");
			n.append(arg == null ? "" : arg.getTypeName());
			n.append("\n");
		}
		n.append("Output Type: \n");
		n.append("\t\t* ");
		n.append(outType() == null ? "" : outType().getTypeName());
		n.append("\n");
		return n.substring(0, n.length() - 1);
	}

	default boolean requestEquals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final OpRequest other = (OpRequest) obj;
		if (!Objects.equals(name(), other.name())) return false;
		if (!Objects.equals(type(), other.type())) return false;
		if (!Objects.equals(outType(), other.outType())) return false;
		return Arrays.equals(argTypes(), other.argTypes());
	}

	default int requestHashCode() {
		return Arrays.deepHashCode(new Object[] { name(), type(), outType(),
			argTypes() });
	}

	// -- Utility methods --

	static Type[] filterNulls(final Type... types) {
		return Arrays.stream(types) //
			.filter(Objects::nonNull) //
			.toArray(Type[]::new);
	}

	// -- Helper methods --

	static void append(final StringBuilder sb, final String s) {
		if (s == null) return;
		if (sb.length() > 0) sb.append("/");
		sb.append(s);
	}

}

class PartialOpRequest implements OpRequest {

	private final String name;
	private final Type[] args;
	private final Type outType;

	PartialOpRequest() {
		this(null);
	}

	PartialOpRequest(String name) {
		this(name, null);
	}

	PartialOpRequest(String name, Nil<?>[] args) {
		this(name, args, null);
	}

	PartialOpRequest(String name, Nil<?>[] args, Nil<?> outType) {
		this.name = name;
		this.args = args == null ? null : Arrays.stream(args) //
			.map(nil -> nil == null ? null : nil.type()) //
			.toArray(Type[]::new);
		this.outType = outType == null ? null : outType.type();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Type type() {
		throw new UnsupportedOperationException(
			"PartialOpRequests do not have a Type!");
	}

	@Override
	public Type outType() {
		return outType;
	}

	@Override
	public Type[] argTypes() {
		return args;
	}

	@Override
	public String label() {
		throw new UnsupportedOperationException(
			"PartialOpRequests do not have a Label!");
	}

	@Override
	public boolean typesMatch(Type opType,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		throw new UnsupportedOperationException(
			"PartialOpRequests cannot match types!");
	}
}
