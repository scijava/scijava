/*-
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

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRequest;
import org.scijava.types.Nil;
import org.scijava.types.Types;
import org.scijava.types.infer.GenericAssignability;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class InfoMatchingOpRequest implements OpRequest {

	/** Name of the op, or null for any name. */
	private final String name;

	/** Type which the op must match. */
	private final Type type;

	/** The op's output parameter types, or null for no constraints. */
	private final Type outType;

	/** Arguments to be passed to the op. */
	private final Type[] args;

	/**
	 * Mapping of TypeVariables of the {@link OpInfo} to the {@link Type}s of
	 * {@code specialType}
	 */
	private final Map<TypeVariable<?>, Type> map = new HashMap<>();

	public InfoMatchingOpRequest(OpInfo info, Nil<?> specialType) {
		this.name = info.names().get(0);
		Type from = specialType.getType();
		Type to = info.opType();
		this.type = Types.getExactSuperType(to, Types.raw(from));
		if (this.type instanceof ParameterizedType) {
			if (!GenericAssignability.checkGenericAssignability(from,
				(ParameterizedType) this.type, this.map, true))
				throw new IllegalArgumentException();
		}
		else {
			if (!Types.isAssignable(from, this.type, this.map))
				throw new IllegalArgumentException();
		}
		args = info.inputs().stream().map(m -> mappedType(m.type(), this.map))
			.toArray(Type[]::new);
		outType = mappedType(info.outputType(), this.map);
	}

	private Type mappedType(Type t, Map<TypeVariable<?>, Type> map) {
		try {
			return Types.substituteTypeVariables(t, map);
		}
		catch (Exception e) {
			return t;
		}
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public Type type() {
		return this.type;
	}

	@Override
	public Type outType() {
		return this.outType;
	}

	@Override
	public Type[] argTypes() {
		return this.args;
	}

	@Override
	public String label() {
		final StringBuilder sb = new StringBuilder();
		OpRequest.append(sb, name);
		if (type != null) {
			OpRequest.append(sb, Types.name(type));
		}
		return sb.toString();
	}

	@Override
	public boolean typesMatch(Type opType,
		Map<TypeVariable<?>, Type> typeVarAssigns)
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
