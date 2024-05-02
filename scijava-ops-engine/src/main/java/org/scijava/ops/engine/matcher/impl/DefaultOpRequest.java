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

package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.scijava.ops.api.OpRequest;
import org.scijava.ops.engine.matcher.OpMatcher;
import org.scijava.types.Types;
import org.scijava.types.inference.GenericAssignability;

/**
 * Data structure which identifies an Op by name and/or type(s) and/or argument
 * type(s), along with a list of input arguments.
 * <p>
 * With the help of the {@link OpMatcher}, an {@link OpRequest} holds all
 * information needed to create an appropriate Op.
 * </p>
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Curtis Rueden
 */
public class DefaultOpRequest implements OpRequest {

	/** Name of the op, or null for any name. */
	private final String name;

	/** Type which the op must match. */
	private final Type type;

	/** The op's output parameter types, or null for no constraints. */
	private final Type outType;

	/** Arguments to be passed to the op. */
	private final Type[] args;

	// -- Static construction methods --

	public static DefaultOpRequest fromTypes(final String name, final Type type,
		final Type outType, final Type... args)
	{
		return new DefaultOpRequest(name, type, outType, OpRequest.filterNulls(
			args));
	}

	// -- Constructor --

	/**
	 * Creates a new op request.
	 *
	 * @param name name of the op, or null for any name.
	 * @param type type which the ops must match.
	 * @param outType the op's required output type.
	 * @param args arguments to the op.
	 */
	public DefaultOpRequest(final String name, final Type type,
		final Type outType, final Type[] args)
	{
		this.name = name;
		this.type = type;
		this.outType = outType;
		this.args = args;
	}

	// -- OpRequest methods --

	/** Gets the name of the op. */
	@Override
	public String getName() {
		return name;
	}

	/** Gets the type which the op must match. */
	@Override
	public Type getType() {
		return type;
	}

	/**
	 * Gets the op's output type constraint, or null for no constraint.
	 */
	@Override
	public Type getOutType() {
		return outType;
	}

	/** Gets the op's arguments. */
	@Override
	public Type[] getArgs() {
		return args.clone();
	}

	/**
	 * Gets a label identifying the op's scope (i.e., its name and/or types).
	 */
	@Override
	public String getLabel() {
		final StringBuilder sb = new StringBuilder();
		OpRequest.append(sb, name);
		if (type != null) {
			OpRequest.append(sb, Types.name(type));
		}
		return sb.toString();
	}

	/**
	 * Determines whether the specified type satisfies the op's required types
	 * using {@link Types#isApplicable(Type[], Type[])}.
	 */
	@Override
	public boolean typesMatch(final Type opType,
		final Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		if (type == null) return true;
		if (type instanceof ParameterizedType) {
			if (!GenericAssignability.checkGenericAssignability(opType,
				(ParameterizedType) type, typeVarAssigns, true))
			{
				return false;
			}
		}
		else {
			if (!Types.isAssignable(opType, type)) {
				return false;
			}
		}
		return true;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return requestString();
	}

	@Override
	public boolean equals(final Object obj) {
		return requestEquals(obj);
	}

	@Override
	public int hashCode() {
		return requestHashCode();
	}

}
