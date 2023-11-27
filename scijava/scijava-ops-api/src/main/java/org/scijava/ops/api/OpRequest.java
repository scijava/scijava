/*
 * #%L
 * SciJava Operations API: Outward-facing Interfaces used by the SciJava Operations framework.
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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
import java.util.Optional;
import java.util.stream.Collectors;

import org.scijava.types.Nil;
import org.scijava.types.Types;

/**
 * Data structure which identifies an Op by name and/or type(s) and/or argument
 * type(s), along with a list of input arguments.
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Curtis Rueden
 */
public interface OpRequest {

	// -- OpRef methods --

	/** Gets the name of the op. */
	String getName();

	/** Gets the type which the op must match. */
	Type getType();

	/**
	 * Gets the op's output type constraint, or null for no constraint.
	 */
	Type getOutType();

	/** Gets the op's arguments. */
	Type[] getArgs();

	/**
	 * Gets a label identifying the op's scope (i.e., its name and/or types).
	 */
	String getLabel();

	boolean typesMatch(final Type opType);

	/**
	 * Determines whether the specified type satisfies the op's required types
	 * using {@link Types#isApplicable(Type[], Type[])}.
	 */
	boolean typesMatch(final Type opType,
		final Map<TypeVariable<?>, Type> typeVarAssigns);

	// -- Object methods --

	default String requestString() {
		String n = getName() == null ? "" : "Name: \"" + getName() + "\", Types: ";
		n += getType() + "\n";
		n += "Input Types: \n";
		for (Type arg : getArgs()) {
			n += "\t\t* ";
			n += arg == null ? "" : arg.getTypeName();
			n += "\n";
		}
		n += "Output Type: \n";
		n += "\t\t* ";
		n += getOutType() == null ? "" : getOutType().getTypeName();
		n += "\n";
		return n.substring(0, n.length() - 1);
	}

	default boolean requestEquals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final OpRequest other = (OpRequest) obj;
		if (!Objects.equals(getName(), other.getName())) return false;
		if (!Objects.equals(getType(), other.getType())) return false;
		if (!Objects.equals(getOutType(), other.getOutType())) return false;
		if (!Arrays.equals(getArgs(), other.getArgs())) return false;
		return true;
	}

	default int requestHashCode() {
		return Arrays.deepHashCode(new Object[] { getName(), getType(),
			getOutType(), getArgs() });
	}

	// -- Utility methods --

	static Type[] filterNulls(final Type... types) {
		Type[] ts = Arrays.stream(types).filter(t -> t != null).toArray(
			Type[]::new);
		return ts == null ? null : ts;
	}

	// -- Helper methods --

	static void append(final StringBuilder sb, final String s) {
		if (s == null) return;
		if (sb.length() > 0) sb.append("/");
		sb.append(s);
	}

}

class PartialOpRequest implements OpRequest {

	private final Optional<String> name;
	private final Optional<Type[]> args;
	private final Optional<Type> outType;

	PartialOpRequest() {
		this.name = Optional.empty();
		this.args = Optional.empty();
		this.outType = Optional.empty();
	}

	PartialOpRequest(String name) {
		this.name = Optional.of(name);
		this.args = Optional.empty();
		this.outType = Optional.empty();
	}

	PartialOpRequest(String name, Nil<?>[] args) {
		this.name = Optional.of(name);
		Type[] tmp = Arrays.stream(args).map(nil -> nil == null ? null : nil
			.getType()).toArray(Type[]::new);
		this.args = Optional.of(tmp);
		this.outType = Optional.empty();
	}

	PartialOpRequest(String name, Nil<?>[] args, Nil<?> outType) {
		this.name = Optional.of(name);
		Type[] tmp = Arrays.stream(args).map(nil -> nil == null ? null : nil
			.getType()).toArray(Type[]::new);
		this.args = Optional.of(tmp);
		this.outType = Optional.of(outType.getType());
	}

	@Override
	public String getName() {
		return name.orElse(null);
	}

	@Override
	public Type getType() {
		throw new UnsupportedOperationException(
			"PartialOpRequests do not have a Type!");
	}

	@Override
	public Type getOutType() {
		return outType.orElse(null);
	}

	@Override
	public Type[] getArgs() {
		return args.orElse(null);
	}

	@Override
	public String getLabel() {
		throw new UnsupportedOperationException(
			"PartialOpRequests do not have a Label!");
	}

	@Override
	public boolean typesMatch(Type opType) {
		throw new UnsupportedOperationException(
			"PartialOpRequests cannot match types!");
	}

	@Override
	public boolean typesMatch(Type opType,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		throw new UnsupportedOperationException(
			"PartialOpRequests cannot match types!");
	}
}
